package ru.di9.ihc.cli;

import lombok.Getter;
import lombok.Setter;
import picocli.CommandLine;
import ru.di9.ihc.Domain;
import ru.di9.ihc.IhcClient;
import ru.di9.ihc.RecordType;

import java.util.concurrent.Callable;

@Getter
@Setter
@CommandLine.Command(
        name = "add",
        description = "Добавить запись домена")
public class AddDomainRecordCommand implements Callable<Integer> {
    @CommandLine.Option(names = {"-h", "--help"}, description = "Страница помощи", usageHelp = true)
    private boolean flagHelp;

    @CommandLine.Option(names = {"-u", "--username"}, description = "Имя пользователя", required = true)
    private String username;

    @CommandLine.Option(names = {"-p", "--password"}, description = "Пароль пользователя", required = true, arity = "0..1", interactive = true)
    private String password;

    @CommandLine.Parameters(index = "0", description = "Имя домена")
    private String domainName;

    @CommandLine.Option(names = {"--name"}, description = "Имя записи")
    private String recordName;

    @CommandLine.Option(names = {"--type"}, description = "Тип записи", converter = CliRecordTypeConverter.class, required = true)
    private RecordType recordType;

    @CommandLine.Option(names = {"--content"}, description = "Контент", required = true)
    private String recordContent;

    @CommandLine.Option(names = {"--priority"}, description = "Приоритет")
    private Integer recordPriority;

    @Override
    public Integer call() throws Exception {
        IhcClient ihc = new IhcClient();
        if (!ihc.auth(username, password)) {
            System.err.println("Неверное имя пользователя или пароль");
            return -1;
        }

        Domain domain = ihc.getDomains().stream().filter(d -> d.name().equalsIgnoreCase(domainName)).findFirst()
                .orElseThrow(() -> new RuntimeException("DOMAIN '%s' NOT EXISTS".formatted(domainName)));

        ihc.addDomainRecord(domain, recordName, recordType, recordContent, recordPriority);
        return 0;
    }
}
