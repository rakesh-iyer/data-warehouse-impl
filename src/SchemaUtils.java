import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SchemaUtils
{
    // Map.entry is a helper to create a Map.Entry from key and value.
    static Map<Class, Type> classTypeMap = Map.ofEntries(
            Map.entry(char.class, Type.FixedLengthByteArray),
            Map.entry(int.class, Type.Int32),
            Map.entry(long.class, Type.Int64),
            Map.entry(boolean.class, Type.Boolean),
            Map.entry(float.class, Type.Float),
            Map.entry(double.class, Type.Double),
            Map.entry(String.class, Type.ByteArray));
    static Map<Type, String> typeClassNameMap = Map.ofEntries(
            Map.entry(Type.ByteArray, "String"),
            Map.entry(Type.FixedLengthByteArray, "char"),
            Map.entry(Type.Int32, "int"),
            Map.entry(Type.Int64, "long"),
            Map.entry(Type.Boolean, "boolean"),
            Map.entry(Type.Float, "float"),
            Map.entry(Type.Double, "double"));

    static List<SchemaElement> buildSchema(Class type) {
        return buildNestedSchema(type);
    }

    static SchemaElement createAtomicSchemaElement(Class type,
                                                   FieldRepetitionType fieldRepetitionType,
                                                   String fieldName) {
        Type elementType = classTypeMap.get(type);
        return new SchemaElement(elementType, 1, fieldRepetitionType,
                fieldName, /*numChilds=*/0, /*convertedType=*/null);
    }

    static List<SchemaElement> buildNestedSchema(Class type) {
        List<SchemaElement> schema = new ArrayList<>();
        // need to construct schema from given type.
        // Elements are added to the list in DFS order.
        Field[] fields = type.getFields();
        for (Field field: fields) {
            Class subType;
            String fieldName = field.getName();
            SchemaElement schemaElement;
            FieldRepetitionType fieldRepetitionType;
            if (Utils.isTypeRepeated(field.getType())) {
                subType = field.getType().componentType();
                fieldRepetitionType = FieldRepetitionType.Repeated;
            } else if (Utils.isTypeOptional(field.getType())) {
                // We only support Integer optionals.
                subType = int.class;
                fieldRepetitionType = FieldRepetitionType.Required;
            } else {
                subType = field.getType();
                fieldRepetitionType = FieldRepetitionType.Required;
            }
            if (Utils.isTypeAtomic(subType)) {
                schemaElement = createAtomicSchemaElement(subType,
                        fieldRepetitionType, fieldName);
                schema.add(schemaElement);
            } else {
                schemaElement = new SchemaElement(/*type=*/null, 1,
                        fieldRepetitionType, fieldName, /*numChilds
                        =*/subType.getFields().length, /*convertedType=*/null);
                schema.add(schemaElement);
                schema.addAll(buildNestedSchema(subType));
            }
        }
        return schema;
    }

    Class deserialize(List<SchemaElement> schema) {
        StringBuilder classDefinition = new StringBuilder();
        buildClassDefinition(schema, 0, classDefinition);
        // https://stackoverflow.com/questions/11605097/how-to-create-a-java-lang-class-object-from-a-java-file-in-java
        System.out.println(classDefinition.toString());
        return null;
    }

    int buildClassDefinition(List<SchemaElement> schema, int offset,
                             StringBuilder classDefinition) {
        SchemaElement schemaElement = schema.get(offset);
        if (!schemaElement.isNested()) {
            classDefinition.append(String.format("%s %s;",
                    typeClassNameMap.get(schemaElement.type),
                    schemaElement.name));
            return offset+1;
        }
        classDefinition.append(String.format("class %s {",
                schema.get(offset).name));
        offset++;
        for (int i = 0; i < schemaElement.numChilds; i++) {
            offset = buildClassDefinition(schema, offset, classDefinition);
        }
        classDefinition.append("}");
        return offset;
    }
}
