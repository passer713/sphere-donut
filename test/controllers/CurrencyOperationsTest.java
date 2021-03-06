package controllers;

import com.google.common.base.Optional;
import com.typesafe.config.ConfigFactory;
import exceptions.DefaultCurrencyNotFound;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.Configuration;
import play.mvc.Http;

import java.util.Collections;
import java.util.Currency;
import java.util.Map;

import static controllers.CurrencyOperations.CURRENCY_CONFIG;
import static controllers.CurrencyOperations.parseCode;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.*;

public class CurrencyOperationsTest {
    public static final Currency EUR = Currency.getInstance("EUR");
    public static final Currency USD = Currency.getInstance("USD");

    @Before
    public void setUp() throws Exception {
        final Http.Context emptyContext = new Http.Context(null, null, null,
                Collections.<String, String>emptyMap(),
                Collections.<String, String>emptyMap(),
                Collections.<String, Object>emptyMap());
        Http.Context.current.set(emptyContext);
    }

    @After
    public void tearDown() throws Exception {
        Http.Context.current.remove();
    }

    @Test
    public void getsCurrencyFromConfiguration() {
        assertThat(opsWithCurrency("EUR").currency()).isEqualTo(EUR);
    }

    @Test
    public void throwsExceptionWhenInvalidCurrency() {
        try {
            opsWithCurrency("INVALID").currency();
            fail("DefaultCurrencyNotFound exception expected with invalid currency");
        } catch (DefaultCurrencyNotFound e) {
            assertThat(e.getMessage()).isNotEmpty();
        }
    }

    @Test
    public void throwsExceptionWhenNoneConfigured() {
        try {
            opsWithCurrency(Optional.<String>absent()).currency();
            fail("DefaultCurrencyNotFound exception expected with missing currency");
        } catch (DefaultCurrencyNotFound e) {
            assertThat(e.getMessage()).isNotEmpty();
        }
    }

    @Test
    public void parserReturnsCurrencyWhenValidCurrencyCode() {
        assertThat(parseCode("USD").get()).isEqualTo(USD);
    }

    @Test
    public void parserReturnsEmptyWhenInvalidCurrencyCode() {
        assertThat(parseCode("INVALID").isPresent()).isFalse();
    }

    private CurrencyOperations opsWithCurrency(final String currencyInConfig) {
        return opsWithCurrency(Optional.of(currencyInConfig));
    }

    private CurrencyOperations opsWithCurrency(final Optional<String> currencyInConfig) {
        final Configuration configuration = configWithCurrency(currencyInConfig);
        return CurrencyOperations.of(configuration);
    }

    private Configuration configWithCurrency(final Optional<String> currency) {
        final Map<String, String> configMap;
        if (currency.isPresent()) {
            configMap = singletonMap(CURRENCY_CONFIG, currency.get());
        } else {
            configMap = emptyMap();
        }
        return new Configuration(ConfigFactory.parseMap(configMap));
    }
}
