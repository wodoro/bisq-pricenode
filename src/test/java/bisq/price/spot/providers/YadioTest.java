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

import bisq.price.AbstractExchangeRateProviderTest;
import bisq.price.spot.ExchangeRate;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.StandardEnvironment;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

@Slf4j
public class YadioTest extends AbstractExchangeRateProviderTest {

    @Test
    public void doGet_successfulCall() {
        doGet_successfulCall(new Yadio(new StandardEnvironment()));
    }

    /**
     * Pins the whitelist behavior for ETB (Ethiopian Birr) so that any future
     * change accidentally removing it from {@code YADIO_CURRENCIES_WHITELIST}
     * would be caught here.
     * <p>
     * Lives alongside {@link #doGet_successfulCall()} which is the broader
     * smoke test. We tolerate the live Yadio API being unavailable (empty
     * result) to avoid CI flakiness — when the API is reachable, ETB must
     * be among the returned rates. See bisq-network/bisq-mobile#1434.
     */
    @Test
    public void doGet_returnsETBWhenApiReachable() {
        Set<ExchangeRate> rates = new Yadio(new StandardEnvironment()).doGet();
        assumeFalse(rates.isEmpty(), "Yadio API returned no rates (likely network/API issue); skipping ETB assertion");

        Set<String> currencies = rates.stream()
                .map(ExchangeRate::getCurrency)
                .collect(Collectors.toSet());

        assertTrue(currencies.contains("ETB"),
                "ETB should be among Yadio's returned rates. Actual currencies: " + currencies);
    }

}
