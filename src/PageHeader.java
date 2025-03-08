import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class PageHeader {
    PageType type;
    int uncompressedPageSize;
    int compressedPageSize;
    int crc;
    DataPageHeader dataPageHeader;
    IndexPageHeader indexPageHeader;
    DictionaryPageHeader dictionaryPageHeader;

    PageHeader(PageType type) {
        this.type = type;
        if (type == PageType.DataPage) {
            this.dataPageHeader = new DataPageHeader();
        } else if (type == PageType.IndexPage) {
            this.indexPageHeader = new IndexPageHeader();
        } else if (type == PageType.DictionaryPage) {
            this.dictionaryPageHeader = new DictionaryPageHeader();
        }
    }

    void write(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.write(type.value);
        dataOutputStream.writeInt(uncompressedPageSize);
        dataOutputStream.writeInt(compressedPageSize);
        dataOutputStream.writeInt(crc);
        if (type == PageType.DataPage) {
            dataOutputStream.write(dataPageHeader.serialize());
        } else if (type == PageType.IndexPage) {
            dataOutputStream.write(indexPageHeader.serialize());
        } else if (type == PageType.DictionaryPage) {
            dataOutputStream.write(dictionaryPageHeader.serialize());
        }
    }
}
