package ru.di9.ihc.cli;

import lombok.Setter;
import lombok.ToString;
import picocli.CommandLine;

@Setter
@ToString
@CommandLine.Command(
        name = "app",
        description = "IHC DNS Tools",
        subcommands = {
                DomainsListCommand.class,
                DomainRecordCommand.class
        },
        version = "2.0")
public class CliApp {
    @CommandLine.Option(names = {"-h", "--help"}, description = "Страница помощи", usageHelp = true)
    private boolean flagHelp;

    @CommandLine.Option(names = {"-v", "--version"}, description = "Версия программы", versionHelp = true)
    private boolean flagVersion;

    public static void main(String[] args) {
        CliApp cliApp = new CliApp();
        CommandLine commandLine = new CommandLine(cliApp);
        commandLine.parseArgs(args);

        if (cliApp.flagHelp) {
            CommandLine.usage(cliApp, System.out);
            return;
        } else if (cliApp.flagVersion) {
            commandLine.printVersionHelp(System.out);
            return;
        }

        System.exit(commandLine.execute(args));
    }
}
