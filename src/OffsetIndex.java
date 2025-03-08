import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OffsetIndex {
    List<PageLocation> pageLocations = new ArrayList<>();
    // optional unencoded/uncompressed size for byte array types.
    // we will not use this initially.
    List<Integer> byteArrayDataBytes;
    void write(DataOutputStream dataOutputStream) throws IOException {
        for (PageLocation pageLocation: pageLocations) {
            dataOutputStream.write(pageLocation.serialize());
        }
    }

    void addNewPage(int fileOffset, int firstRowOffset) {
        pageLocations.add(new PageLocation(fileOffset, firstRowOffset));
    }

    int getStartIndex(int pageNumber) {
        return pageLocations.get(pageNumber).firstRowIndex;
    }

    void print() {
        for (PageLocation pageLocation: pageLocations) {
            Utils.printObject(pageLocation);
        }
        Utils.printObject(this);
    }
}
