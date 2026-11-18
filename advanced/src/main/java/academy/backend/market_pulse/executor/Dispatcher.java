package academy.backend.market_pulse.executor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import static academy.backend.market_pulse.executor.JobState.WAITING;
import static academy.backend.market_pulse.executor.JobState.QUEUED;

/**
 * Планирует выполнение подписок и передаёт задачи пулу рабочих потоков
 */
public class Dispatcher implements AutoCloseable {
    private final PoolWorkers workers;
    private final List<SubscriberJob> subscribers;
    private final Object lock = new Object();
    private final Thread dispatcher;
    private boolean closed;

    public Dispatcher(PoolWorkers workers) {
        this.workers = workers;
        this.subscribers = new ArrayList<>();

        this.dispatcher = new Thread(this::dispatch, "dispatcher");
        dispatcher.start();
    }

    public void add(SubscriberJob subscriberJob) {
        synchronized (lock) {
            if (closed) {
                throw new RejectedExecutionException("Диспетчер закрыт");
            }
            if (subscriberJob.isCancelled()) {
                throw new IllegalArgumentException("Подписка уже отменена");
            }
            subscribers.add(subscriberJob);
            lock.notifyAll();
        }
    }

    public boolean cancel(SubscriberJob subscriberJob) {
        synchronized (lock) {
            if (!subscribers.remove(subscriberJob)) {
                return false;
            }
            subscriberJob.cancel();
            lock.notifyAll();
            return true;
        }
    }

    @Override
    public void close() throws InterruptedException {
        synchronized (lock) {
            closed = true;
            for (SubscriberJob job : subscribers) {
                job.cancel();
            }
            subscribers.clear();
            dispatcher.interrupt();
            lock.notifyAll();
        }

        try {
            dispatcher.join();
        } finally {
            workers.close();
        }
    }

    private void dispatch() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                process();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void process() throws InterruptedException {
        synchronized (lock) {
            if (closed) {
                return;
            }
            long now = System.nanoTime();
            var iterator = subscribers.iterator();

            while (iterator.hasNext()) {
                SubscriberJob job = iterator.next();

                if (isCancelledOrExpired(job, now)) {
                    iterator.remove();
                    continue;
                }

                if (job.getState() == WAITING && now - job.getNextRun() >= 0) {
                    submitSubscription(job);
                }
            }
        }
    }

    private void submitSubscription(SubscriberJob job) throws InterruptedException {
        job.setState(QUEUED);
        try {
            workers.execute(() -> executeSubscription(job));
        } catch (InterruptedException | RejectedExecutionException e) {
            job.setState(WAITING);
            throw e;
        }
    }

    private void executeSubscription(SubscriberJob job) {
        synchronized (lock) {
            if (closed || isCancelledOrExpired(job, System.nanoTime())) {
                job.setState(WAITING);
                lock.notifyAll();
                return;
            }
            job.setState(JobState.RUNNING);
        }

        try {
            job.getAction().run();
        } finally {
            synchronized (lock) {
                job.setNextRun(System.nanoTime() + job.getInterval().toNanos());
                job.setState(WAITING);
                lock.notifyAll();
            }
        }
    }

    private static boolean isCancelledOrExpired(SubscriberJob job, long now) {
        return job.isCancelled() || now - job.getExpiresAt() >= 0;
    }
}
