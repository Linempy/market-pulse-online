package academy.backend.market_pulse.executor;

/**
 * Состояние запроса подписки: ожидание запуска, нахождение в очереди и выполнение задачи
 *
 * @author Linempy
 * @since 18.11.2026
 */
public enum JobState {
    WAITING, QUEUED, RUNNING
}
