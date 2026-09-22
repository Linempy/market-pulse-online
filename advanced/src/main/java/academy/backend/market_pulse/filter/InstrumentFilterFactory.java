package academy.backend.market_pulse.filter;

import academy.backend.market_pulse.dictionary.InstrumentType;
import academy.backend.market_pulse.model.Currency;

import java.math.BigDecimal;

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

        int filtersCount = 0;

        filtersCount += type != null ? 1 : 0;
        filtersCount += ticker != null ? 1 : 0;
        filtersCount += currency != null ? 1 : 0;
        filtersCount += hasPrice ? 1 : 0;

        if (filtersCount > 1) {
            throw new IllegalArgumentException(
                    "Можно использовать только один критерий фильтрации"
            );
        }
    }
}
