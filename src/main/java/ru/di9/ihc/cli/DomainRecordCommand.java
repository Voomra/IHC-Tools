package ru.di9.ihc.cli;

import lombok.Getter;
import lombok.Setter;
import picocli.CommandLine;

@Getter
@Setter
@CommandLine.Command(
        name = "domain-record",
        description = "Работа с записями домена",
        subcommands = {
                DomainRecordsListCommand.class,
                AddDomainRecordCommand.class,
                EditDomainRecordCommand.class,
                DeleteDomainRecordCommand.class
        })
public class DomainRecordCommand {
    @CommandLine.Option(names = {"-h", "--help"}, description = "Страница помощи", usageHelp = true)
    private boolean flagHelp;
}
