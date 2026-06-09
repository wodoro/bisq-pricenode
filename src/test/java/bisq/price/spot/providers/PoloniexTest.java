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

package bisq.price.spot.providers;

import bisq.price.spot.ExchangeRate;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Offline test: a stubbed WebClient feeds a canned Poloniex ticker array, so
 * the BTC-pair filtering and rate construction run without any network call.
 */
@Slf4j
public class PoloniexTest {

    // ETH and LTC are in Poloniex's supported set; XRP is not -> must be filtered out.
    private static final String CANNED_RESPONSE = """
            [
              {"symbol":"ETH_BTC","price":"0.05"},
              {"symbol":"LTC_BTC","price":"0.0012"},
              {"symbol":"XRP_BTC","price":"0.00001"}
            ]""";

    @Test
    public void doGet_parsesSupportedBtcPairsOffline() {
        Poloniex provider = new Poloniex(new StandardEnvironment()) {
            @Override
            protected WebClient webClient() {
                return StubWebClient.returningJson(CANNED_RESPONSE);
            }
        };

        Set<String> currencies = provider.doGet().stream()
                .map(ExchangeRate::getCurrency)
                .collect(Collectors.toSet());

        assertTrue(currencies.contains("ETH"), "ETH expected, got: " + currencies);
        assertTrue(currencies.contains("LTC"), "LTC expected, got: " + currencies);
        assertTrue(!currencies.contains("XRP"), "unsupported XRP must be filtered, got: " + currencies);
    }
}
