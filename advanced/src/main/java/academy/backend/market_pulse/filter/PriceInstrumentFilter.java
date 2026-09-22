package academy.backend.market_pulse.filter;

import academy.backend.market_pulse.model.Instrument;
import academy.backend.market_pulse.model.Stock;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

/**
 * Стратегия отбора акций по дивидендной доходности.
 * Для инструментов, не являющихся акциями, всегда возвращает {@code false}.
 */
@RequiredArgsConstructor
public class PriceInstrumentFilter implements InstrumentFilter {

    private final PriceOperator operator;
    private final BigDecimal price;

    @Override
    public boolean matches(Instrument instrument) {
        if (!(instrument instanceof Stock stock)) {
            return false;
        }

        int result = stock.getDividendYield().compareTo(price);
        return switch (operator) {
            case LT -> result < 0;
            case LE -> result <= 0;
            case GT -> result > 0;
            case GE -> result >= 0;
            case EQ -> result == 0;
        };
    }
}
