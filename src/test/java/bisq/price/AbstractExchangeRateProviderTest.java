package bisq.price;

import bisq.price.spot.ExchangeRate;
import bisq.price.spot.ExchangeRateProvider;
import bisq.price.spot.MockedExchangeSupport;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public abstract class AbstractExchangeRateProviderTest {

    /**
     * Exercises an XChange-based provider's rate-parsing path fully offline.
     *
     * The provider is wrapped by {@link MockedExchangeSupport} in a Mockito spy
     * whose {@code createExchange} seam returns a mocked exchange serving canned
     * BTC/EUR and BTC/USD tickers. This replaces the former live API call: the
     * test is now deterministic and makes no network calls, while still running
     * the real desired-pair filtering and {@link ExchangeRate} construction.
     */
    protected void doGet_successfulCall(ExchangeRateProvider exchangeProvider) {
        ExchangeRateProvider provider = MockedExchangeSupport.withMockedExchange(exchangeProvider);

        Set<ExchangeRate> retrievedExchangeRates = provider.doGet();
        retrievedExchangeRates.forEach(e -> log.info("Found exchange rate " + e.toString()));

        // The canned tickers map to EUR and USD, both Bisq-supported fiat, so the
        // provider must yield rates. (A provider that excludes one still yields the other.)
        assertFalse(retrievedExchangeRates.isEmpty(),
                "provider should parse the mocked tickers into exchange rates");
        checkProviderCurrencyPairs(provider, retrievedExchangeRates);
    }

    /**
     * Check that every retrieved currency pair is between BTC and either
     * A) a fiat currency on the list of Bisq-supported fiat currencies, or
     * B) an altcoin on the list of Bisq-supported altcoins
     *
     * @param retrievedExchangeRates Exchange rates retrieved from the provider
     */
    private void checkProviderCurrencyPairs(ExchangeRateProvider exchangeProvider, Set<ExchangeRate> retrievedExchangeRates) {
        Set<String> retrievedRatesCurrencies = retrievedExchangeRates.stream()
                .map(ExchangeRate::getCurrency)
                .collect(Collectors.toSet());

        Set<String> supportedFiatCurrenciesRetrieved = exchangeProvider.getSupportedFiatCurrencies().stream()
                .filter(retrievedRatesCurrencies::contains)
                .collect(Collectors.toCollection(TreeSet::new));
        log.info("Retrieved rates for supported fiat currencies: " + supportedFiatCurrenciesRetrieved);

        Set<String> supportedCryptoCurrenciesRetrieved = exchangeProvider.getSupportedCryptoCurrencies().stream()
                .filter(retrievedRatesCurrencies::contains)
                .collect(Collectors.toCollection(TreeSet::new));
        log.info("Retrieved rates for supported altcoins: " + supportedCryptoCurrenciesRetrieved);

        Set<String> supportedCurrencies = Sets.union(
                exchangeProvider.getSupportedCryptoCurrencies(),
                exchangeProvider.getSupportedFiatCurrencies());

        Set<String> unsupportedCurrencies = Sets.difference(retrievedRatesCurrencies, supportedCurrencies);
        assertTrue(unsupportedCurrencies.isEmpty(),
                "Retrieved exchange rates contain unsupported currencies: " + unsupportedCurrencies);
    }
}
