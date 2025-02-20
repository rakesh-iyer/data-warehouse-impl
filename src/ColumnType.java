public enum ColumnType {
    NORMAL((byte)0),
    REPEATED((byte)1),
    OPTIONAL((byte)2);
    final int value;

    ColumnType(byte value) {
        this.value = value;
    }

    static ColumnType of(byte value) {
        for (ColumnType columnType: values()) {
            if (value == columnType.value) {
                return columnType;
            }
        }
        return null;
    }
}