package academy.backend.market_pulse.filter;

import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.model.Instrument;
import lombok.RequiredArgsConstructor;

/**
 * Стратегия отбора инструментов по валюте.
 * Инструмент подходит, если его валюта совпадает с заданной.
 */
@RequiredArgsConstructor
public class CurrencyInstrumentFilter implements InstrumentFilter {

    private final Currency currency;

    @Override
    public boolean matches(Instrument instrument) {
        return instrument.getCurrency() == currency;
    }
}
