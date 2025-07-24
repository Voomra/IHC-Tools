package ru.di9.ihc.cli;

import lombok.Getter;
import lombok.Setter;
import picocli.CommandLine;
import ru.di9.ihc.Domain;
import ru.di9.ihc.DomainRecord;
import ru.di9.ihc.IhcClient;
import ru.di9.ihc.RecordType;

import java.util.concurrent.Callable;

@Getter
@Setter
@CommandLine.Command(
        name = "edit",
        description = "Изменить запись домена")
public class EditDomainRecordCommand implements Callable<Integer> {
    @CommandLine.Option(names = {"-h", "--help"}, description = "Страница помощи", usageHelp = true)
    private boolean flagHelp;

    @CommandLine.Option(names = {"-u", "--username"}, description = "Имя пользователя", required = true)
    private String username;

    @CommandLine.Option(names = {"-p", "--password"}, description = "Пароль пользователя", required = true, arity = "0..1", interactive = true)
    private String password;

    @CommandLine.Parameters(index = "0", description = "Имя домена")
    private String domainName;

    @CommandLine.Parameters(index = "1", description = "Имя записи")
    private String recordName;

    @CommandLine.Option(names = {"--name"}, description = "Новое имя записи")
    private String recordNewName;

    @CommandLine.Option(names = {"--content"}, description = "Контент")
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

        DomainRecord record = ihc.getDomainRecords(domain).stream().filter(r -> r.getName().equals(recordName)).findFirst()
                .orElseThrow(() -> new RuntimeException("RECORD '%s' FOR DOMAIN '%s' NOT EXISTS".formatted(recordName, domainName)));

        if (recordNewName != null) record.setName(recordNewName);
        if (recordContent != null) record.setContent(recordContent);
        if (recordPriority != null) record.setPriority(recordPriority);

        ihc.updateDomainRecord(domain, record);
        return 0;
    }
}
