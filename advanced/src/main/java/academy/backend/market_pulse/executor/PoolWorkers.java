package academy.backend.market_pulse.executor;

import lombok.Getter;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionException;

/**
 * Пул рабочих потоков, выполняющих задачи из очереди
 */
public class PoolWorkers {
    private final int numberWorkers;
    private final Deque<Thread> pool = new ArrayDeque<>();
    private final BlockingDeque<Runnable> queueTasks = new LinkedBlockingDeque<>();
    @Getter
    private volatile boolean stopped;


    public PoolWorkers(int numberWorkers) {
        if (numberWorkers <= 0) {
            throw new IllegalArgumentException("N должны быть больше 0");
        }

        for (int i = 0; i < numberWorkers; i++) {
            Thread worker = new Thread(() -> {
                while (!stopped && !Thread.currentThread().isInterrupted()) {
                    try {
                        Runnable job = queueTasks.take();

                        try {
                            job.run();
                        } catch (RuntimeException e) {
                            System.err.println("Ошибка задачи: " + e.getMessage());
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
            );
            pool.add(worker);
            worker.start();
        }
        this.numberWorkers = numberWorkers;
    }

    public synchronized void submit(Runnable job) throws InterruptedException {
        if (!stopped) {
            queueTasks.put(job);
        } else {
            System.out.println("Задача отклонена, пул завершает работу");
            throw new RejectedExecutionException();
        }
    }

    public void close() throws InterruptedException {
        synchronized (this) {
            stopped = true;
            queueTasks.clear();
        }

        for (Thread worker : pool) {
            worker.interrupt();
        }

        for (Thread worker : pool) {
            worker.join();
        }
    }

}
