import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class RecordDecoder {
    Map<String, RecordDecoder> nestedRecordDecoders = new HashMap<>();
    Class type;
    ByteBuffer record;
    Field[] fields;
    int decodedFieldIndex;
    boolean decodingRepeated;
    int repeatedIndex;
    int repeatedCount;
    Class repeatedType;
    static class DecodedField {
        String fieldName;
        Object fieldValue;
        DecodedField(String fieldName, Object fieldValue) {
            this.fieldName = fieldName;
            this.fieldValue = fieldValue;
        }
    }

    RecordDecoder(Class schema) {
        this.type = schema;
        this.fields = schema.getFields();
        for (Field field: schema.getFields()) {
            Class childType = field.getType();
            if (!isNestedType(childType)) {
                continue;
            }
            Class nestedType = getNestedType(childType);
            nestedRecordDecoders.put(field.getName(),
                    new RecordDecoder(nestedType));
        }
    }

    Class getNestedType(Class type) {
        if (Utils.isTypeNested((type))) {
            return type;
        } else {
            return type.componentType();
        }
    }

    boolean isNestedType(Class type) {
        return Utils.isTypeNested(type) ||
                (Utils.isTypeRepeated(type) &&
                        Utils.isTypeNested(type.componentType()));
    }

    void print() {
        System.out.println("Printing the nested fields");
        for (String field: nestedRecordDecoders.keySet()) {
            System.out.println(field);
        }
    }


    RecordDecoder getRecordDecoder(String fieldName) {
        return nestedRecordDecoders.get(fieldName);
    }

    void initialize(ByteBuffer record) {
        this.record = record;
        decodedFieldIndex = 0;
        decodingRepeated = false;
    }

    boolean hasData() {
        return decodedFieldIndex < fields.length;
    }

    DecodedField getDataRepeated(Class fieldType, String fieldName)
            throws Exception {
        if (!decodingRepeated) {
            decodingRepeated = true;
            repeatedType = fieldType.getComponentType();
            repeatedCount = readRepeatedCount();
            repeatedIndex = 0;
        }
        // Should we have a null count for repeated?
        DecodedField decodedField = getDataSingle(repeatedType, fieldName);
        repeatedIndex++;
        if (repeatedIndex == repeatedCount) {
            decodingRepeated = false;
            decodedFieldIndex++;
        }
        return decodedField;
    }

    Class readOptionalType(Class fieldType) throws Exception {
        ValueType valueType = ValueType.of(record.get());
        switch (valueType) {
            case ValueType.INT:
                return int.class;
            case ValueType.CHAR:
                return char.class;
            case ValueType.STRING:
                return String.class;
        }
        throw new Exception(valueType + " is an unsupported atomic type.");
    }

    boolean isOptionalPresent() {
        return record.get() != 0;
    }

    DecodedField getDataOptional(Class fieldType, String fieldName)
            throws Exception {
        if (!isOptionalPresent()) {
            return new DecodedField(fieldName, null);
        }
        Class optionalType = readOptionalType(fieldType);
        return getDataSingle(optionalType, fieldName);
    }

    DecodedField getDataSingle(Class fieldType, String fieldName) throws Exception {
        if (Utils.isTypeNested(fieldType)) {
            return new DecodedField(fieldName, null);
        }
        Object fieldValue = readAtomicField(fieldType);
        return new DecodedField(fieldName, fieldValue);
    }

    DecodedField getData() throws Exception {
        Field field = fields[decodedFieldIndex];
        Class fieldType = field.getType();
        String fieldName = field.getName();

        if (Utils.isTypeRepeated(fieldType)) {
            return getDataRepeated(fieldType, fieldName);
        }
        // Increment the field information.
        decodedFieldIndex++;
        if (Utils.isTypeOptional(fieldType)) {
            return getDataOptional(fieldType, fieldName);
        } else {
            return getDataSingle(fieldType, fieldName);
        }
    }

    Object readAtomicField(Class type) throws Exception {
        if (type == int.class) {
            return record.getInt();
        } else if (type == char.class) {
            return record.get();
        } else if (type == String.class) {
            byte count = record.get();
            byte [] data = new byte[count];
            record.get(data);
            return new String(data);
        }
        throw new Exception(type + " is an unsupported atomic type.");
    }

    Object readOptionalField() throws Exception {
        ValueType valueType = ValueType.of(record.get());
        switch (valueType) {
            case ValueType.INT:
                return record.getInt();
            case ValueType.CHAR:
                return record.get();
            case ValueType.STRING:
                byte count = record.get();
                byte [] data = new byte[count];
                record.get(data);
                return new String(data);
        }
        throw new Exception(valueType + " is an unsupported atomic type.");
    }

    byte readRepeatedCount() {
        return record.get();
    }
}
