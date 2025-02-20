import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

public class ColumnDeserializer {
    static char readCharacter(byte[] data, int offset) {
        return (char) data[offset];
    }

    static int readInteger(byte[] data, int offset) {
        return data[offset] + (data[offset + 1] << 8) +
                (data[offset + 2] << 16) + (data[offset + 3] << 24);
    }

    static int readIntegerBigEndian(byte[] data, int offset) {
        return (data[offset] << 24) + (data[offset + 1] << 16) +
                (data[offset + 2] << 8) + data[offset + 3];
    }

    static ValueType getValueType(Class type) {
        if (type == int.class) {
            return ValueType.INT;
        } else if (type == char.class) {
            return ValueType.CHAR;
        } else if (type == String.class) {
            return ValueType.STRING;
        } else {
//            throw new RuntimeException("This is unexpected.");
            return ValueType.CUSTOM;
        }
    }

    static Column columnFrom(String columnName, Class type,
                            ColumnType columnType,
                       byte[] columnData) {
        Column column = new Column(columnName, columnType);
        ValueType valueType = getValueType(type);
        for (int i = 0; i < columnData.length;) {
            Object value = null;
            if (valueType == ValueType.CHAR) {
                // We are just dealing with single byte characters.
                value = readCharacter(columnData, i);
                i++;
            } else if (valueType == ValueType.INT) {
                value = readInteger(columnData, i);
                i += 4;
            } else if (valueType == ValueType.STRING) {
                byte strLength = columnData[i];
                i++;
                value = new String(columnData, i, strLength);
                i += strLength;
            }
            int repetitionLevel = readIntegerBigEndian(columnData, i);
            i += 4;
            int definitionLevel = readIntegerBigEndian(columnData, i);
            i += 4;
            Column.ColumnValue columnValue = new Column.ColumnValue(value,
                    repetitionLevel, definitionLevel);
            column.addValue(columnValue);
        }
        return column;
    }

    static Column columnFrom(Class type, ColumnType columnType,
                          String columnName) {
        Properties properties = System.getProperties();
        Path columnPath =
                Paths.get(properties.getProperty("user.dir")).resolve(columnName);
        try {
            FileChannel fileChannel = FileChannel.open(columnPath,
                    StandardOpenOption.READ);
            // NIO file channel reads maybe continued.
            int fileSize = (int)fileChannel.size();
            byte[] columnBytes = new byte[fileSize];
            ByteBuffer columnByteBuffer = ByteBuffer.wrap(columnBytes);
            fileChannel.read(columnByteBuffer);
            return columnFrom(columnName, type, columnType, columnBytes);
        } catch (IOException e) {
            System.out.println("Column file not found.");
        }
        return null;
    }

    static Column buildColumn(String columnName, byte[] columnData) {
        ColumnType columnType = ColumnType.of(columnData[0]);
        Column column = new Column(columnName, columnType);
        if (columnType == ColumnType.REPEATED) {
            return column;
        } else if (columnType == ColumnType.OPTIONAL) {
            return column;
        }
        // We process normal columns here.
        ValueType valueType = ValueType.of(columnData[1]);
        int valueOffset = 2;
        int repetitionLevel = 0;
        int definitionLevel = 0;
        switch (valueType) {
            // We are just dealing with single byte characters.
            case ValueType.CHAR:
                for (int i = valueOffset; i < columnData.length; i++) {
                    char value = Character.valueOf((char) columnData[i]);
                    Column.ColumnValue columnValue = new Column.ColumnValue(value,
                            repetitionLevel, definitionLevel);
                    column.addValue(columnValue);
                }
                break;
            case ValueType.INT:
                for (int i = valueOffset; i < columnData.length; i += 4) {
                    int value = columnData[i] + columnData[i + 1] << 8 +
                            columnData[i + 2] << 16 + columnData[i + 3] << 24;
                    Column.ColumnValue columnValue = new Column.ColumnValue(value,
                            repetitionLevel, definitionLevel);
                    column.addValue(columnValue);
                }
                break;
            case ValueType.STRING:
                for (int i = valueOffset; i < columnData.length; ) {
                    // We limit to 255 byte strings for now
                    byte strLength = columnData[i];
                    String str = new String(columnData, i, strLength);
                    Column.ColumnValue columnValue = new Column.ColumnValue(str,
                            repetitionLevel, definitionLevel);
                    column.addValue(columnValue);
                    i += strLength;
                }
                break;
            case ValueType.CUSTOM:
                // the steps to do this would be
                // have the custom type stored in the byte format, for this it
                // has to be a concrete type with only value fields.
                // you read the type create a java class out of it and then
                // deserialize the bytes corresponding to it.
                break;
            default:
                break;
        }
        return column;
    }

}
