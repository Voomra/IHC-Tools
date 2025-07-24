package ru.di9.ihc.cli;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Table {
    private final List<TableCol> cols = new ArrayList<>();

    public Table(TableCol... cols) {
        this.cols.addAll(Arrays.asList(cols));
    }

    public String print() {
        Object[] headers = new Object[cols.size()];
        String formatRow;
        String strLine;
        int maxRows = 0;

        {
            StringBuilder sbFormatRow = new StringBuilder();
            StringBuilder sbStrLine = new StringBuilder();

            for (int i = 0; i < cols.size(); i++) {
                TableCol col = cols.get(i);

                headers[i] = col.getHeader();
                maxRows = Math.max(maxRows, col.getRows().size());

                sbFormatRow.append(" %%-%ds ".formatted(col.getSize()));
                sbStrLine.append("-".repeat(col.getSize() + 2));

                if (i < cols.size() - 1) {
                    sbFormatRow.append('|');
                    sbStrLine.append('|');
                }
            }

            formatRow = sbFormatRow.toString();
            strLine = sbStrLine.toString();
        }

        StringBuilder result = new StringBuilder();
        result.append(formatRow.formatted(headers)).append('\n')
                .append(strLine).append('\n');

        Object[] cells;
        for (int i = 0; i < maxRows; i++) {
            cells = new Object[headers.length];
            for (int j = 0; j < cols.size(); j++) {
                cells[j] = cols.get(j).getRows().get(i);
            }

            result.append(formatRow.formatted(cells));

            if (i < maxRows - 1) {
                result.append('\n');
            }
        }

        return result.toString();
    }
}
