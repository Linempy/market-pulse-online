package academy.backend.market_pulse.filter;

import academy.backend.market_pulse.dictionary.InstrumentType;
import academy.backend.market_pulse.model.Instrument;
import lombok.RequiredArgsConstructor;

/**
 * Стратегия отбора инструментов по типу.
 * Инструмент подходит, если его тип совпадает с заданным.
 */
@RequiredArgsConstructor
public class TypeInstrumentFilter implements InstrumentFilter {

    private final InstrumentType type;

    @Override
    public boolean matches(Instrument instrument) {
        return instrument.getType() == type;
    }
}
