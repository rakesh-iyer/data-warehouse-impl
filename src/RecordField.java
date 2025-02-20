import java.lang.reflect.Field;
import java.util.*;

public class RecordField {
    Map<Integer, RecordField> stateTransition = new HashMap<>();
    int maxRepetitionLevel;
    int repetitionLevel;
    // We use a linked hash map to preserve insertion ordering.
    // This is good enough as we will insert sub-fields in order of schema
    // parsing.
    Map<String, RecordField> subRecordFieldsMap = new LinkedHashMap<>();
    static RecordField rootRecordField;
    String fieldName;
    String fullyQualifiedFieldName;
    boolean atomic;
    boolean optional;
    boolean repeated;
    FieldReader fieldReader;
    Class fieldType;
    static List<RecordField> leafFields = new ArrayList<>();
    static List<FieldReader> leafFieldReaders = new ArrayList<>();

    RecordField(String fieldName, String fullyQualifiedFieldName,
                Class fieldType, int maxRepetitionLevel, int definitionLevel,
                int fullDefinitionLevel, boolean atomic, boolean optional,
                boolean repeated) {
        this.fieldName = fieldName;
        this.fullyQualifiedFieldName = fullyQualifiedFieldName;
        this.maxRepetitionLevel = maxRepetitionLevel;
        this.atomic = atomic;
        this.optional = optional;
        this.repeated = repeated;
        this.fieldType = fieldType;
        this.fieldReader = new FieldReader(this, fieldName,
                fullyQualifiedFieldName, fieldType, definitionLevel,
                fullDefinitionLevel, /*atomic=*/true, optional, repeated);
    }

    // Record fields will exist for each field nested or atomic.
    // Field Readers otoh will only exist for atomic fields.
    RecordField(Class schema, String fieldName, String fullyQualifiedFieldName,
                int maxRepetitionLevel, int definitionLevel,
                int fullDefinitionLevel, boolean isRepeated) {
        Field[] fields = schema.getFields();
        this.fullyQualifiedFieldName = fullyQualifiedFieldName;
        this.fieldName = fieldName;
        this.maxRepetitionLevel = maxRepetitionLevel;
        // Add field reader for nested records as well.
        this.fieldReader = new FieldReader(this, fieldName,
                fullyQualifiedFieldName, fieldType, definitionLevel,
                fullDefinitionLevel,/*atomic=*/false, /*optional=*/false,
                isRepeated);
        int childFullDefinitionLevel = fullDefinitionLevel+1;
        for (Field field: fields) {
            Class fieldType = field.getType();
            String childFieldName = field.getName();
            String childFullyQualifiedFieldName = String.format("%s-%s",
                    fullyQualifiedFieldName, childFieldName);
            RecordField childRecordField;
            if (Utils.isTypeAtomic(fieldType)) {
                childRecordField = new RecordField(childFieldName,
                        childFullyQualifiedFieldName, fieldType,
                        maxRepetitionLevel, definitionLevel,
                        childFullDefinitionLevel,/*atomic=*/true, /*optional
                        =*/false, /*repeated=*/false);
                leafFields.add(childRecordField);
                leafFieldReaders.add(childRecordField.getFieldReader());
            } else if (Utils.isTypeOptional(fieldType)) {
                // We only support optional integers at this moment
                childRecordField = new RecordField(childFieldName,
                        childFullyQualifiedFieldName, int.class,
                        maxRepetitionLevel, definitionLevel+1,
                        childFullDefinitionLevel,/*atomic=*/true,
                        /*optional=*/true, /*repeated=*/false);
                leafFields.add(childRecordField);
                leafFieldReaders.add(childRecordField.getFieldReader());
            } else if (Utils.isTypeRepeated(fieldType)) {
                // We increment the repetition level.
                if (Utils.isTypeAtomic(fieldType.componentType())) {
                    childRecordField = new RecordField(childFieldName,
                            childFullyQualifiedFieldName, fieldType.componentType(),
                            maxRepetitionLevel+1, definitionLevel+1,
                            childFullDefinitionLevel, /*atomic=*/true,
                            /*optional=*/false, /*repeated=*/true);
                    leafFields.add(childRecordField);
                    leafFieldReaders.add(childRecordField.getFieldReader());
                } else {
                    // A repeated nested field.
                    childRecordField =
                            new RecordField(fieldType.componentType(),
                            childFieldName,
                            childFullyQualifiedFieldName, maxRepetitionLevel+1,
                            definitionLevel, childFullDefinitionLevel,
                                    /*repeated=*/true);
                }
            } else {
                // this is a simple nested field.
                childRecordField = new RecordField(fieldType, childFieldName,
                        childFullyQualifiedFieldName, maxRepetitionLevel,
                        definitionLevel, childFullDefinitionLevel, /*repeated
                        =*/false);
            }
            subRecordFieldsMap.put(childFieldName, childRecordField);
        }
    }

    String getFieldName() {
        return fieldName;
    }

    // This should be setup by the main routines.
    static void setRootRecordField(RecordField rootRecordField) {
        RecordField.rootRecordField = rootRecordField;
    }

    static RecordField getRootRecordField() {
        return rootRecordField;
    }

    int getMaxRepetitionLevel() {
        return maxRepetitionLevel;
    }

    void setTransition(int level, RecordField destinationRecordField) {
        stateTransition.put(level, destinationRecordField);
    }

    RecordField getTransition(int level) {
        return stateTransition.get(level);
    }

    static class MatchResult {
        boolean isPresent;
        RecordField lcaRecordField;

        MatchResult(boolean isPresent, RecordField lcaRecordField) {
            this.isPresent = isPresent;
            this.lcaRecordField = lcaRecordField;
        }
    }

    MatchResult lowestCommonAncestor(RecordField field1, RecordField field2) {
        if (field1 == this || field2 == this) {
            return new MatchResult(true, this);
        }
        int countPresent = 0;
        RecordField lcaRecordField = null;
        for (RecordField subRecordField: subRecordFieldsMap.values()) {
            MatchResult subRecordFieldMatchResult =
                    subRecordField.lowestCommonAncestor(field1,
                    field2);
            if (subRecordFieldMatchResult.isPresent) {
                countPresent++;
            }
            if (subRecordFieldMatchResult.lcaRecordField != null) {
                lcaRecordField = subRecordFieldMatchResult.lcaRecordField;
            }
        }

        if (countPresent == 2) {
            lcaRecordField = this;
        }

        return new MatchResult(countPresent != 0, lcaRecordField);
    }

    List<String> findPath(RecordField destinationField) {
        // we found the path to the destination field.
        if (destinationField.equals(this)) {
            return new ArrayList<>();
        }
        for (RecordField subRecordField: subRecordFieldsMap.values()) {
            List<String> path =
                    subRecordField.findPath(destinationField);
            if (path == null) {
                continue;
            }
            path.add(fieldName);
            return path;
        }
        // There is no path to the destination record field from 'this'.
        return null;
    }

    List<FieldReader> getLeafFieldReaders() {
        return leafFieldReaders;
    }

    FieldReader getFieldReader() {
        return fieldReader;
    }

    List<RecordField> getLeafFields() {
        return leafFields;
    }
}
