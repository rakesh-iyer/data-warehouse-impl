public enum Encoding {
    Plain(0),
    Dictionary(2),
    RLE(3),
    BitPacked(4),
    DeltaEncoding(5),
    DeltaLengthByteArray(6),
    DeltaStrings(7),
    RLEDictionary(8),
    ByteStreamSplit(9);

    byte value;
    Encoding(int value) {
        this.value = (byte)value;
    }

    static Encoding from(String typeString) {
        return Encoding.valueOf(typeString);
    }
}
