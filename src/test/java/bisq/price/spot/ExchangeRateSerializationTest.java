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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Characterization test pinning the wire format of {@link ExchangeRate}.
 *
 * Bisq exchange clients parse this JSON; the field names come from
 * {@code @JsonProperty} renames (currency -> currencyCode, timestamp ->
 * timestampSec) and an explicit field order. A Jackson/Spring upgrade must not
 * silently change any of this, so the contract is asserted byte-for-byte.
 */
public class ExchangeRateSerializationTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void exchangeRate_serializesToStableClientContract() throws Exception {
        ExchangeRate rate = new ExchangeRate("USD", 100000.5d, 1700000000000L, "TestProvider");

        String json = mapper.writeValueAsString(rate);

        // Exact wire format: renamed keys, declared order, numeric (unquoted) price/timestamp.
        assertEquals(
                "{\"currencyCode\":\"USD\",\"price\":100000.5,\"timestampSec\":1700000000000,\"provider\":\"TestProvider\"}",
                json);
    }

    @Test
    public void exchangeRate_doesNotLeakInternalGetterNames() throws Exception {
        String json = mapper.writeValueAsString(
                new ExchangeRate("EUR", 95000d, 1700000000000L, "P"));

        // The internal field/getter names must NOT appear on the wire.
        assertFalse(json.contains("\"currency\""), "must serialize as currencyCode");
        assertFalse(json.contains("\"timestamp\""), "must serialize as timestampSec");
    }
}
