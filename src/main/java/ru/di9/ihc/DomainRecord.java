package ru.di9.ihc;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class DomainRecord {
    private final int id;
    private final boolean readOnly;
    private final RecordType type;

    private String name;
    private String content;
    private Integer priority;

    public DomainRecord(int id, boolean readOnly, String name, RecordType type, String content, Integer priority) {
        this.id = id;
        this.readOnly = readOnly;
        this.name = name;
        this.type = type;
        this.content = content;
        this.priority = priority;
    }

    public void setName(String name) {
        if (readOnly) {
            throw new RuntimeException("READ ONLY RECORD");
        }
        this.name = name;
    }

    public void setContent(String content) {
        if (readOnly) {
            throw new RuntimeException("READ ONLY RECORD");
        }
        this.content = content;
    }

    public void setPriority(Integer priority) {
        if (readOnly) {
            throw new RuntimeException("READ ONLY RECORD");
        }
        if (!RecordType.MX.equals(type) && !RecordType.SRV.equals(type)) {
            throw new RuntimeException("NOT SUPPORT SET PRIORITY FOR " + type.name() + " RECORD");
        }
        this.priority = priority;
    }
}
