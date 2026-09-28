package academy.backend.market_pulse.filter;

import academy.backend.market_pulse.dictionary.InstrumentType;
import academy.backend.market_pulse.model.Currency;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Создаёт стратегию фильтрации по параметрам команды {@code list}.
 */
public final class InstrumentFilterFactory {

    private InstrumentFilterFactory() {
    }

    public static InstrumentFilter create(InstrumentType type,
                                          String ticker,
                                          Currency currency,
                                          PriceOperator priceOperator,
                                          BigDecimal price) {
        validate(type, ticker, currency, priceOperator, price);

        if (type != null) {
            return new TypeInstrumentFilter(type);
        }

        if (ticker != null) {
            return new TickerInstrumentFilter(ticker);
        }

        if (currency != null) {
            return new CurrencyInstrumentFilter(currency);
        }

        if (price != null) {
            return new PriceInstrumentFilter(priceOperator, price);
        }

        return new AllInstrumentFilter();
    }

    private static void validate(InstrumentType type,
                                 String ticker,
                                 Currency currency,
                                 PriceOperator priceOperator,
                                 BigDecimal price) {
        boolean hasPrice = price != null;
        boolean hasPriceOperator = priceOperator != null;

        if (hasPrice != hasPriceOperator) {
            throw new IllegalArgumentException(
                    "--price и --price-op должны передаваться вместе"
            );
        }

        long filtersCount = Stream.of(type, ticker, currency, price).filter(Objects::nonNull).count();

        if (filtersCount > 1) {
            throw new IllegalArgumentException(
                    "Можно использовать только один критерий фильтрации"
            );
        }
    }
}
