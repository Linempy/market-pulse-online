package academy.backend.market_pulse.executor;

/**
 * Состояние запроса подписки: ожидание запуска, нахождение в очереди и выполнение задачи
 */
public enum JobState {
    WAITING, QUEUED, RUNNING
}
