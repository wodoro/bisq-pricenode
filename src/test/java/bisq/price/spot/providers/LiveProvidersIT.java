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
import bisq.price.spot.ExchangeRateProvider;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Live integration tests against the real exchange APIs. Tagged {@code live}, so
 * they are excluded from the default {@code test} run and only execute via the
 * {@code liveTest} Gradle task (and the dedicated, non-blocking CI job).
 *
 * Purpose: catch upstream breakage the mocked unit tests cannot — endpoint
 * changes, response-shape drift (parse failures), or a provider returning
 * currencies outside the Bisq-supported set. A provider being temporarily down
 * or geoblocked yields an empty result, which is tolerated; a hard failure
 * (exception / unsupported currency) is a real signal.
 */
@Slf4j
@Tag("live")
public class LiveProvidersIT {

    private static final Environment ENV = new StandardEnvironment();

    static Stream<Arguments> providers() {
        return Stream.of(
                new Binance(ENV), new Bitfinex(ENV), new Bitflyer(ENV), new Bitstamp(ENV),
                new BTCMarkets(ENV), new CoinbasePro(ENV), new Coinone(ENV),
                new IndependentReserve(ENV), new Kraken(ENV), new Luno(ENV),
                new MercadoBitcoin(ENV), new Paribu(ENV),
                new CoinGecko(ENV), new Poloniex(ENV), new Yadio(ENV), new CryptoYa(ENV)
        ).map(p -> Arguments.of(p.getName(), p));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("providers")
    public void liveDoGet_returnsOnlySupportedCurrencies(String name, ExchangeRateProvider provider) {
        Set<ExchangeRate> rates;
        try {
            rates = provider.doGet();
        } catch (WebClientResponseException e) {
            // HTTP error response, e.g. CryptoYa returns 403 outside LATAM. A known
            // geoblock/availability condition, not a contract regression -> skip.
            assumeTrue(false, name + " HTTP " + e.getStatusCode() + " (geoblock/unavailable); skipping");
            return;
        } catch (WebClientException e) {
            // Connection/DNS/timeout — environment, not a regression -> skip.
            assumeTrue(false, name + " unreachable: " + e.getMessage());
            return;
        }
        assertNotNull(rates, name + " returned null rates");
        rates.forEach(r -> log.info("{} live rate {}", name, r));

        // Any rate that comes back must be for a Bisq-supported currency; otherwise
        // the upstream response shape has drifted from what the provider expects.
        Set<String> supported = Sets.union(
                provider.getSupportedFiatCurrencies(), provider.getSupportedCryptoCurrencies());
        Set<String> unsupported = rates.stream()
                .map(ExchangeRate::getCurrency)
                .filter(c -> !supported.contains(c))
                .collect(Collectors.toSet());
        assertTrue(unsupported.isEmpty(), name + " returned unsupported currencies: " + unsupported);
    }

    /**
     * Pins the ETB (Ethiopian Birr) whitelist end-to-end against the live Yadio
     * API. Tolerates the API being unavailable. See bisq-network/bisq-mobile#1434.
     */
    @ParameterizedTest(name = "Yadio ETB")
    @MethodSource("yadioProvider")
    public void liveYadio_includesETB(Yadio yadio) {
        Set<String> currencies = yadio.doGet().stream()
                .map(ExchangeRate::getCurrency)
                .collect(Collectors.toSet());
        assumeFalse(currencies.isEmpty(), "Yadio API returned no rates (down/geoblocked); skipping");
        assertTrue(currencies.contains("ETB"),
                "ETB should be among Yadio's live rates. Actual: " + currencies);
    }

    static Stream<Yadio> yadioProvider() {
        return Stream.of(new Yadio(ENV));
    }
}
