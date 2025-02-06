public enum ValueType {
    INT((byte)0),
    CHAR((byte)1),
    STRING((byte)2),
    CUSTOM((byte)3);
    byte value;

    ValueType(byte value) {
        this.value = value;
    }

    static ValueType of(byte value) {
        for (ValueType valueType: values()) {
            if (value == valueType.value) {
                return valueType;
            }
        }
        return null;
    }
}