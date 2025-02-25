import java.util.HashMap;
import java.util.Map;

public class FieldReader {
    Map<String, FieldReader> childFieldReaders = new HashMap<>();
    boolean atomic;
    boolean optional;
    boolean repeated;
    String fieldName;
    String fullyQualifiedName;
    static FieldReader rootFieldReader;
    int definitionLevel;
    int fullDefinitionLevel;
    Class fieldType;
    Column column;
    int columnValuePosition;
    RecordField recordField;

    String getFieldName() {
        return fieldName;
    }

    static void setRootFieldReader(FieldReader rootFieldReader) {
        FieldReader.rootFieldReader = rootFieldReader;
    }

    static FieldReader getRootFieldReader() {
        return rootFieldReader;
    }
    FieldReader(RecordField recordField, String fieldName,
                String fullyQualifiedName, Class fieldType,
                int definitionLevel, int fullDefinitionLevel, boolean atomic,
                boolean optional, boolean repeated) {
        this.recordField = recordField;
        this.atomic = atomic;
        this.optional = optional;
        this.repeated = repeated;
        this.fieldName = fieldName;
        this.fullyQualifiedName = fullyQualifiedName;
        this.fieldType = fieldType;
        this.definitionLevel = definitionLevel;
        this.fullDefinitionLevel = fullDefinitionLevel;
        if (atomic) {
            ColumnType columnType;
            if (optional) {
                columnType = ColumnType.OPTIONAL;
            } else if (repeated) {
                columnType = ColumnType.REPEATED;
            } else {
                columnType = ColumnType.NORMAL;
            }
            this.column = ColumnDeserializer.columnFrom(fieldType,
                    columnType, fullyQualifiedName);
        }
    }

    RecordField getRecordField() {
        return recordField;
    }

    boolean hasData() {
        return column.isValidPosition(columnValuePosition);
    }

    Object getDataAndMove() {
        Column.ColumnValue columnValue =
                column.getColumnValue(columnValuePosition);
        columnValuePosition++;
        // We return a null value if it is null for the record.
        // You could use definition level < max level as an indicator of null.
        return columnValue.value;
    }

    int getRepetitionLevel() {
        // Return 0 if the column value position is invalid.
        if (!column.isValidPosition(columnValuePosition)) {
            return 0;
        }
        Column.ColumnValue columnValue =
                column.getColumnValue(columnValuePosition);
        return columnValue.repetitionLevel;
    }

    int getNextRepetitionLevel() {
        // Return 0 if the column value position is invalid.
        if (!column.isValidPosition(columnValuePosition+1)) {
            return 0;
        }
        Column.ColumnValue columnValue =
                column.getColumnValue(columnValuePosition+1);
        return columnValue.repetitionLevel;
    }

    // its not clear whether these fields should in the field or in the field
    // reader. We can defer that thought for now.
    int treeLevel() {
        return definitionLevel;
    }

    FieldReader nextFieldReader(int repetitionLevel) {
        RecordField nextRecordField =
                recordField.stateTransition.get(repetitionLevel);
        return nextRecordField != null ? nextRecordField.fieldReader : null;
    }

    int getFullDefinitionLevel() {
        return fullDefinitionLevel;
    }

}
