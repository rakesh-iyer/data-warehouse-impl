import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WhereClause {
    // Each WhereClause has the following structure.
    // condition[i] conditionOperator[i] condition[i+1]
    List<WhereCondition> conditions= new ArrayList<>();
    List<ConditionOperator> conditionOperators = new ArrayList<>();

    // The following classes could be used for further refactoring.
    // Their current purpose is to help with readability.
    static interface ConditionOperator {
    }

    static class AndConditionOperator implements ConditionOperator {
    }

    static class OrConditionOperator implements ConditionOperator {
    }

    boolean evaluate(Map<String, Object> fieldValues) {
        // we execute the conditions based on the protocol.
        // we don't support bracketization, just assume operators are applied
        // serially.
        WhereCondition first = conditions.get(0);
        Object value = fieldValues.get(first.fieldName);
        boolean retValue = first.evaluate(value);
        for (int i = 1; i < conditions.size(); i++) {
            WhereCondition nextCondition = conditions.get(i);
            ConditionOperator operator = conditionOperators.get(i-1);
            Object nextValue = fieldValues.get(nextCondition.fieldName);
            boolean newValue = nextCondition.evaluate(nextValue);
            if (operator instanceof OrConditionOperator) {
                retValue = retValue | newValue;
            } else if (operator instanceof AndConditionOperator) {
                retValue = retValue & newValue;
            }
        }
        return retValue;
    }

    WhereClause(String whereClauseString) {
        int nextIndex = 0;
        while (nextIndex < whereClauseString.length()) {
            int andIndex = whereClauseString.indexOf("AND",
                    nextIndex);
            andIndex = andIndex >= 0 ? andIndex : Integer.MAX_VALUE;
            int orIndex = whereClauseString.indexOf("OR", nextIndex);
            orIndex = orIndex >= 0 ? orIndex : Integer.MAX_VALUE;
            if (andIndex < orIndex) {
                String conditionString = whereClauseString.substring(nextIndex,
                        andIndex).trim();
                // add operator and condition.
                conditions.add(new WhereCondition(conditionString));
                conditionOperators.add(new AndConditionOperator());
                nextIndex = andIndex + "AND".length();
            } else if (orIndex < Integer.MAX_VALUE) {
                String conditionString = whereClauseString.substring(nextIndex,
                        orIndex).trim();
                // add operator and condition.
                conditions.add(new WhereCondition(conditionString));
                conditionOperators.add(new OrConditionOperator());
                nextIndex = orIndex + "OR".length();
            } else {
                break;
            }
        }
        // add the last condition
        conditions.add(new WhereCondition(whereClauseString.substring(nextIndex).trim()));

    }

    List<String> getFieldNames() {
        List<String> fieldNames = new ArrayList<>();
        // Extract the FQFN to assemble the values for the query.
        for (WhereCondition condition: conditions) {
            fieldNames.add(condition.fieldName);
        }
        return fieldNames;
    }
}
