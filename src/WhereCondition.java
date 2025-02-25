import java.util.Comparator;

public class WhereCondition {
    static interface ArithmeticOperator {
        boolean evaluate(Object fieldValue, Object targetValue);
    }
    static class EqualsOperator implements ArithmeticOperator {
        Comparator<Object> comparator;
        EqualsOperator(Comparator<Object> comparator) {
            this.comparator = comparator;
        }
        public boolean evaluate(Object fieldValue, Object targetValue) {
            return comparator.compare(fieldValue, targetValue) == 0;
        }
    }
    static class LesserOperator implements ArithmeticOperator {
        Comparator<Object> comparator;
        LesserOperator(Comparator<Object> comparator) {
            this.comparator = comparator;
        }
        public boolean evaluate(Object fieldValue, Object targetValue) {
            return comparator.compare(fieldValue, targetValue) < 0;
        }
    }
    static class GreaterOperator implements ArithmeticOperator {
        Comparator<Object> comparator;
        GreaterOperator(Comparator<Object> comparator) {
            this.comparator = comparator;
        }
        public boolean evaluate(Object fieldValue, Object targetValue) {
            return comparator.compare(fieldValue, targetValue) > 0;
        }
    }

    String fieldName;
    ArithmeticOperator operator;
    Object targetValue;
    boolean evaluate(Object fieldValue) {
        return operator.evaluate(fieldValue, targetValue);
    }

    WhereCondition(String conditionString) {
        String[] conditionParts = conditionString.split(">|<|=");
        this.fieldName = conditionParts[0];
        Comparator<Object> comparator =
                RecordField.getRecordField(this.fieldName).comparator;
        // we may want to parse the target value as the right type.
        this.targetValue = conditionParts[1];
        if (conditionString.contains(">")) {
            this.operator = new GreaterOperator(comparator);
        } else if (conditionString.contains("<")) {
            this.operator = new LesserOperator(comparator);
        } else {
            this.operator = new EqualsOperator(comparator);
        }
    }
}