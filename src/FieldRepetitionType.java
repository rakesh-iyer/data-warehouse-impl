public enum FieldRepetitionType {
    Required(0),
    Optional(1),
    Repeated(2);
    byte value;
    FieldRepetitionType(int value) {
        this.value = (byte)value;
    }
}
