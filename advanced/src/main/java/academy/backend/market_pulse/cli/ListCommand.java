package academy.backend.market_pulse.cli;

import academy.backend.market_pulse.dictionary.InstrumentType;
import academy.backend.market_pulse.filter.InstrumentFilter;
import academy.backend.market_pulse.filter.InstrumentFilterFactory;
import academy.backend.market_pulse.filter.PriceOperator;
import academy.backend.market_pulse.model.Currency;
import academy.backend.market_pulse.repository.InstrumentRepository;
import lombok.RequiredArgsConstructor;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.math.BigDecimal;
import java.util.concurrent.Callable;

@RequiredArgsConstructor
@Command(name = "list", description = "Список инструментов")
public class ListCommand implements Callable<Integer> {

    @Option(names = "--type", description = "Фильтр по типу инструмента")
    private InstrumentType type;

    @Option(names = "--ticker", description = "Фильтр по подстроке в тикере без учёта регистра")
    private String ticker;

    @Option(names = "--currency", description = "Фильтр по валюте инструмента")
    private Currency currency;

    @Option(names = "--price-op", description = "Оператор сравнения доходности акции (LT, LE, EQ, GE, GT)")
    private PriceOperator priceOperator;

    @Option(names = "--price", description = "Значение дивидендной доходности акции для сравнения")
    private BigDecimal price;

    private final InstrumentRepository repository;

    @Override
    public Integer call() {
        InstrumentFilter filter = InstrumentFilterFactory.create(
                type,
                ticker,
                currency,
                priceOperator,
                price
        );

        for (var instrument : repository) {
            if (filter.matches(instrument)) {
                System.out.println(instrument.getDescription());
            }
        }

        return 0;
    }
}
