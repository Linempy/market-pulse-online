package academy.backend.market_pulse.service;

import academy.backend.market_pulse.model.Quote;

import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Обработка пользовательских запросов котировок. Запросы копятся в очереди {@link Queue} и
 * обрабатываются по порядку поступления (FIFO); полученные котировки складываются в кеш {@link Map},
 * чтобы повторный запрос не ходил к источнику снова. Разделение «положить запрос» и «обработать» —
 * заготовка под асинхронную обработку.
 */
// NOTICE: акцент при показе — разделение request()/processAll(). В однопоточном CLI это выглядит
// избыточно, но это именно та точка, в которую позже встанет асинхронная обработка (отдельный
// поток/пул разбирает очередь, не блокируя вызывающего).
public class QuoteService implements AutoCloseable {

    private static final int QUEUE_CAPACITY = 100;
    private static final long TIME_AWAIT = 5;

    private final QuoteSource source;
    // NOTICE: ArrayDeque — реализация Queue общего назначения на кольцевом буфере, offer/poll —
    // O(1) на обоих концах, накладных расходов меньше, чем у LinkedList.
    private final BlockingQueue<QuoteRequest> pending = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
    private final ConcurrentMap<String, Quote> cache = new ConcurrentHashMap<>();
    private final ExecutorService workerPool;

    private boolean closed = false;

    public QuoteService(QuoteSource source) {
        this.source = source;
        this.workerPool = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "QuoteService-Worker");
            t.setDaemon(true);
            return t;
        });
        this.workerPool.execute(this::processRequests);
    }

    public record QuoteRequest(String ticker, CompletableFuture<Optional<Quote>> quote) {
    }

    public CompletableFuture<Optional<Quote>> request(String ticker) {
        var result = new CompletableFuture<Optional<Quote>>();

        synchronized (this) {
            if (closed) {
                result.completeExceptionally(new IllegalStateException("Сервис котировок недоступен"));
                return result;
            }

            boolean isAdded = pending.offer(new QuoteRequest(ticker.toUpperCase(), result));
            if (!isAdded) {
                result.completeExceptionally(new TimeoutException("Очередь запросов переполнена"));
            }
        }

        return result;
    }

    public void processRequests() {
        while (!Thread.currentThread().isInterrupted()) {
            QuoteRequest request = null;
            try {
                request = pending.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            String ticker = request.ticker;
            if (cache.containsKey(ticker)) {
                request.quote.complete(Optional.ofNullable(cache.get(ticker)));
                continue;
            }

            Optional<Quote> fetched = source.fetch(ticker);
            fetched.ifPresentOrElse(
                    quote -> cache.put(ticker, quote),
                    () -> {
                    }
            );
            request.quote.complete(fetched);
        }
    }

    public Optional<Quote> cached(String ticker) {
        return Optional.ofNullable(cache.get(ticker.toUpperCase()));
    }

    @Override
    public synchronized void close() {
        if (closed) {
            return;
        }
        closed = true;

        workerPool.shutdownNow();
        try {
            if (!workerPool.awaitTermination(TIME_AWAIT, TimeUnit.SECONDS)) {
                workerPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            workerPool.shutdownNow();
            Thread.currentThread().interrupt();
        }

        QuoteRequest request;
        while ((request = pending.poll()) != null) {
            request.quote.completeExceptionally(new CancellationException("Сервис котировок отклонил запрос"));
        }
    }
}
