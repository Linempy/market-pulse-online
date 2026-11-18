package academy.backend.market_pulse.executor;

import lombok.Getter;
import lombok.Setter;

import java.time.Duration;

/**
 * Подписка на периодическое выполнение задачи с ограниченным сроком действия.
 *
 * @author Linempy
 * @since 18.11.2026
 */
@Getter
public class SubscriberJob {
    private final String name;
    private final Duration interval;
    private final Duration lifetime;
    private final long expiresAt;
    private final Runnable action;
    @Setter
    private long nextRun;
    @Setter
    private JobState state;
    private boolean cancelled;

    void cancel() {
        cancelled = true;
    }

    public SubscriberJob(String name, Duration interval, Duration lifetime, Runnable action) {
        this.name = name;
        this.action = action;

        checkDuration(interval, lifetime);
        this.interval = interval;
        this.lifetime = lifetime;

        long now = System.nanoTime();
        this.nextRun = now;
        this.expiresAt = now + lifetime.toNanos();
        state = JobState.WAITING;
    }

    private void checkDuration(Duration interval, Duration lifetime) {
        if (!(interval.isPositive() && lifetime.isPositive())) {
            throw new IllegalArgumentException("Интервал и срок действия должны быть положительными");
        }
    }
}
