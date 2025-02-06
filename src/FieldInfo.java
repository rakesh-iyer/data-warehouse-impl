public class FieldInfo {
    String fieldType;
    String fieldName;

    FieldInfo(String fieldType, String fieldName) {
        this.fieldType = fieldType;
        this.fieldName = fieldName;
    }

    public String toString() {
        return fieldType + "::" + fieldName;
    }
}