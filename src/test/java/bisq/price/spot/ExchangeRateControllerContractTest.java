/*
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package bisq.price.spot;

import bisq.price.common.config.Config;
import bisq.price.mining.FeeRateService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Characterization test pinning the HTTP contract of {@code /getAllMarketPrices}.
 *
 * Asserts the response shape Bisq clients depend on: the per-provider
 * {@code <prefix>Ts}/{@code <prefix>Count} metadata, the sorted {@code data}
 * array of rates, and the fee block whose key the controller renames from
 * {@code dataMap} to {@code bitcoinFeeInfo}. Uses standalone MockMvc (no
 * provider classpath scanning, no network) so it stays fast and deterministic
 * across Spring/Jackson upgrades.
 */
public class ExchangeRateControllerContractTest {

    @Test
    public void getAllMarketPrices_pinsResponseContract() throws Exception {
        ExchangeRateService exchangeRateService = mock(ExchangeRateService.class);
        FeeRateService feeRateService = mock(FeeRateService.class);

        // Representative market-price payload (provider metadata + sorted data list).
        Map<String, Object> marketPrices = new LinkedHashMap<>();
        marketPrices.put("btcAverageTs", 1700000000L);
        marketPrices.put("btcAverageCount", 1);
        marketPrices.put("data", List.of(
                new ExchangeRate("USD", 100000.5d, 1700000000000L, "TestProvider")));
        when(exchangeRateService.getAllMarketPrices()).thenReturn(marketPrices);

        // Fee service returns the LEGACY "dataMap" key; the controller must rename it.
        Map<String, Object> fees = new LinkedHashMap<>();
        fees.put(Config.BTC_FEES_TS, 1700000001L);
        Map<String, Long> feeData = new LinkedHashMap<>();
        feeData.put(Config.BTC_TX_FEE, 25L);
        feeData.put(Config.BTC_MIN_TX_FEE, 5L);
        fees.put(Config.LEGACY_FEE_DATAMAP, feeData);
        when(feeRateService.getFees()).thenReturn(fees);

        MockMvc mockMvc = MockMvcBuilders
                .standaloneSetup(new ExchangeRateController(exchangeRateService, feeRateService))
                .build();

        mockMvc.perform(get("/getAllMarketPrices"))
                .andExpect(status().isOk())
                // per-provider metadata
                .andExpect(jsonPath("$.btcAverageTs").value(1700000000L))
                .andExpect(jsonPath("$.btcAverageCount").value(1))
                // data array element keys = the pinned ExchangeRate wire contract
                .andExpect(jsonPath("$.data[0].currencyCode").value("USD"))
                .andExpect(jsonPath("$.data[0].price").value(100000.5))
                .andExpect(jsonPath("$.data[0].timestampSec").value(1700000000000L))
                .andExpect(jsonPath("$.data[0].provider").value("TestProvider"))
                // fee block: key renamed dataMap -> bitcoinFeeInfo, legacy key absent
                .andExpect(jsonPath("$.bitcoinFeesTs").value(1700000001L))
                .andExpect(jsonPath("$.bitcoinFeeInfo.btcTxFee").value(25))
                .andExpect(jsonPath("$.bitcoinFeeInfo.btcMinTxFee").value(5))
                .andExpect(jsonPath("$.dataMap").doesNotExist());
    }
}
