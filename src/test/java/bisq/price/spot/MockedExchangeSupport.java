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

import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.instrument.Instrument;
import org.knowm.xchange.service.marketdata.MarketDataService;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Test support for XChange-based {@link ExchangeRateProvider}s. Lives in package
 * {@code bisq.price.spot} so it can stub the package-visible
 * {@link ExchangeRateProvider#createExchange} seam.
 *
 * Wraps a provider in a Mockito spy whose exchange client is mocked to serve
 * canned BTC/EUR and BTC/USD tickers, so {@code doGet()} runs the real parsing
 * logic with no network access.
 */
public final class MockedExchangeSupport {

    private MockedExchangeSupport() {
    }

    public static ExchangeRateProvider withMockedExchange(ExchangeRateProvider provider) {
        ExchangeRateProvider spy = spy(provider);

        CurrencyPair btcEur = new CurrencyPair("BTC/EUR");
        CurrencyPair btcUsd = new CurrencyPair("BTC/USD");
        List<Instrument> instruments = List.of(btcEur, btcUsd);
        List<Ticker> tickers = List.of(
                new Ticker.Builder().instrument(btcEur).last(new BigDecimal("50000")).timestamp(new Date()).build(),
                new Ticker.Builder().instrument(btcUsd).last(new BigDecimal("55000")).timestamp(new Date()).build());

        Exchange exchange = mock(Exchange.class);
        MarketDataService marketDataService = mock(MarketDataService.class);
        try {
            when(exchange.getExchangeInstruments()).thenReturn(instruments);
            when(exchange.getMarketDataService()).thenReturn(marketDataService);
            when(marketDataService.getTickers(any())).thenReturn(tickers);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        doReturn(exchange).when(spy).createExchange(any());
        return spy;
    }
}
