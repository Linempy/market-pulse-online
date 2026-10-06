package academy.backend.market_pulse.cli;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.Callable;

import academy.backend.market_pulse.model.Instrument;
import academy.backend.market_pulse.repository.InstrumentRepository;
import lombok.RequiredArgsConstructor;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

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

    // TODO №1: реализовать сохранение через try-with-resources
    // TODO №2: показать боль от связки FI <-> throws
    // TODO №3: обернуть IOException в InstrumentSaveException, а не отдавать его как есть
    // TODO №4: реализовать с помощью Vavr.Try
    @Override
    public Integer call() throws IOException {
        int saved = 0;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            saved = writeAll(writer);
        }
        System.out.printf("Инструменты сохранены: %d шт%n", saved);
        return 0;
    }

    private int writeAll(BufferedWriter writer) throws IOException {
        int count = 0;
        for (Instrument instrument : repository) {
            writer.write(instrument.toString());
            writer.newLine();
            count++;
        }
        return count;
    }
}
