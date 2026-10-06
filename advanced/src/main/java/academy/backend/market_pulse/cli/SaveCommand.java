package academy.backend.market_pulse.cli;

import academy.backend.market_pulse.exception.InstrumentSaveException;
import academy.backend.market_pulse.repository.InstrumentRepository;
import lombok.RequiredArgsConstructor;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * Сохраняет текущий список инструментов в файл по указанному пользователем пути — первый в
 * проекте настоящий {@link AutoCloseable} ресурс (файл), а не игрушечная демонстрация.
 */
@Command(name = "save", description = "Сохранение инструментов из репозитория в файл")
@RequiredArgsConstructor
public class SaveCommand implements Callable<Integer> {

    @Parameters(index = "0", description = "Путь к файлу для сохранения")
    private String path;

    private final InstrumentRepository repository;

    // TODO №4: реализовать с помощью Vavr.Try
    @Override
    public Integer call() {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            repository.forEach(instrument -> {
                try {
                    writer.write(instrument.toString());
                    writer.newLine();
                } catch (IOException e) {
                    throw new InstrumentSaveException("Ошибка при записи в файл: %s".formatted(path), e);
                }
            });
        } catch (IOException e) {
            throw new InstrumentSaveException("Ошибка при записи в файл: %s".formatted(path), e);
        }
        System.out.println("Инструменты были сохранены");
        return 0;
    }
}
