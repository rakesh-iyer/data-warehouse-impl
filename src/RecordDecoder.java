import java.lang.reflect.Field;
import java.util.*;

public class RecordDecoder {
    Map<String, RecordDecoder> fieldsMap = new HashMap<>();
    Class atomicType;

    RecordDecoder(Class atomicType, boolean atomic) {
        this.atomicType = atomicType;
    }

    boolean isPrimitiveType(Class type) {
        return type == int.class || type == char.class || type == String.class;
    }

    RecordDecoder(Class schema) {
        Field[] fields = schema.getFields();
        for (Field field: fields) {
            String columnName = field.getName();
            Class type = field.getType();
            RecordDecoder recordDecoder;
            // primitive types
            if (isPrimitiveType(type)) {
                recordDecoder = new RecordDecoder(type, /*atomic=*/true);
            } else {
                // This is a nested type.
                // we will add support for repeated and optional types.
                recordDecoder = new RecordDecoder(type);
            }
            fieldsMap.put(columnName, recordDecoder);
        }
    }

    List<String> getFields() {
        return new ArrayList<>(fieldsMap.keySet());
    }

    RecordDecoder getRecordDecoder(String fieldName) {
        return fieldsMap.get(fieldName);
    }

    Object readField(byte [] record, String fieldName) {

        //       return ;
    }
}