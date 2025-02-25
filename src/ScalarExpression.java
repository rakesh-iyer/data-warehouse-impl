import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ScalarExpression {
    List<String> fieldNames;
    Map<String, Object> fieldValues = new HashMap<>();
    boolean evaluated;

    ScalarExpression(String expression) {
        // We limit to singleton fields as a typical scalar expression.
        // We propose to add sum, count, min, max, avg, etc for repeated fields.
        // We won't support arithmetic operations across fields.
        this.fieldNames = List.of(expression);
    }

    boolean isReady(Map<String, Object> fieldValues) {
        String fieldName = fieldNames.getFirst();
        if (!RecordField.getRecordField(fieldName).repeated) {
            return true;
        }

        Object value = fieldValues.get(fieldName);
        return value != null && ((Aggregation.RepeatedValue)value).isDone();
    }

    boolean isEvaluated() {
        return evaluated;
    }

    void clearEvaluated() {
        evaluated = false;
    }

    Object evaluate(Map<String, Object> fieldValues) {
        // We are dealing with simple expressions atm.
        // We could however use common functionality as the where clause.
        String fieldName = fieldNames.getFirst();
        Object value = fieldValues.get(fieldName);
        evaluated = true;
        if (!RecordField.getRecordField(fieldName).repeated) {
            return value;
        }
        Aggregation.RepeatedValue repeatedValue =
            (Aggregation.RepeatedValue)value;
        return repeatedValue;
    }

    List<String> extractFieldNames() {
        return List.of(fieldNames.getFirst());
    }

    int getRepetitionLevel() {
        List<String> fullyQualifiedNames = extractFieldNames();
        int maxRepetitionLevel = Integer.MIN_VALUE;
        for (String fullyQualifiedName: fullyQualifiedNames) {
            int repetitionLevel =
                    RecordField.getRepetitionLevel(fullyQualifiedName);
            if (maxRepetitionLevel < repetitionLevel) {
                maxRepetitionLevel = repetitionLevel;
            }
        }
        return maxRepetitionLevel;
    }

    public String toString() {
        return fieldNames.getFirst();
    }
}
