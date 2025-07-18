package ru.di9.ihc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DomainRecordsResponse(Data data) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Data(List<Record> records) {
    }

    public record Record(
            int id,
            boolean readOnly,
            String name,
            String type,
            String content,
            Integer prio
    ) {
    }
}
