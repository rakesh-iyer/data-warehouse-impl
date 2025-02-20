import java.util.List;

public class Record {
    StringBuilder recordString = new StringBuilder();
    Record() {
        recordString.append("start record\n");
    }
    void addTag(String tagName, boolean startEnd)  {
        if (startEnd) {
            recordString.append(String.format("start %s\n", tagName));
        } else {
            recordString.append(String.format("end %s\n", tagName));
        }
    }

    void addValue(String fieldName, Object value) {
        recordString.append(String.format("%s:%s\n", fieldName, value));
    }

    void print() {
        System.out.println(recordString.toString());
    }

    String finish() {
        recordString.append("end record");
        return recordString.toString();
    }

    void startNestedRecords(RecordField sourceField,
                          RecordField destinationField) {
        List<String> path = sourceField.findPath(destinationField);
        // remove the source field from the path
        if (path.size() == 0) {
            return;
        }
        path.removeLast();
        for (int i = path.size() - 1; i >= 0; i--) {
            addTag(path.get(i), /*startEnd=*/true);
        }
    }

    void endNestedRecords(RecordField sourceField,
                         RecordField destinationField) {
        List<String> path = sourceField.findPath(destinationField);
        // remove the source field from the path
        if (path.size() == 0) {
            return;
        }
        path.removeLast();
        for (int i = 0; i < path.size(); i++) {
            addTag(path.get(i), /*startEnd=*/false);
        }
    }
}
