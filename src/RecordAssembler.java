import java.util.ArrayList;
import java.util.List;

public class RecordAssembler {
    FieldReader lastReader;
    Object assembleRecord(List<FieldReader> readers, Class schema) throws Exception {
        Record record = new Record();
        lastReader = FieldReader.getRootFieldReader();
        FieldReader reader = readers.getFirst();
        if (!reader.hasData()) {
            System.out.println("Records are finished. Exiting.");
            return null;
        }

        // The state machine hides all the complexities involved.
        // We only use move to level algorithm.
        // Its not clear if return to level is really necessary.
        while (reader.hasData()) {
            Object currentValue = reader.getDataAndMove();
            int nextRepetitionLevel = reader.getRepetitionLevel();
            if (currentValue != null) {
                moveToLevel(reader, record);
                record.addValue(reader.getFieldName(), currentValue);
            } else {
                moveToLevel(reader, record);
            }
            // We let the next value access to be encapsulated in the next
            // field reader computation.
            reader = reader.nextFieldReader(nextRepetitionLevel);
            if (reader == null) {
                // We have reached the end of the record.
                break;
            }
            //    returnToLevel(reader, record, reader.treeLevel());
        }
        returnToLevel(FieldReader.getRootFieldReader(), record);
        String recordString = record.finish();
        System.out.println(String.format("Record is as follows:\n%s\n",
                recordString));
        return recordString;
    }

    List<Object> assembleRecords(List<FieldReader> fieldReaders,
                                 Class schema) throws Exception {
        List<Object> records = new ArrayList<>();
        Object record;
        while ((record = assembleRecord(fieldReaders, schema)) != null) {
            records.add(record);
        }
        return records;
    }

    void moveToLevel(FieldReader nextReader, Record record) {
        RecordField lcaField =
                FieldReader.getRootFieldReader().getRecordField().lowestCommonAncestor(
                        lastReader.getRecordField(), nextReader.getRecordField()).lcaRecordField;
        // If the LCA is the parent for both last and current field then skip
        // processing the nesting logic. Lets optimize this later.
            record.endNestedRecords(lcaField, lastReader.getRecordField());
            record.startNestedRecords(lcaField, nextReader.getRecordField());
        lastReader = nextReader;
    }

    void returnToLevel(FieldReader nextReader, Record record) {
        RecordField lcaField =
                RecordField.getRootRecordField().lowestCommonAncestor(
                        lastReader.getRecordField(), nextReader.getRecordField()).lcaRecordField;
        record.endNestedRecords(lcaField, lastReader.getRecordField());
        lastReader = nextReader;
    }
}
