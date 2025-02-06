import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FieldWriter {
    int definitionLevel;
    int repetitionLevel;
    String fieldId;
    boolean atomic;
    Map<String, FieldWriter> fieldsMap = new HashMap<>();
    Class atomicType;

    // this builds the nested field writer.
    FieldWriter(Class schema, int definitionLevel) {
        this.atomic = false;
        this.fieldId = UUID.randomUUID().toString();
        this.definitionLevel = definitionLevel;
        Field[] fields = schema.getFields();
        for (Field field: fields) {
            String columnName = field.getName();
            Class type = field.getType();
            FieldWriter childFieldWriter;
            // primitive types
            if (type == int.class || type == char.class || type == String.class) {
                childFieldWriter = new FieldWriter(type, definitionLevel,
                        /*atomic=*/true);
            } else {
                // This is a nested type.
                // we will add support for repeated and optional types.
                childFieldWriter = new FieldWriter(type,
                        definitionLevel);
            }
            fieldsMap.put(columnName, childFieldWriter);
        }
    }

    FieldWriter(Class atomicType, int definitionLevel, boolean atomic) {
        this.definitionLevel = definitionLevel;
        this.fieldId = UUID.randomUUID().toString();
        this.atomicType = atomicType;
    }

    public void setRepetitionLevel(int repetitionLevel) {
        this.repetitionLevel = repetitionLevel;
    }

    FieldWriter getChildFieldWriter(String fieldName) {
        return fieldsMap.get(fieldName);
    }

    String fieldId() {
        return fieldId;
    }

    int depth() {
        return 0;
    }

    boolean isAtomic() {
        return atomic;
    }

    write(byte[] ) {

    }
}

