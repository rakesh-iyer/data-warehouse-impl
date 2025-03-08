public enum CompressionCodec {
    Uncompressed(0);

    byte value;
    CompressionCodec(int value) {
        this.value = (byte)value;
    }
}
