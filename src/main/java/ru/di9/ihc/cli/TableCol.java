package ru.di9.ihc.cli;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TableCol {
    private final List<String> rows = new ArrayList<>();

    @Getter
    private final String header;

    @Getter
    private int size;

    public TableCol(String header) {
        this.header = header;
        this.size = header.length();
    }

    public void putRow(Integer row) {
        if (row == null) putRow((String) null);
        else putRow(String.valueOf(row));
    }

    public void putRow(String row) {
        if (row == null) {
            rows.add("");
            return;
        }

        rows.add(row);
        size = Math.max(size, row.length());
    }

    public List<String> getRows() {
        return Collections.unmodifiableList(rows);
    }
}
