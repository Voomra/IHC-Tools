package ru.di9.ihc.cli;

import lombok.Getter;
import lombok.Setter;
import picocli.CommandLine;
import ru.di9.ihc.Domain;
import ru.di9.ihc.IhcClient;

import java.util.List;
import java.util.concurrent.Callable;

@Getter
@Setter
@CommandLine.Command(
        name = "domains",
        description = "Список доменов")
public class DomainsListCommand implements Callable<Integer> {
    @CommandLine.Option(names = {"-h", "--help"}, description = "Страница помощи", usageHelp = true)
    private boolean flagHelp;

    @CommandLine.Option(names = {"-u", "--username"}, description = "Имя пользователя", required = true)
    private String username;

    @CommandLine.Option(names = {"-p", "--password"}, description = "Пароль пользователя", required = true, arity = "0..1", interactive = true)
    private String password;

    @Override
    public Integer call() {
        IhcClient ihc = new IhcClient();
        if (!ihc.auth(username, password)) {
            System.err.println("Неверное имя пользователя или пароль");
            return -1;
        };

        List<Domain> domains = ihc.getDomains();

        var colId = new TableCol("ID");
        var colDomain = new TableCol("Домен");
        var colPunycode = new TableCol("Punycode");

        for (Domain domain : domains) {
            colId.putRow(domain.id());
            colDomain.putRow(domain.name());
            colPunycode.putRow(domain.punycode());
        }

        System.out.println(new Table(colId, colDomain, colPunycode).print());
        return 0;
    }
}
