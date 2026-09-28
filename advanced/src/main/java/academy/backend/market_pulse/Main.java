package academy.backend.market_pulse;

import academy.backend.market_pulse.cli.AddCommand;
import academy.backend.market_pulse.cli.ListCommand;
import academy.backend.market_pulse.cli.MarketPulseCli;
import academy.backend.market_pulse.repository.InMemoryInstrumentRepository;
import academy.backend.market_pulse.repository.InstrumentRepository;
import picocli.CommandLine;

public class Main {

    public static void main(String[] args) {
        InstrumentRepository repository = new InMemoryInstrumentRepository();
        // NOTICE: подкоманды регистрируются вручную через addSubcommand — у них нет
        //  конструктора без аргументов, каждой нужен уже готовый repository.
        CommandLine cli = new CommandLine(new MarketPulseCli())
                .addSubcommand(new AddCommand(repository))
                .addSubcommand(new ListCommand(repository));
        int exitCode = cli.execute(args);
        System.exit(exitCode);
    }
}
