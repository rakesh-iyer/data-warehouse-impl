import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;

public class RowGroup {
    static int MAX_ROWS = 5000;
    List<ColumnChunk> columnChunks =  new ArrayList<>();
    Map<String, ColumnChunk> columnChunkByName = new HashMap<>();
    int totalByteSize;
    int numRows;

    RowGroup(List<SchemaElement> schema, String filePath) {
        Deque<String> pathStack = new LinkedList<String>();
        Deque<Integer> childrenStack = new LinkedList<Integer>();
        // schema elements are in dfs order.
        String currentPath = "";
        // We expect the childrenStack to finish with one item left, we set this
        // to the maximum value of children plus one.
        childrenStack.addFirst(schema.size() + 1);
        // Iterate into the schema.
        for (SchemaElement schemaElement: schema) {
            if (schemaElement.numChilds == 0) {
                String columnName = !currentPath.isEmpty() ?
                        currentPath + "." + schemaElement.name :
                        schemaElement.name;
                // this is atomic
                ColumnChunk columnChunk = new ColumnChunk(schemaElement.type,
                        columnName, filePath);
                columnChunks.add(columnChunk);
                columnChunkByName.put(columnName, columnChunk);
                int childCount = childrenStack.removeFirst();
                childCount--;
                if (childCount > 0) {
                    // still children remain.
                    childrenStack.addFirst(childCount);
                } else {
                    // undo stack on the last child
                    currentPath = pathStack.removeFirst();
                }
            } else {
                String newPath = !currentPath.isEmpty() ?
                        currentPath + "." + schemaElement.name :
                        schemaElement.name;
                pathStack.addFirst(currentPath);
                currentPath = newPath;
                childrenStack.addFirst(schemaElement.numChilds);
            }
        }
    }

    void write(DataOutputStream dataOutputStream) throws IOException {
        for (ColumnChunk columnChunk: columnChunks) {
            columnChunk.write(dataOutputStream);
        }
        dataOutputStream.write(totalByteSize);
        dataOutputStream.write(numRows);
    }

    int addColumnValues(Object value, Class schema, int columnOffset)
            throws Exception {
        for (Field field: schema.getFields()) {
            Class type = field.getType();
            // if field is nested recurse into it
            if (Utils.isTypeNested(type)) {
                // TODO:: Add support for Repetition levels similar to Dremel.
                // We just retrieve the first value atm.
                if (Utils.isTypeRepeated(type)) {
                    Object repeatedValue = field.get(value);
                    value = Array.get(repeatedValue, 0);
                    columnOffset = addColumnValues(value,
                            type.componentType(), columnOffset);
                } else {
                    columnOffset = addColumnValues(field.get(value), type,
                            columnOffset);
                }
                continue;
            }
            Object columnValue;
            if (Utils.isTypeAtomic(type)) {
                columnValue = field.get(value);
            } else if (Utils.isTypeOptional(type)){
                // TODO:: Add support for Definition Levels and all optionals
                //  similar to Dremel.
                columnValue = ((Optional<Integer>)field.get(value)).get();
            } else {
                // TODO:: Add support for Repetition levels similar to Dremel.
                // We just retrieve the first value atm.
                Object repeatedValue = field.get(value);
                columnValue = Array.get(repeatedValue, 0);
            }
            int bytesWritten =
                    columnChunks.get(columnOffset).addValue(columnValue,
                            numRows);
            totalByteSize += bytesWritten;
            columnOffset++;
        }
        return columnOffset;
    }

    void addRow(Object value, Class schema) throws Exception {
        addColumnValues(value, schema, 0);
        numRows++;
    }

    ColumnChunk getColumnChunkByName(String columnName) {
        return columnChunkByName.get(columnName);
    }

    List<Map<String, Object>> findRows(String columnName,
                                            Object startValue,
                            Object endValue) throws IOException {
        ColumnChunk predicateColumnChunk = getColumnChunkByName(columnName);
        List<Integer> valueIndexes =
                predicateColumnChunk.findValueIndexes(startValue,
                endValue);
        List<Map<String, Object>> rowValues = new ArrayList<>();
        for (Integer valueIndex: valueIndexes) {
            // Use a Treemap for easy visual inspection.
            Map<String, Object> valueMap = new TreeMap<>();
            for (String columnChunkName: columnChunkByName.keySet()) {
                ColumnChunk columnChunk =
                        columnChunkByName.get(columnChunkName);
                Object columnValue = columnChunk.getValue(valueIndex);
                valueMap.put(columnChunkName, columnValue);
            }
            rowValues.add(valueMap);
        }
        return rowValues;
    }

    void print() {
        for (ColumnChunk columnChunk: columnChunks) {
            columnChunk.print();
        }
    }
}
