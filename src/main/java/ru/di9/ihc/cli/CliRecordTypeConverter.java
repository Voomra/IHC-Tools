package ru.di9.ihc.cli;

import picocli.CommandLine;
import ru.di9.ihc.RecordType;

public class CliRecordTypeConverter implements CommandLine.ITypeConverter<RecordType> {
    @Override
    public RecordType convert(String s) {
        return RecordType.valueOf(s.toUpperCase());
    }
}
