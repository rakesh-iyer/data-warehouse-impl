import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

public class Column {
    String name;
    ColumnType type;
    List<ColumnValue> values = new ArrayList<>();
    ValueType valueType;
    // In memory Dremel.
    ByteBuffer memoryBuffer = ByteBuffer.allocateDirect(10000);

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

    void addValue(ColumnValue value) {
        values.add(value);
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

    public ColumnValue getColumnValue(int position) {
        return values.get(position);
    }

    public boolean isValidPosition(int position) {
        return position >= 0 && position < values.size();
    }

    List<ColumnValue> getValues() {
        return values;
    }

    ByteBuffer getMemoryBuffer() {
        return memoryBuffer;
    }

    String getName() {
        return name;
    }

    Object aggregate() {
        Object value = values.getFirst().value;
        if (value instanceof String) {
            List<String> strings = new ArrayList<>();
            for (ColumnValue columnValue : values) {
                strings.add((String) columnValue.value);
            }
            return strings;
        } else if (value instanceof Character) {
            StringBuilder stringBuilder = new StringBuilder();
            for (ColumnValue columnValue : values) {
                stringBuilder.append((Character) columnValue.value);
            }
            return stringBuilder.toString();
        } else if (value instanceof Integer) {
            Long sum = 0L;
            for (ColumnValue columnValue : values) {
                sum += (Integer) columnValue.value;
            }
            return sum;
        }
        return null;
    }

    Object maximum() {
        Object value = values.getFirst().value;
        if (value instanceof String) {
            String maximum = (String) value;
            for (ColumnValue columnValue : values) {
                String currentValue = (String) columnValue.value;
                if (currentValue.compareTo(maximum) > 0) {
                    maximum = currentValue;
                }
            }
            return maximum;
        } else if (value instanceof Character) {
            Character maximum = (Character) value;
            for (ColumnValue columnValue : values) {
                Character currentValue = (Character) columnValue.value;
                if (currentValue > maximum) {
                    maximum = currentValue;
                }
            }
            return maximum;
        } else if (value instanceof Integer) {
            int maximum = (int) value;
            for (ColumnValue columnValue : values) {
                int currentValue = (int) columnValue.value;
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
            String minimum = (String) value;
            for (ColumnValue columnValue : values) {
                String currentValue = (String) columnValue.value;
                if (currentValue.compareTo(minimum) < 0) {
                    minimum = currentValue;
                }
            }
            return minimum;
        } else if (value instanceof Character) {
            Character minimum = (Character) value;
            for (ColumnValue columnValue : values) {
                Character currentValue = (Character) columnValue.value;
                if (currentValue < minimum) {
                    minimum = currentValue;
                }
            }
            return minimum;
        } else if (value instanceof Integer) {
            int minimum = (int) value;
            for (ColumnValue columnValue : values) {
                int currentValue = (int) columnValue.value;
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
            for (ColumnValue columnValue : values) {
                String currentValue = (String) columnValue.value;
                if (stringCondition(condition, currentValue, targetValue)) {
                    filtered.add(currentValue);
                }
            }
        } else if (value instanceof Character) {
            for (ColumnValue columnValue : values) {
                Character currentValue = (Character) columnValue.value;
                if (charCondition(condition, currentValue,
                        targetValue.charAt(0))) {
                    filtered.add(currentValue);
                }
            }
        } else if (value instanceof Integer) {
            for (ColumnValue columnValue : values) {
                Character currentValue = (Character) columnValue.value;
                if (intCondition(condition, currentValue,
                        Integer.parseInt(targetValue))) {
                    filtered.add(currentValue);
                }
            }
        }
        return filtered;
    }

    static ColumnType getColumnType(Class type) {
        if (Utils.isTypeRepeated(type)) {
            return ColumnType.REPEATED;
        } else if (Utils.isTypeOptional(type)) {
            return ColumnType.OPTIONAL;
        } else {
            return ColumnType.NORMAL;
        }
    }

    //
    // a. Start with supporting Normal columns with string, char and int data.
    // b. Add support for repeated and optional types.
    // c. Add support for custom message types.
    void write(Object fieldData, int repetitionLevel, int definitionLevel)
            throws Exception {
        if (fieldData instanceof Integer) {
            memoryBuffer.putInt((Integer) fieldData);
            memoryBuffer.putInt(repetitionLevel);
            memoryBuffer.putInt(definitionLevel);
        } else if (fieldData instanceof Byte) {
            memoryBuffer.put((Byte) fieldData);
            memoryBuffer.putInt(repetitionLevel);
            memoryBuffer.putInt(definitionLevel);
        } else if (fieldData instanceof String) {
            String stringFieldData = (String) fieldData;
            memoryBuffer.put((byte) stringFieldData.length());
            memoryBuffer.put(stringFieldData.getBytes(
                    Charset.defaultCharset()));
            memoryBuffer.putInt(repetitionLevel);
            memoryBuffer.putInt(definitionLevel);
        } else {
            System.out.println("A special value is written");
        }
    }

    void printContents() {
        // read the bytebuffer or put it in a file.
        memoryBuffer.flip();
        System.out.println("ColumnName: " + name);
        while (memoryBuffer.hasRemaining()) {
            byte data = memoryBuffer.get();
            System.out.print(" " + data);
        }
        System.out.println();
    }
}
