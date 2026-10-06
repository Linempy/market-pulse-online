package academy.backend.market_pulse.cli;

import academy.backend.market_pulse.exception.InstrumentSaveException;
import academy.backend.market_pulse.model.Instrument;
import academy.backend.market_pulse.repository.InstrumentRepository;
import io.vavr.control.Try;
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

    @Override
    public Integer call() {

        int countSaved = Try.withResources(() -> new BufferedWriter(new FileWriter(path)))
                .of(this::writeAll)
                .getOrElseThrow(
                        cause -> new InstrumentSaveException("Ошибка при записи в файл: %s".formatted(path), cause)
                );
        System.out.printf("Инструменты были сохранены: %d шт", countSaved);
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
