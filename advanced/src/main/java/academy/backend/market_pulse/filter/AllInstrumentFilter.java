package academy.backend.market_pulse.filter;

import academy.backend.market_pulse.model.Instrument;

/**
 * Стратегия без фильтрации: подходит любой переданный инструмент.
 */
public class AllInstrumentFilter implements InstrumentFilter {
    @Override
    public boolean matches(Instrument instrument) {
        return true;
    }
}
