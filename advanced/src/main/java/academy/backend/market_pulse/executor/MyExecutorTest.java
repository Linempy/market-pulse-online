package academy.backend.market_pulse.executor;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Небольшая демонстрация работы
 */
public class MyExecutorTest {
    static void main() throws InterruptedException {
        PoolWorkers poolWorkers = new PoolWorkers(4);
        Dispatcher dispatcher = new Dispatcher(poolWorkers);
        SubscriberJob job1 = new SubscriberJob("Job1",
                Duration.of(5, TimeUnit.SECONDS.toChronoUnit()),
                Duration.of(15, TimeUnit.SECONDS.toChronoUnit()),
                () -> System.out.println("Это я - Job1!")
        );

        dispatcher.add(job1);
        Thread.sleep(10000);
        dispatcher.cancel(job1);
    }
}