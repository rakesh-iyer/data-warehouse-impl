import java.nio.ByteBuffer;

public class DataPageHeader {
    int numValues;
    Encoding encoding = Encoding.Plain;
    Encoding definitionLevelEncoding = Encoding.Plain;
    Encoding repetitionLevelEncoding = Encoding.Plain;

    byte[] serialize() {
        ByteBuffer byteBuffer = Utils.getSmallByteBuffer();
        byteBuffer.putInt(numValues);
        byteBuffer.put(encoding.value);
        byteBuffer.put(definitionLevelEncoding.value);
        byteBuffer.put(repetitionLevelEncoding.value);
        return Utils.getBytes(byteBuffer);
    }
}
