import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

// need to convert something like
// docid: 10
// Links:
//   Forward: 20
//   Forward: 40
//   Forward: 60

// docid: 20
// Links:
//  Backward: 10
//  Backward: 30
//  Forward: 80

// Links.Forward column
//  value | rl | dl
//   20      0   2
//   40      1   2
//   60      1   2
//   80      0   2
//
// Links.Backward column
//  value | rl | dl
//   null   0    1
//   10     0    2
//   30     1    2


public class RecordWriter {
    void writeRecord(ByteBuffer record, RecordDecoder recordDecoder,
                     FieldWriter fieldWriter,
                     int repetitionLevel) {
        fieldWriter.setRepetitionLevel(repetitionLevel);
        Set<String> seenFieldIds = new HashSet<>();
        for (String fieldName: recordDecoder.getFields()) {
            FieldWriter childFieldWriter =
                    fieldWriter.getChildFieldWriter(fieldName);
            int childRepetitionLevel = repetitionLevel;
            if (seenFieldIds.contains(childFieldWriter.fieldId())) {
                childRepetitionLevel = childFieldWriter.depth();
            } else {
                seenFieldIds.add(childFieldWriter.fieldId());
            }

            if (childFieldWriter.isAtomic()) {
                // We use the ArrayUtils.subarray that beautifully encapsulates
                // the intent of a byte subbuffer.
                // However the subarray is immutable.
                byte[] readData = recordDecoder.readField(record,
                        fieldName);
                childFieldWriter.write(readData);
            } else {
                RecordDecoder childRecordDecoder =
                        recordDecoder.getRecordDecoder(fieldName);
                writeRecord(record, childRecordDecoder, childFieldWriter,
                        childRepetitionLevel);
            }
        }
    }
}


