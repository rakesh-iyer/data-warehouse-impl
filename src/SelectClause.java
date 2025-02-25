import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SelectClause {
    List<String> fieldNames;
    List<ScalarExpression> scalarExpressions = new ArrayList<>();

    SelectClause(String selectClauseString) {
        String fields[] = selectClauseString.split(",");
        fieldNames = Arrays.asList(fields);
        for (String fieldName: fieldNames) {
            scalarExpressions.add(new ScalarExpression(fieldName));
        }
    }

    // We only support field names atm.
    List<ScalarExpression> getScalarExpressions() {
        return scalarExpressions;
    }

    void clearScalarExpressionEvaluations() {
        for (ScalarExpression scalarExpression: scalarExpressions) {
            scalarExpression.clearEvaluated();
        }
    }
}
