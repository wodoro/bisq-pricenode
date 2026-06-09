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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Offline test: a stubbed WebClient feeds a canned Yadio response so the
 * whitelist + validity filtering and rate construction run without any network
 * call. The timestamp is "now" so the ticker passes its freshness check.
 */
@Slf4j
public class YadioTest {

    // ARS + ETB are whitelisted; ZZZ is not -> must be filtered out.
    private static String cannedResponse() {
        long nowMs = System.currentTimeMillis();
        return String.format("""
                {"BTC":1.0E-5,"base":"USD","timestamp":%d,
                 "USD":{"ARS":1000.0,"ETB":120.0,"ZZZ":1.0}}""", nowMs);
    }

    private Yadio provider() {
        return new Yadio(new StandardEnvironment()) {
            @Override
            protected WebClient webClient() {
                return StubWebClient.returningJson(cannedResponse());
            }
        };
    }

    @Test
    public void doGet_parsesWhitelistedRatesOffline() {
        Set<String> currencies = provider().doGet().stream()
                .map(ExchangeRate::getCurrency)
                .collect(Collectors.toSet());

        assertTrue(currencies.contains("ARS"), "ARS expected, got: " + currencies);
        assertFalse(currencies.contains("ZZZ"), "non-whitelisted ZZZ must be filtered, got: " + currencies);
    }

    /**
     * Pins the whitelist behavior for ETB (Ethiopian Birr) so that any future
     * change accidentally removing it from {@code YADIO_CURRENCIES_WHITELIST}
     * would be caught here. See bisq-network/bisq-mobile#1434.
     */
    @Test
    public void doGet_returnsETB() {
        Set<String> currencies = provider().doGet().stream()
                .map(ExchangeRate::getCurrency)
                .collect(Collectors.toSet());

        assertTrue(currencies.contains("ETB"),
                "ETB should be among Yadio's returned rates. Actual currencies: " + currencies);
    }
}
