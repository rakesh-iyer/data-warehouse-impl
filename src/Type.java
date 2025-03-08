public enum Type {
    Boolean(0),
    Int32(1),
    Int64(2),
    Int96(3),
    Float(4),
    Double(5),
    ByteArray(6),
    FixedLengthByteArray(7);

    byte value;
    Type(int value) {
        this.value = (byte)value;
    }
}
