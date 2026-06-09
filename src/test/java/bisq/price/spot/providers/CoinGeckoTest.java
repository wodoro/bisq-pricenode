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
 * Offline test: a stubbed WebClient feeds a canned CoinGecko response so the
 * real deserialization and rate-mapping run without any network call.
 */
@Slf4j
public class CoinGeckoTest {

    private static final String CANNED_RESPONSE = """
            {"rates":{
              "usd":{"name":"US Dollar","unit":"$","value":50000,"type":"fiat"},
              "eur":{"name":"Euro","unit":"E","value":46000,"type":"fiat"}
            }}""";

    @Test
    public void doGet_parsesRatesOffline() {
        CoinGecko provider = new CoinGecko(new StandardEnvironment()) {
            @Override
            protected WebClient webClient() {
                return StubWebClient.returningJson(CANNED_RESPONSE);
            }
        };

        Set<String> currencies = provider.doGet().stream()
                .map(ExchangeRate::getCurrency)
                .collect(Collectors.toSet());

        assertTrue(currencies.contains("USD"), "USD rate expected, got: " + currencies);
        assertTrue(currencies.contains("EUR"), "EUR rate expected, got: " + currencies);
    }
}
