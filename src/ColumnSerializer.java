import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class ColumnSerializer {
    static void writeColumn(Column column,
                             FileOutputStream fileOutputStream) throws IOException {
        // Write the column type and the value type as header data.
        fileOutputStream.write(column.getType().value);
        fileOutputStream.write(column.getValueType().value);
        for (Column.ColumnValue columnValue : column.getValues()) {
            if (columnValue.value instanceof String) {
                String value = (String) columnValue.value;
                fileOutputStream.write((byte) value.length());
                fileOutputStream.write(value.getBytes());
            } else if (columnValue.value instanceof Integer) {
                int value = (int) columnValue.value;
                fileOutputStream.write(value & 0xff);
                fileOutputStream.write((value & 0xff00) >> 8);
                fileOutputStream.write((value & 0xff0000) >> 16);
                fileOutputStream.write((value & 0xff000000) >> 24);
            } else if (columnValue.value instanceof Character) {
                byte value = (byte) columnValue.value;
                fileOutputStream.write(value);
            } else {
                throw new RuntimeException("Unsupported value type found.");
            }
        }
    }



    static void storeColumn(Column column, Path storePath) throws IOException {
        FileOutputStream fileOutputStream =
                new FileOutputStream(storePath.toFile());
        writeColumn(column, fileOutputStream);
    }

    static void storeColumnContents(Column column, String storePath) throws IOException {
        column.getMemoryBuffer().flip();
        /*
        The READ and WRITE options determine if the file should be opened for
        reading and/or writing. If neither option (or the APPEND option) is
        contained in the array then the file is opened for reading.
        CREATE_NEW:
        This option is ignored when the file is opened only for reading.
        CREATE:
        This option is ignored if the CREATE_NEW option is also present or
        the file is opened only for reading.
         */
        FileChannel fileChannel =
                FileChannel.open(Paths.get(storePath).resolve(column.getName()),
                        StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        fileChannel.write(column.getMemoryBuffer());
    }
}
