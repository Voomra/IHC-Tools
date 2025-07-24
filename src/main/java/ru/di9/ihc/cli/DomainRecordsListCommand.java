package ru.di9.ihc.cli;

import lombok.Getter;
import lombok.Setter;
import picocli.CommandLine;
import ru.di9.ihc.Domain;
import ru.di9.ihc.DomainRecord;
import ru.di9.ihc.IhcClient;

import java.util.List;
import java.util.concurrent.Callable;

@Getter
@Setter
@CommandLine.Command(
        name = "list",
        description = "Список записей доменов")
public class DomainRecordsListCommand implements Callable<Integer> {
    @CommandLine.Option(names = {"-h", "--help"}, description = "Страница помощи", usageHelp = true)
    private boolean flagHelp;

    @CommandLine.Option(names = {"-u", "--username"}, description = "Имя пользователя", required = true)
    private String username;

    @CommandLine.Option(names = {"-p", "--password"}, description = "Пароль пользователя", required = true, arity = "0..1", interactive = true)
    private String password;

    @CommandLine.Parameters(index = "0", description = "Имя домена")
    private String domainName;

    @Override
    public Integer call() throws Exception {
        IhcClient ihc = new IhcClient();
        if (!ihc.auth(username, password)) {
            System.err.println("Неверное имя пользователя или пароль");
            return -1;
        }

        Domain domain = ihc.getDomains().stream().filter(d -> d.name().equalsIgnoreCase(domainName)).findFirst()
                .orElseThrow(() -> new RuntimeException("DOMAIN '%s' NOT EXISTS".formatted(domainName)));
        List<DomainRecord> domainRecords = ihc.getDomainRecords(domain);

        var colId = new TableCol("ID");
        var colName = new TableCol("Запись");
        var colType = new TableCol("Тип");
        var colPriority = new TableCol("Приоритет");
        var colContent = new TableCol("Контент");
        var colReadOnly = new TableCol("Только для чтения");

        for (DomainRecord record : domainRecords) {
            colId.putRow(record.getId());
            colName.putRow(record.getName());
            colType.putRow(record.getType().name());
            colPriority.putRow(record.getPriority());
            colContent.putRow(record.getContent());
            colReadOnly.putRow(record.isReadOnly() ? "да" : "нет");
        }

        System.out.println(new Table(colId, colName, colType, colPriority, colContent, colReadOnly).print());
        return 0;
    }
}
