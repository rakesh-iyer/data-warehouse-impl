public enum PageType {
    DataPage(0),
    IndexPage(1),
    DictionaryPage(2);
    byte value;
    PageType(int value) {
        this.value = (byte)value;
    }
}
