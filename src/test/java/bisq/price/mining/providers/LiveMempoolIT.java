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

package bisq.price.mining.providers;

import bisq.price.mining.FeeRate;
import bisq.price.mining.FeeRateProvider;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Live integration test against the real mempool.space fee API. Tagged
 * {@code live} (excluded from the default run; executes via {@code liveTest}).
 * Catches endpoint/response changes the mocked unit test cannot.
 */
@Tag("live")
public class LiveMempoolIT {

    private static final Environment ENV = new StandardEnvironment();

    @Test
    public void liveDoGet_returnsFeeWithinBounds() {
        MempoolFeeRateProvider provider = new MempoolFeeRateProvider.First(ENV);

        FeeRate feeRate = provider.doGet();

        // A null rate means the endpoint was unreachable (environment), not a contract
        // regression -> skip. When data is returned it must be within the valid bounds.
        assumeTrue(feeRate != null, "mempool API unreachable; skipping");
        assertTrue(feeRate.getPrice() >= FeeRateProvider.MIN_FEE_RATE_FOR_TRADING,
                "fee below minimum: " + feeRate.getPrice());
        assertTrue(feeRate.getPrice() <= FeeRateProvider.MAX_FEE_RATE,
                "fee above maximum: " + feeRate.getPrice());
    }
}
