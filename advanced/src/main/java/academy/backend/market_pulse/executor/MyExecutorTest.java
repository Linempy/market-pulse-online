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

        SubscriberJob job2 = new SubscriberJob("Job2",
                Duration.of(2, TimeUnit.SECONDS.toChronoUnit()),
                Duration.of(15, TimeUnit.SECONDS.toChronoUnit()),
                () -> System.out.println("А это я - Job2!")
        );

        dispatcher.add(job1);
        Thread.sleep(5000);
        dispatcher.add(job2);
        Thread.sleep(5000);
        dispatcher.cancel(job1);
        Thread.sleep(5000);
        dispatcher.close();
    }
}