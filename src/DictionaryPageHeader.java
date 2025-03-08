import java.nio.ByteBuffer;

public class DictionaryPageHeader {
    int numValues;

    byte[] serialize() {
        ByteBuffer byteBuffer = Utils.getSmallByteBuffer();
        byteBuffer.putInt(numValues);
        return Utils.getBytes(byteBuffer);
    }
}
