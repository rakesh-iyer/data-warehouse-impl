import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

//  Reference:: https://parquet.apache.org/docs/file-format/metadata/
public class FileMetadata {
    int version = 1;
    List<SchemaElement> schema;
    int numRows;
    List<RowGroup> rowGroups = new ArrayList<>();
    List<KeyValue> keyValueMetadata = new ArrayList<>();
    int footerLength;
    int magicNumber;
    String fileName;

    FileMetadata(String fileName, Class type) {
        this.fileName = fileName;
        this.schema = SchemaUtils.buildSchema(type);
        this.rowGroups.add(new RowGroup(schema, fileName));
    }

    void addRowGroup() {
        rowGroups.add(new RowGroup(schema, fileName));
    }

    void addRow(Object value, Class valueType) throws Exception {
        if (rowGroups.getLast().numRows == RowGroup.MAX_ROWS) {
            addRowGroup();
        }
        rowGroups.getLast().addRow(value, valueType);
        numRows++;
    }

    void write(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeInt(version);
        for (SchemaElement schemaElement: schema) {
            schemaElement.write(dataOutputStream);
        }
        dataOutputStream.writeInt(numRows);
        for (RowGroup rowGroup: rowGroups) {
            rowGroup.write(dataOutputStream);
        }
        for (KeyValue keyValue: keyValueMetadata) {
            dataOutputStream.write(keyValue.key.getBytes());
            dataOutputStream.write(keyValue.value.getBytes());
        }
        dataOutputStream.writeInt(footerLength);
        dataOutputStream.writeInt(magicNumber);
        dataOutputStream.write(fileName.getBytes());
    }

    void write() throws Exception {
        try(DataOutputStream dataOutputStream =
                new DataOutputStream(new FileOutputStream(fileName))) {
            write(dataOutputStream);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    List<Object> findValues(String columnName, Object startValue,
                         Object endValue) throws IOException {
        List<Object> values = new ArrayList<>();
        for (RowGroup rowGroup: rowGroups) {
            values.addAll(rowGroup.findRows(columnName, startValue,
                    endValue));
        }
        return values;
    }

    void print() {
        for (RowGroup rowGroup: rowGroups) {
            rowGroup.print();
        }
    }
}
