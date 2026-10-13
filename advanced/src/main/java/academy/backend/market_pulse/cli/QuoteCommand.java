package academy.backend.market_pulse.cli;

import academy.backend.market_pulse.model.Quote;
import academy.backend.market_pulse.service.QuoteService;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

@Command(name = "quote", description = "Получить котировки по одному или нескольким тикерам")
public class QuoteCommand implements Callable<Integer> {


    @Parameters(arity = "1..*", description = "Тикеры инструментов")
    private List<String> tickers;

    private final QuoteService quoteService;

    public QuoteCommand(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @Override
    public Integer call() {
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        AtomicInteger errorCount = new AtomicInteger();

        for (String ticker : tickers) {
            CompletableFuture<Optional<Quote>> result = quoteService.request(ticker);
            CompletableFuture<Void> printed = result.thenAccept(quote -> {
                        quote.ifPresentOrElse(
                                System.out::println,
                                () -> System.out.println("Котировка не найдена: " + ticker.toUpperCase()));
                    })
                    .exceptionally(ex -> {
                        System.out.printf("Ошибка обработки %s: %s%n", ticker, ex.getMessage());
                        errorCount.incrementAndGet();
                        return null;
                    });

            futures.add(printed);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0])).join();
        return errorCount.get() > 0 ? 1 : 0;
    }
}
