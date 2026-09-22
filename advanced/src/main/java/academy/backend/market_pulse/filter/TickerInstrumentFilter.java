package academy.backend.market_pulse.filter;

import academy.backend.market_pulse.model.Instrument;
import lombok.RequiredArgsConstructor;

/**
 * Стратегия отбора инструментов по части тикера.
 * Сравнение выполняется без учёта регистра.
 */
@RequiredArgsConstructor
public class TickerInstrumentFilter implements InstrumentFilter {

    private final String ticker;

    @Override
    public boolean matches(Instrument instrument) {
        return instrument.getTicker()
                .toLowerCase()
                .contains(ticker.toLowerCase());
    }
}
