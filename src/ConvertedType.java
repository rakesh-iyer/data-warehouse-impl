public enum ConvertedType {
    Utf8(0),
    Map(1),
    MapKey(2),
    MapKeyValue(3),
    List(4);
    byte value;
    ConvertedType(int value) {
        this.value = (byte)value;
    }
}
