import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ColumnChunk {
    String filePath;
    int fileOffset;
    ColumnMetadata columnMetadata;
    ColumnIndex columnIndex;
    OffsetIndex offsetIndex;
    List<Page> pageList = new ArrayList<>();
    //  Add the column and offset index information, for quick access to other
    // columns in the row
    PlainEncoder encoder = new PlainEncoder();
    ColumnChunk(Type columnType, String columnPath, String filePath) {
        columnMetadata = new ColumnMetadata(columnType, columnPath);
        this.filePath = String.format("%s.%s", filePath, columnPath);
        columnIndex = new ColumnIndex(columnType, encoder);
        offsetIndex = new OffsetIndex();
        addNewPage(0);
    }

    void addNewPage(int rowOffsetForNewPage) {
        pageList.add(new Page());
        columnIndex.addNewPage();
        offsetIndex.addNewPage(fileOffset, /*firstRowOffset
        =*/rowOffsetForNewPage);
    }

    void write(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.write(filePath.getBytes());
        dataOutputStream.writeInt(fileOffset);
        columnMetadata.write(dataOutputStream);
        columnIndex.write(dataOutputStream);
        offsetIndex.write(dataOutputStream);
        for (Page page: pageList) {
            page.write(dataOutputStream);
        }
    }

    // returns the bytes written.
    int addValue(Object value, int numRows) throws IOException {
        // write Value with the write encodings.
        columnMetadata.numValues++; // should we use atomics?
        // can you write the entire value in the current page, if not create
        // new page and need to do 2 writes?
        columnMetadata.addValue();
        if (columnMetadata.type == Type.ByteArray) {
            columnMetadata.updateSizes(((String)value).length());
        } else {
            columnMetadata.updateSizes(columnMetadata.getSize());
        }
        byte[] encodedValue = encoder.encode(value, columnMetadata.type);
        // Write the encoded value to the data page.
        Page currentPage = pageList.getLast();
        int pageOffset = currentPage.addValue(encodedValue);
        // Assume pages for a column chunk are laid sequentially.
        // Since we write to the end of the file we increment the file offset.
        fileOffset += encodedValue.length;
        // create new Page if it cannot accomodate another field of this type.
        if (currentPage.size() - pageOffset < columnMetadata.getSize()) {
            // Adjust file offset by skipped bytes at the end of the file.
            fileOffset += (currentPage.size() - pageOffset);
            addNewPage(/*rowOffsetForNewPage=*/numRows+1);
        }

        columnIndex.updateIndexEntry(pageList.size()-1, value);
        return encodedValue.length;
    }

    <T extends Comparable<T>> boolean isValueInRangeInternal(Object object,
                                                             Object startRange,
                                                             Object endRange) {
        T value = (T)object;
        T startRangeValue = (T)startRange;
        T endRangeValue = (T)endRange;
        return value.compareTo(startRangeValue) >= 0 &&
                value.compareTo(endRangeValue) <= 0;
    }

    boolean isValueInRange(Object value, Object startRange, Object endRange) throws IOException {
        if (value instanceof Character) {
            return isValueInRangeInternal((Character)value,
                    (Character)startRange,
                    (Character)endRange);
        } else if (value instanceof Integer) {
            return isValueInRangeInternal((Integer)value, (Integer)startRange,
                    (Integer)endRange);
        } else if (value instanceof String) {
            return isValueInRangeInternal((String)value, (String)startRange,
                    (String)endRange);
        }
        throw new IOException("value is not of recognized type");
    }

    List<Integer> findValueIndexes(Object startValue, Object endValue) throws IOException {
        // From index we locate the pages that contain the range
        List<Integer> valueIndexes = new ArrayList<>();
        List<Integer> pagesInRange = columnIndex.getPagesInRange(startValue,
                endValue);
        for (int pageNumber: pagesInRange) {
            Page page = pageList.get(pageNumber);
            int offset = 0;
            int index = offsetIndex.getStartIndex(pageNumber);
            while (offset < page.size()) {
                Object value = encoder.decode(page.data, offset,
                        columnMetadata.type);
                if (isValueInRange(value, startValue, endValue)) {
                    valueIndexes.add(index);
                }
                offset += columnMetadata.getSize();
                index++;
            }
        }
        return valueIndexes;
    }

    Object getValue(int valueIndex) {
        int pageNumber;
        for (pageNumber = 0; pageNumber < pageList.size(); pageNumber++) {
            if (valueIndex < offsetIndex.getStartIndex(pageNumber)) {
                break;
            }
        }
        // Ideally we should do this for multiple values in the same page.
        // iterate in the page corresponding to pageNumber -1.
        Page page = pageList.get(pageNumber-1);
        int offsetFromPageStart =
                valueIndex - offsetIndex.getStartIndex(pageNumber-1);
        Object value = encoder.decode(page.data,
                offsetFromPageStart * columnMetadata.getSize(),
                columnMetadata.type);
        return value;
    }

    void print() {
        columnIndex.print();
        offsetIndex.print();
        for (Page page: pageList) {
            page.print();
        }
    }
}
