import java.util.List;

public class FiniteStateMachine {
    // The algorithm takes as input the fields that should be populated
    // in the records,in the order in which they appear in the schema.
    FiniteStateMachine(List<RecordField> recordFields) {
        try {
            for (int i = 0; i < recordFields.size(); i++) {
                RecordField recordField = recordFields.get(i);
                int maxRepetitionLevel = recordField.getMaxRepetitionLevel();
                RecordField barrier = (i + 1 < recordFields.size()) ?
                        recordFields.get(i + 1) : null; // we use root as
                // the final state just to experiment.
                int barrierLevel = commonRepetitionLevel(recordField, barrier);
                // Add self-links for all use cases after the barrier.
                // These maybe overridden with appropriate fields based on
                // schema and selected fields.
                for (int j = barrierLevel+1; j <= maxRepetitionLevel; j++) {
                    recordField.setTransition(j, recordField);
                }

                // Check all the previous fields to see if there should be a
                // transition to them.
                // This occurs if their repetition level is a value higher than
                // the barrier level. This represents that whenever the next value's
                // RL matches it the next state would be chosen to be to go back
                // to one of those fields.
                // I have used the recordfield as its own prerecordfield, to
                // address the bootstrap scenarios.
                for (int j = i-1; j >= 0; j--) {
                    RecordField preRecordField = recordFields.get(j);
                    if (preRecordField.getMaxRepetitionLevel() <= barrierLevel) {
                        continue;
                    }
                    int backLevel = commonRepetitionLevel(preRecordField,
                            recordField);
                    recordField.setTransition(backLevel, preRecordField);
                }

                // Fill in the gaps from the previous values.
                // Implicitly atleast one of the pre Fields need to be present
                // for this to work.
                for (int level = barrierLevel + 1; level <= maxRepetitionLevel; level++) {
                    if (recordField.getTransition(level) == null) {
                        recordField.setTransition(level,
                                recordField.getTransition(level - 1));
                    }
                }

                for (int level = 0; level < barrierLevel + 1; level++) {
                    recordField.setTransition(level, barrier);
                }
            }
        } catch (Throwable t) {
          System.out.println(t);
        }
    }

    int commonRepetitionLevel(RecordField field1, RecordField field2) {
        if (field1 == null || field2 == null) {
            return 0;
        }
        RecordField rootRecordField = RecordField.getRootRecordField();
        RecordField.MatchResult result =
                rootRecordField.lowestCommonAncestor(field1, field2);
        return result.lcaRecordField.maxRepetitionLevel;
    }
}
