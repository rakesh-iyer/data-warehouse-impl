import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

// represents an In-Memory page
public class Page {
    static final int PAGE_SIZE = 4096;
    PageHeader pageHeader;
    byte[] data = new byte[PAGE_SIZE];

    Page() {
        pageHeader = new PageHeader(PageType.DataPage);
    }

    int addValue(byte[] value) {
        System.arraycopy(value, 0, data, pageHeader.compressedPageSize,
                value.length);
        pageHeader.uncompressedPageSize += value.length;
        pageHeader.compressedPageSize += value.length;
        return pageHeader.compressedPageSize;
    }

    void write(DataOutputStream dataOutputStream) throws IOException {
        pageHeader.write(dataOutputStream);
        dataOutputStream.write(data);
    }
    // we should make start offset after page header.
    int size() {
        return data.length;
    }

    void print() {
        Utils.printObject(this);
    }
}
