
import java.util.*;

// An Aggregation is modelled as a state machine.
// evaluate with evaluate the sql expression.
// fetch will get the values.
public class Aggregation {
    int fetchLevel;
    Set<FieldReader> fieldReaders;
    static class RepeatedValue {
        boolean done;
        List<Object> values = new ArrayList<>();

        void addValue(Object value) {
            values.add(value);
        }

        void markDone() {
            done = true;
        }

        boolean isDone() {
            return done;
        }

        Object getFirst() {
            return values.get(0);
        }

        public String toString() {
            return values.toString();
        }
    }

    Map<String, Object> fieldValues;

    Aggregation(Set<FieldReader> fieldReaders) {
        this.fieldReaders = fieldReaders;
    }

    void evaluate(String sqlExpression) throws Exception {
        int selectLevel = 0;
        SqlParser sqlParser = new SqlParser();
        SqlComponents sqlComponents = sqlParser.getSqlComponents(sqlExpression);
        fieldValues = new HashMap<>();
        while (fetchFieldValues()) {
            // Where clauses do not typically support aggregates e.g. filter
            // records where Min(repeated_field) > 10.
            // Having is an extension to support this.
            if (sqlComponents.whereClause.evaluate(fieldValues)) {
                for (ScalarExpression scalarExpression :
                        sqlComponents.selectClause.getScalarExpressions()) {
                    if (scalarExpression.getRepetitionLevel() >= selectLevel) {
                        if (scalarExpression.isReady(fieldValues) && !scalarExpression.isEvaluated()) {
                            Object value =
                                    scalarExpression.evaluate(fieldValues);
                            System.out.println(String.format("%s:%s",
                                    scalarExpression, value));
                        }
                    }
                }
                selectLevel = fetchLevel;
            } else {
                selectLevel = Math.min(selectLevel, fetchLevel);
            }
            // This implies previous "row" of field values are consumed.
            if (fetchLevel == 0) {
                fieldValues.clear();
                sqlComponents.selectClause.clearScalarExpressionEvaluations();
            }
        }
    }

    // This is a fascinating algorithm.
    // We go from nesting level 0 to the maximum, and then pop back up to
    // lower levels until we have consumed all the field values.
    // For repeated fields we add the notion of done so that we only evaluate
    // in the higher layers when we are indeed done accumulating the entire
    // repeated field value.
    boolean fetchFieldValues() {
        int nextLevel = 0;
        for (FieldReader fieldReader: fieldReaders) {
            RecordField recordField = fieldReader.recordField;
            String fullyQualifiedName = recordField.getFullyQualifiedName();
            if (fieldReader.getRepetitionLevel() >= fetchLevel) {
                // This may not be the best proxy to stop the fetching.
                if (!fieldReader.hasData()) {
                    return false;
                }
                Object value = fieldReader.getDataAndMove();
                if (recordField.repeated) {
                    RepeatedValue repeatedValue =
                            (RepeatedValue)fieldValues.get(fullyQualifiedName);
                    if (repeatedValue == null) {
                        repeatedValue = new RepeatedValue();
                        fieldValues.put(fullyQualifiedName, repeatedValue);
                    }
                    repeatedValue.addValue(value);
                } else {
                    fieldValues.put(recordField.fullyQualifiedName, value);
                }
            }
            // We are done retrieving the repeated value.
            if (recordField.repeated && fieldReader.getRepetitionLevel() == 0) {
                RepeatedValue repeatedValue =
                        (RepeatedValue)fieldValues.get(fullyQualifiedName);
                repeatedValue.markDone();
            }
            nextLevel = Math.max(nextLevel,
                    fieldReader.getRepetitionLevel());
        }
        fetchLevel = nextLevel;
        return true;
    }
}
