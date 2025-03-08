import java.util.List;

public class RowGroupMetadata {
    List<ColumnChunk> columnChunks;
    int totalByteSize;
    int numRows;
}
