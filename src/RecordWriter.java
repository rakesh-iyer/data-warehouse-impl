import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

public class RecordWriter {
    void write(ByteBuffer record, RecordDecoder recordDecoder,
               FieldWriter fieldWriter,
               int repetitionLevel) throws Exception {
        fieldWriter.addRepDefLevel(repetitionLevel);
        Set<String> seenFieldIds = new HashSet<>();
        recordDecoder.initialize(record);
        while (recordDecoder.hasData()) {
            RecordDecoder.DecodedField decodedField = recordDecoder.getData();
            FieldWriter childFieldWriter =
                    fieldWriter.getChildFieldWriter(decodedField.fieldName);
            int childRepetitionLevel = repetitionLevel;
            if (seenFieldIds.contains(childFieldWriter.fieldId())) {
                // what is this tree depth that is being read.
                // The child repetition level chRepetitionLevel is set to that
                // of the most recently repeated field or else defaults to
                // its parent’s level.
                childRepetitionLevel = childFieldWriter.depth();
            } else {
                seenFieldIds.add(childFieldWriter.fieldId());
            }

            if (childFieldWriter.isAtomic()) {
                childFieldWriter.write(decodedField.fieldValue,
                        childRepetitionLevel);
            } else {
                RecordDecoder childRecordDecoder =
                        recordDecoder.getRecordDecoder(decodedField.fieldName);
                write(record, childRecordDecoder, childFieldWriter,
                        childRepetitionLevel);
            }
        }
    }
}
