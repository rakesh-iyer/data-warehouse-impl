public enum BoundaryOrder {
    Unordered(0),
    Ascending(1),
    Descending(2);

    byte value;
    BoundaryOrder(int value) {
        this.value = (byte)value;
    }
}
