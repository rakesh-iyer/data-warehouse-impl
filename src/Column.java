import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Column {
    String name;
    ColumnType type;
    List<ColumnValue> values = new ArrayList<>();
    ValueType valueType;
    FieldWriter fieldWriter;

    // TODO::
    // We can assume we can derive the value type by allowing it to be
    // decoded by the caller.
    // Lets see how to do this.
    static class ColumnValue {
        int repetitionLevel;
        int definitionLevel;
        Object value;
        ColumnValue(Object value, int repetitionLevel, int definitionLevel) {
            this.repetitionLevel = repetitionLevel;
            this.definitionLevel = definitionLevel;
            this.value = value;
        }
    }

    /*
        what does a field writer do,
        what does it need to encapsulate.
     */

    Column(String name) {
        this(name, ColumnType.NORMAL);
    }

    Column(String name, ColumnType type) {
        this.name = name;
        this.type = type;
    }

    void addValue(Object value) {
        values.add(new ColumnValue(value, 0, 0));
    }

    ColumnType getType() {
        return type;
    }

    ValueType getValueType() {
        return valueType;
    }

    int numberOfRecords() {
        return values.size();
    }

    Object aggregate() {
        Object value = values.getFirst().value;
        if (value instanceof String) {
            List<String> strings = new ArrayList<>();
            for (ColumnValue columnValue: values) {
                strings.add((String)columnValue.value);
            }
            return strings;
        } else if (value instanceof Character) {
            StringBuilder stringBuilder = new StringBuilder();
            for (ColumnValue columnValue: values) {
                stringBuilder.append((Character) columnValue.value);
            }
            return stringBuilder.toString();
        } else if (value instanceof Integer) {
            Long sum = 0L;
            for (ColumnValue columnValue: values) {
                sum += (Integer)columnValue.value;
            }
            return sum;
        }
        return null;
    }

    Object maximum() {
        Object value = values.getFirst().value;
        if (value instanceof String) {
            String maximum = (String)value;
            for (ColumnValue columnValue: values) {
                String currentValue = (String)columnValue.value;
                if (currentValue.compareTo(maximum) > 0) {
                    maximum = currentValue;
                }
            }
            return maximum;
        } else if (value instanceof Character) {
            Character maximum = (Character)value;
            for (ColumnValue columnValue: values) {
                Character currentValue = (Character)columnValue.value;
                if (currentValue > maximum) {
                    maximum = currentValue;
                }
            }
            return maximum;
        } else if (value instanceof Integer) {
            int maximum = (int)value;
            for (ColumnValue columnValue: values) {
                int currentValue = (int)columnValue.value;
                if (currentValue > maximum) {
                    maximum = currentValue;
                }
            }
            return maximum;
        }
        return null;
    }

    Object minimum() {
        Object value = values.getFirst().value;
        if (value instanceof String) {
            String minimum = (String)value;
            for (ColumnValue columnValue: values) {
                String currentValue = (String)columnValue.value;
                if (currentValue.compareTo(minimum) < 0) {
                    minimum = currentValue;
                }
            }
            return minimum;
        } else if (value instanceof Character) {
            Character minimum = (Character)value;
            for (ColumnValue columnValue: values) {
                Character currentValue = (Character)columnValue.value;
                if (currentValue < minimum) {
                    minimum = currentValue;
                }
            }
            return minimum;
        } else if (value instanceof Integer) {
            int minimum = (int)value;
            for (ColumnValue columnValue: values) {
                int currentValue = (int)columnValue.value;
                if (currentValue < minimum) {
                    minimum = currentValue;
                }
            }
            return minimum;
        }
        return null;
    }

    boolean stringCondition(String condition, String value, String target)
            throws Exception {
        if (condition.equals("=")) {
            return value.equals(target);
        } else if (condition.equals(">")) {
            return value.compareTo(target) > 0;
        } else if (condition.equals("<")) {
            return value.compareTo(target) < 0;
        } else if (condition.equals(">=")) {
            return value.equals(target) || value.compareTo(target) > 0;
        } else if (condition.equals("<=")) {
            return value.equals(target) || value.compareTo(target) < 0;
        } else {
            throw new Exception("Unsupported condition.");
        }
    }

    boolean charCondition(String condition, Character value, Character target)
            throws Exception {
        if (condition.equals("=")) {
            return value.equals(target);
        } else if (condition.equals(">")) {
            return value.compareTo(target) > 0;
        } else if (condition.equals("<")) {
            return value.compareTo(target) < 0;
        } else if (condition.equals(">=")) {
            return value.equals(target) || value.compareTo(target) > 0;
        } else if (condition.equals("<=")) {
            return value.equals(target) || value.compareTo(target) < 0;
        } else {
            throw new Exception("Unsupported condition.");
        }
    }

    boolean intCondition(String condition, int value, int target)
            throws Exception {
        if (condition.equals("=")) {
            return value == target;
        } else if (condition.equals(">")) {
            return value > target;
        } else if (condition.equals("<")) {
            return value < target;
        } else if (condition.equals(">=")) {
            return value == target || value > target;
        } else if (condition.equals("<=")) {
            return value == target || value < target;
        } else {
            throw new Exception("Unsupported condition.");
        }
    }

    // We only support =, >=, <=, >, < as filter options.
    Object filtered(String filter) throws Exception {
        String[] filterSplitted = filter.split(" ");
        if (filterSplitted.length != 2) {
            return null;
        }
        String condition = filterSplitted[0];
        String targetValue = filterSplitted[1];
        Object value = values.getFirst().value;
        List<Object> filtered = new ArrayList<>();
        if (value instanceof String) {
            for (ColumnValue columnValue: values) {
                String currentValue = (String)columnValue.value;
                if (stringCondition(condition, currentValue, targetValue)) {
                    filtered.add(currentValue);
                }
            }
        } else if (value instanceof Character) {
            for (ColumnValue columnValue: values) {
                Character currentValue = (Character)columnValue.value;
                if (charCondition(condition, currentValue,
                        targetValue.charAt(0))) {
                    filtered.add(currentValue);
                }
            }
        } else if (value instanceof Integer) {
            for (ColumnValue columnValue: values) {
                Character currentValue = (Character)columnValue.value;
                if (intCondition(condition, currentValue,
                        Integer.parseInt(targetValue))) {
                    filtered.add(currentValue);
                }
            }
        }
        return filtered;
    }

    void writeTo(FileOutputStream fileOutputStream) throws IOException {
        // Write the column type and the value type as header data.
        fileOutputStream.write(getType().value);
        fileOutputStream.write(getValueType().value);
        for (ColumnValue columnValue: values) {
            if (columnValue.value instanceof String) {
                String value = (String)columnValue.value;
                fileOutputStream.write((byte)value.length());
                fileOutputStream.write(value.getBytes());
            } else if (columnValue.value instanceof Integer) {
                int value = (int)columnValue.value;
                fileOutputStream.write(value & 0xff);
                fileOutputStream.write((value & 0xff00) >> 8);
                fileOutputStream.write((value & 0xff0000) >> 16);
                fileOutputStream.write((value & 0xff000000) >> 24);
            } else if (columnValue.value instanceof Character) {
                byte value = (byte)columnValue.value;
                fileOutputStream.write(value);
            } else {
                throw new RuntimeException("Unsupported value type found.");
            }
        }
    }

    //
    // a. Start with supporting Normal columns with string, char and int data.
    // b. Add support for repeated and optional types.
    // c. Add support for custom message types.
    static Column build(String columnName, byte[] columnData) {
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
                    char value = Character.valueOf((char)columnData[i]);
                    ColumnValue columnValue = new ColumnValue(value,
                            repetitionLevel, definitionLevel);
                    column.addValue(columnValue);
                }
                break;
            case ValueType.INT:
                for (int i = valueOffset; i < columnData.length; i += 4) {
                    int value = columnData[i] + columnData[i+1] << 8 +
                            columnData[i+2] << 16 + columnData[i+3] << 24;
                    ColumnValue columnValue = new ColumnValue(value,
                            repetitionLevel, definitionLevel);
                    column.addValue(columnValue);
                }
                break;
            case ValueType.STRING:
                for (int i = valueOffset; i < columnData.length;) {
                    // We limit to 255 byte strings for now
                    byte strLength = columnData[i];
                    String str = new String(columnData, i, strLength);
                    ColumnValue columnValue = new ColumnValue(str,
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

    void store(Path storePath) throws RuntimeException {
        try {
            FileOutputStream fileOutputStream =
                    new FileOutputStream(storePath.toFile());
            writeTo(fileOutputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}
