package ru.di9.ihc.cli;

import lombok.Getter;
import lombok.Setter;
import picocli.CommandLine;
import ru.di9.ihc.Domain;
import ru.di9.ihc.DomainRecord;
import ru.di9.ihc.IhcClient;

import java.util.concurrent.Callable;

@Getter
@Setter
@CommandLine.Command(
        name = "delete",
        description = "Удалить запись домена")
public class DeleteDomainRecordCommand implements Callable<Integer> {
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

        ihc.removeDomainRecord(domain, record.getId());
        return 0;
    }
}
