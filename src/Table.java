import java.util.List;

public class Table {
    FileMetadata fileMetadata;
    void create(String name, Class schema, String fileName) throws Exception {
        FileMetadata fileMetadata = new FileMetadata(fileName, schema);
    }

    void addData(List<Object> dataRows, Class schema) throws Exception {
        // whats the row group to add this to.
        // lets assume we add to one row group and let it lead to creating a
        // new row group.
        RowGroup rowGroup = fileMetadata.rowGroups.getLast();
        for (Object dataRow: dataRows) {
            rowGroup.addRow(dataRow, schema);
        }
    }
}
