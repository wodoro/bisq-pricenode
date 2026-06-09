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
 * Offline test: a stubbed WebClient feeds a canned CriptoYa response so the
 * whitelist + freshness filtering and ARS rate construction run without any
 * network call.
 */
@Slf4j
public class CryptoYaTest {

    // "ripio" is whitelisted; "notwhitelisted" must be dropped. Timestamp is now
    // (seconds) so it passes the "newer than yesterday" freshness check.
    private static String cannedResponse() {
        long nowSec = System.currentTimeMillis() / 1000L;
        return String.format("""
                {
                  "ripio":{"ask":100.0,"totalAsk":101.0,"bid":99.0,"totalBid":98.0,"time":%d},
                  "notwhitelisted":{"ask":50.0,"totalAsk":51.0,"bid":49.0,"totalBid":48.0,"time":%d}
                }""", nowSec, nowSec);
    }

    @Test
    public void doGet_parsesWhitelistedArsRateOffline() {
        CryptoYa provider = new CryptoYa(new StandardEnvironment()) {
            @Override
            protected WebClient webClient() {
                return StubWebClient.returningJson(cannedResponse());
            }
        };

        Set<ExchangeRate> rates = provider.doGet();
        Set<String> currencies = rates.stream()
                .map(ExchangeRate::getCurrency)
                .collect(Collectors.toSet());

        assertTrue(currencies.contains("ARS"), "ARS rate expected, got: " + currencies);
        // Only the whitelisted "ripio" entry yields a rate.
        assertTrue(rates.size() == 1, "exactly one whitelisted rate expected, got: " + rates);
    }
}
