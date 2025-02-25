import java.lang.reflect.Field;
import java.util.*;

/*
FieldWriters form a tree hierarchy isomorphic to that of the input schema.

FieldWriters accumulate levels and propagate them lazily to lower-level
writers.

This is done as follows: each non-leaf writer keeps a sequence of
(repetition, definition) levels.

Each writer also has a ‘version’ number associated with it. Simply stated, a
writer version is incremented by one whenever a level is added.

It is sufficient for children to remember the last parent’s version they
synced. If a child writer ever gets its own(non-null) value, it synchronizes
its state with the parent by fetching new levels, and only then adds the new
data.

Because input data can have thousands of fields and millions records, it is
not feasible to store all levels in memory.

Some levels may be temporarily stored in a file on disk.

For a lossless encoding of empty (sub)records, non-atomic fields (such as
Name.Language in Figure 2) may need to have column stripes of their own,
containing only levels but no non-NULL values.
 */

public class FieldWriter {
    static class RepDefLevel {
        int repetitionLevel;
        int definitionLevel;
        RepDefLevel(int repetitionLevel, int definitionLevel) {
            this.repetitionLevel = repetitionLevel;
            this.definitionLevel = definitionLevel;
        }
    }

    List<RepDefLevel> repetitionDefinitionLevels = new ArrayList<>();
    String fieldId;
    boolean atomic;
    boolean optional;
    boolean repeated;
    Map<String, FieldWriter> childFieldWriters = new LinkedHashMap<>();
    Class type;
    Column column;
    String fieldName;
    String fullyQualifiedName;
    int definitionLevel;
    int depth; // number of optionals, repeateds in the path to this field.

    // this builds the nested field writer.
    // lets refactor this into one interface and 2 classes for atomic and nested
    // entities. but before that less understand how this lazy eval is
    // working as all the field writers are not created right at the start.
    FieldWriter(Class schema, String fieldName,
                String fullyQualifiedName, int definitionLevel) {
        this.atomic = false;
        this.fieldName = fieldName;
        this.fullyQualifiedName = fullyQualifiedName;
        this.fieldId = UUID.randomUUID().toString();
        this.type = schema;
        this.definitionLevel = definitionLevel;
        Field[] fields = schema.getFields();
        for (Field field: fields) {
            String childFieldName = field.getName();
            String childfullyQualifiedName =
                    Utils.constructFullyQualifiedChildFieldName(
                            fullyQualifiedName, childFieldName);
            Class childType = field.getType();
            FieldWriter childFieldWriter;
            // primitive types
            // DL tracks the nesting wrt repeated and optionals, not the
            // normal nesting.
            int childDefinitionLevel = definitionLevel;
            if (Utils.isTypeRepeated(childType) ||
                    Utils.isTypeOptional(childType)) {
                childDefinitionLevel++;
            }
            if (Utils.isTypeAtomic(childType) || Utils.isTypeOptional(childType)) {
                childFieldWriter = new FieldWriter(childFieldName,
                        childfullyQualifiedName,
                        childDefinitionLevel);
                // optionals currently are for atomics only.
                if (Utils.isTypeOptional(childType)) {
                    childFieldWriter.optional = true;
                }
                childFieldWriter.atomic = true;
                childFieldWriter.type = childType;
            } else if (Utils.isTypeRepeated(childType)) {
                childFieldWriter =
                        childFieldWriters.get(childFieldName);
                if (childFieldWriter == null) {
                    if (Utils.isTypeAtomic(childType.componentType())) {
                        childFieldWriter = new FieldWriter(childFieldName,
                                childfullyQualifiedName,
                                childDefinitionLevel);
                        childFieldWriter.atomic = true;
                    } else {
                        childFieldWriter = new FieldWriter(childType.componentType(),
                                childFieldName, childfullyQualifiedName,
                                childDefinitionLevel);
                    }
                    childFieldWriter.repeated = true;
                    childFieldWriter.type = childType.componentType();
                }
            } else {
                // This is a nested type.
                // we will add support for repeated and optional types.
                childFieldWriter = new FieldWriter(childType,
                        childFieldName, childfullyQualifiedName,
                        childDefinitionLevel);
            }
            childFieldWriters.put(childFieldName, childFieldWriter);
        }
    }

    FieldWriter(String fieldName, String fullyQualifiedName,
                int definitionLevel) {
        this.column = new Column(fullyQualifiedName);
        this.fieldName = fieldName;
        this.fullyQualifiedName = fullyQualifiedName;
        this.definitionLevel = definitionLevel;
        this.fieldId = UUID.randomUUID().toString();
    }

    public void addRepDefLevel(int repetitionLevel) {
        repetitionDefinitionLevels.add(new RepDefLevel(repetitionLevel,
                definitionLevel));
    }

    FieldWriter getChildFieldWriter(String fieldName) {
        return childFieldWriters.get(fieldName);
    }

    String fieldId() {
        return fieldId;
    }

    int depth() {
        return repetitionDefinitionLevels.getFirst().definitionLevel;
    }

    boolean isAtomic() {
        return atomic;
    }

    boolean isRepeated() {
        return repeated;
    }

    boolean isOptional() {
        return optional;
    }

    void write(Object fieldData) throws Exception {
        // do we remove this for repeated and optional.
        if (!isAtomic()) {
            throw new Exception("field " + fieldId + "is not atomic.");
        }
        // atomic field writers have a single rep and def level.
        RepDefLevel topRepDefLevel = repetitionDefinitionLevels.getLast();
        column.write(fieldData, topRepDefLevel.repetitionLevel,
                topRepDefLevel.definitionLevel);
    }

    void write(Object fieldData, int repetitionLevel) throws Exception {
        repetitionDefinitionLevels.add(new RepDefLevel(repetitionLevel,
                definitionLevel));
        column.write(fieldData, repetitionLevel, definitionLevel);
    }

    void flush() throws Exception {
        if (isAtomic()) {
            Properties properties = System.getProperties();
            ColumnSerializer.storeColumnContents(column,
                    properties.getProperty("user" +
                    ".dir"));
        }
        for (FieldWriter childFieldWriter : childFieldWriters.values()) {
            childFieldWriter.flush();
        }
    }
}

