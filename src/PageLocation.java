import java.nio.ByteBuffer;

public class PageLocation {
    // offset of the page in the file.
    int fileOffset;
    // sum of compressed page size and header.
    int compressedPageSize;
    // index of the first row of the page in the row group.
    int firstRowIndex;

    PageLocation(int fileOffset, int firstRowIndex) {
        this.fileOffset = fileOffset;
        this.firstRowIndex = firstRowIndex;
    }

    byte[] serialize() {
        ByteBuffer byteBuffer = Utils.getSmallByteBuffer();
        byteBuffer.putInt(fileOffset);
        byteBuffer.putInt(compressedPageSize);
        byteBuffer.putInt(firstRowIndex);
        return Utils.getBytes(byteBuffer);
    }
}
