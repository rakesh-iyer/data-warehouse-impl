import java.nio.ByteBuffer;
import java.util.*;


//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class DwHouse {
    byte[] record1 = {
            /*s1Field1=*/0x47,
            /*s1Field2=*/0x02, 0x03, 0x04, 0x05,
            /*s1Field3.s2Field1=*/0x46,
            /*s1Field3.s2Field2=*/0x03, 0x48, 0x49, 0x4A,
            /*s1Field4[]=*/0x02,
            /*s1Field4[0]=*/0x03, 0x41, 0x42, 0x43,
            /*s1Field4[1]=*/0x04, 0x41, 0x42, 0x43, 0x44,
            /*s1Field5=*/0x01, 0x00, 0x02, 0x3, 0x4, 0x5,
            /*s1Field6[]=*/0x02,
            /*s1Field6[0].s2Field1=*/0x45,
            /*s1Field6[0].s2Field2=*/0x03, 0x48, 0x49, 0x4A,
            /*s1Field6[1].s2Field1=*/0x47,
            /*s1Field6[1].s2Field2=*/0x03, 0x48, 0x49, 0x4A};
    byte[] record2 = {
            /*s1Field1=*/0x48,
            /*s1Field2=*/0x20, 0x30, 0x40, 0x050,
            /*s1Field3.s2Field1=*/0x46,
            /*s1Field3.s2Field2=*/0x03, 0x51, 0x52, 0x53,
            /*s1Field4[]=*/0x02,
            /*s1Field4[0]=*/0x03, 0x49, 0x50, 0x51,
            /*s1Field4[1]=*/0x04, 0x45, 0x48, 0x51, 0x54,
            /*s1Field5=*/0x01, 0x00, 0x20, 0x30, 0x40, 0x50,
            /*s1Field6[]=*/0x02,
            /*s1Field6[0].s2Field1=*/0x45,
            /*s1Field6[0].s2Field2=*/0x03, 0x59, 0x60, 0x61,
            /*s1Field6[1].s2Field1=*/0x45,
            /*s1Field6[1].s2Field2=*/0x03, 0x56, 0x57, 0x58};
    byte[] record3 = {
            /*s1Field1=*/0x47,
            /*s1Field2=*/0x02, 0x03, 0x04, 0x05,
            /*s1Field3.s2Field1=*/0x46,
            /*s1Field3.s2Field2=*/0x03, 0x54, 0x55, 0x56,
            /*s1Field4[]=*/0x02,
            /*s1Field4[0]=*/0x03, 0x41, 0x42, 0x43,
            /*s1Field4[1]=*/0x04, 0x43, 0x45, 0x47, 0x49,
            /*s1Field5=*/0x01, 0x00, 0x20, 0x30, 0x40, 0x50,
            /*s1Field6[]=*/0x02,
            /*s1Field6[0].s2Field1=*/0x45,
            /*s1Field6[0].s2Field2=*/0x03, 0x62, 0x63, 0x64,
            /*s1Field6[1].s2Field1=*/0x45,
            /*s1Field6[1].s2Field2=*/0x03, 0x56, 0x57, 0x58};

    void setupData() {
        RecordWriter recordWriter = new RecordWriter();
        // write a single record, lets walk it through my man.
        // the null optional write does not fail, but it needs proper addressal.
        // Create the root field writer.
        FieldWriter rootFieldWriter = new FieldWriter(Schema1.class, "",
                "", 0);
        RecordDecoder recordDecoder = new RecordDecoder(Schema1.class);
        try {
            ByteBuffer recordBuffer1 = ByteBuffer.wrap(record1);
            recordWriter.write(recordBuffer1, recordDecoder, rootFieldWriter,
                    0);
            ByteBuffer recordBuffer2 = ByteBuffer.wrap(record2);
            recordWriter.write(recordBuffer2, recordDecoder, rootFieldWriter,
                    0);
            ByteBuffer recordBuffer3 = ByteBuffer.wrap(record3);
            recordWriter.write(recordBuffer3, recordDecoder, rootFieldWriter,
                    0);
            rootFieldWriter.flush();
        } catch (Exception e) {
            System.out.println("recordProcessor writing caused an exception " + e);
        }
    }

    void test2() {
        RecordWriter recordWriter = new RecordWriter();
        // write a single record, lets walk it through my man.
        byte[] recordBuffer = {0x47, 0x02, 0x03, 0x04, 0x05, 0x46, 0x03, 0x08,
                0x09, 0x0A, 0x02, 0x03, 0x41, 0x42, 0x43, 0x04, 0x41, 0x42,
                0x43, 0x44, 0x01, 0x00, 0x01, 0x02, 0x03, 0x04, 0x02, 0x45,
                0x03, 0x08, 0x09, 0xA, 0x45, 0x03, 0x08, 0x09, 0xA};
        ByteBuffer record = ByteBuffer.wrap(recordBuffer);
        // Create the root field writer.
        FieldWriter rootFieldWriter = new FieldWriter(Schema1.class, "root",
                "root",
                0);
        RecordDecoder recordDecoder = new RecordDecoder(Schema1.class);
        try {
            recordWriter.write(record, recordDecoder, rootFieldWriter, 0);
            rootFieldWriter.flush();
        } catch (Exception e) {
            System.out.println("recordProcessor writing caused an exception " + e);
        }
    }

    void test3() {
        RecordWriter recordWriter = new RecordWriter();
        // write a single record, lets walk it through my man.
        // the null optional write does not fail, but it needs proper addressal.
        byte[] recordBuffer = {0x47, 0x02, 0x03, 0x04, 0x05, 0x46, 0x03, 0x08,
                0x09, 0x0A, 0x02, 0x03, 0x41, 0x42, 0x43, 0x04, 0x41, 0x42,
                0x43, 0x44, 0x00, 0x02, 0x45,
                0x03, 0x08, 0x09, 0xA, 0x45, 0x03, 0x08, 0x09, 0xA};
        ByteBuffer record = ByteBuffer.wrap(recordBuffer);
        // Create the root field writer.
        FieldWriter rootFieldWriter = new FieldWriter(Schema1.class, "root",
         "root", 0);
        RecordDecoder recordDecoder = new RecordDecoder(Schema1.class);
        try {
            recordWriter.write(record, recordDecoder, rootFieldWriter, 0);
            rootFieldWriter.flush();
        } catch (Exception e) {
            System.out.println("recordProcessor writing caused an exception " + e);
        }
    }

    void test4() {
        RecordWriter recordWriter = new RecordWriter();
        // write a single record, lets walk it through my man.
        // the null optional write does not fail, but it needs proper addressal.
        byte[] recordBuffer1 = {
                /*s1Field1=*/0x47,
                /*s1Field2=*/0x02, 0x03, 0x04, 0x05,
                /*s1Field3.s2Field1=*/0x46,
                /*s1Field3.s2Field2=*/0x03, 0x08, 0x09, 0x0A,
                /*s1Field4[]=*/0x02,
                /*s1Field4[0]=*/0x03, 0x41, 0x42, 0x43,
                /*s1Field4[1]=*/0x04, 0x41, 0x42, 0x43, 0x44,
                /*s1Field5=*/0x01, 0x00, 0x02, 0x3, 0x4, 0x5,
                /*s1Field6[]=*/0x02,
                /*s1Field6[0].s2Field1=*/0x45,
                /*s1Field6[0].s2Field2=*/0x03, 0x08, 0x09, 0xA,
                /*s1Field6[1].s2Field1=*/0x45,
                /*s1Field6[1].s2Field2=*/0x03, 0x08, 0x09, 0xA};
        byte[] recordBuffer2 = {
                /*s1Field1=*/0x47,
                /*s1Field2=*/0x02, 0x03, 0x04, 0x05,
                /*s1Field3.s2Field1=*/0x46,
                /*s1Field3.s2Field2=*/0x03, 0x08, 0x09, 0x0A,
                /*s1Field4[]=*/0x02,
                /*s1Field4[0]=*/0x03, 0x41, 0x42, 0x43,
                /*s1Field4[1]=*/0x04, 0x41, 0x42, 0x43, 0x44,
                /*s1Field5=*/0x01, 0x00, 0x02, 0x3, 0x4, 0x5,
                /*s1Field6[]=*/0x02,
                /*s1Field6[0].s2Field1=*/0x45,
                /*s1Field6[0].s2Field2=*/0x03, 0x08, 0x09, 0xA,
                /*s1Field6[1].s2Field1=*/0x45,
                /*s1Field6[1].s2Field2=*/0x03, 0x08, 0x09, 0xA};
        // Create the root field writer.
        FieldWriter rootFieldWriter = new FieldWriter(Schema1.class, "root",
         "root", 0);
        RecordDecoder recordDecoder = new RecordDecoder(Schema1.class);
        try {
            ByteBuffer record1 = ByteBuffer.wrap(recordBuffer1);
            recordWriter.write(record1, recordDecoder, rootFieldWriter, 0);
            ByteBuffer record2 = ByteBuffer.wrap(recordBuffer2);
            recordWriter.write(record2, recordDecoder, rootFieldWriter, 0);
            rootFieldWriter.flush();
        } catch (Exception e) {
            System.out.println("recordProcessor writing caused an exception " + e);
        }
    }

    void test5() {
        RecordWriter recordWriter = new RecordWriter();
        // write a single record, lets walk it through my man.
        // the null optional write does not fail, but it needs proper addressal.
        // Create the root field writer.
        FieldWriter rootFieldWriter = new FieldWriter(Schema1.class, "root",
                "root", 0);
        RecordDecoder recordDecoder = new RecordDecoder(Schema1.class);
        try {
            ByteBuffer recordBuffer1 = ByteBuffer.wrap(record1);
            recordWriter.write(recordBuffer1, recordDecoder, rootFieldWriter,
                    0);
            ByteBuffer recordBuffer2 = ByteBuffer.wrap(record2);
            recordWriter.write(recordBuffer2, recordDecoder, rootFieldWriter,
                    0);
            ByteBuffer recordBuffer3 = ByteBuffer.wrap(record3);
            recordWriter.write(recordBuffer3, recordDecoder, rootFieldWriter,
                    0);
            rootFieldWriter.flush();
        } catch (Exception e) {
            System.out.println("recordProcessor writing caused an exception " + e);
        }

        try {
            RecordField recordField = new RecordField(Schema1.class, "root",
                    "root", 0, 0, 0, /*repeated=*/false);
            FieldReader.setRootFieldReader(recordField.getFieldReader());
            RecordField.setRootRecordField(recordField);
            List<FieldReader> fieldReaders =
                    recordField.getLeafFieldReaders();
            List<RecordField> fields = recordField.getLeafFields();
            FiniteStateMachine finiteStateMachine =
                    new FiniteStateMachine(fields);
            RecordAssembler recordAssembler = new RecordAssembler();
            recordAssembler.assembleRecords(fieldReaders, Schema1.class);
        } catch (Exception e) {
            System.out.println("Record reading caused an exception " + e);
        }
    }

    void test6() {
        setupData();
        try {
            RecordField recordField = new RecordField(Schema1.class, "root",
                    "root", 0, 0, 0, /*repeated=*/false);
            FieldReader.setRootFieldReader(recordField.getFieldReader());
            RecordField.setRootRecordField(recordField);
            List<RecordField> fields = recordField.getLeafFields();
            Set<String> selectedFields = new HashSet<>();
            selectedFields.addAll(Arrays.asList("root-s1Field1", "root" +
                    "-s1Field3-s2Field2", "root-s1Field6-s2Field2"));
            for (Iterator<RecordField> iter = fields.iterator();
                iter.hasNext();) {
                RecordField field = (RecordField)iter.next();
                if (!selectedFields.contains(field.getFullyQualifiedName())) {
                    iter.remove();
                }
            }
            List<FieldReader> fieldReaders = new ArrayList<>();
            for (RecordField field: fields) {
                fieldReaders.add(field.fieldReader);
            }
            FiniteStateMachine finiteStateMachine =
                    new FiniteStateMachine(fields);
            RecordAssembler recordAssembler = new RecordAssembler();
            recordAssembler.assembleRecords(fieldReaders, Schema1.class);
        } catch (Exception e) {
            System.out.println("Record reading caused an exception " + e);
        }
    }

    void test7() {
        SqlParser sqlParser = new SqlParser();
        try {
            SqlComponents components = sqlParser.getSqlComponents(
                    "Select Field1 " +
                    "From table1 Where " +
                    "Field1=100 AND Field2=200 OR Field3=300 AND Field4=400");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    void test8() {
        setupData();
        RecordField recordField = new RecordField(Schema1.class, "",
                "", 0, 0, 0, /*repeated=*/false);
        FieldReader.setRootFieldReader(recordField.getFieldReader());
        RecordField.setRootRecordField(recordField);
        List<RecordField> fields = recordField.getLeafFields();
        Set<FieldReader> fieldReaders = new HashSet<>();
        Set<String> selectedFieldNames = Set.of("s1Field1", "s1Field2",
                "s1Field3-s2Field2", "s1Field6-s2Field2");
        for (RecordField field: fields) {
            if (selectedFieldNames.contains(field.getFullyQualifiedName())) {
                fieldReaders.add(field.fieldReader);
            }
        }
        Aggregation aggregation = new Aggregation(fieldReaders);
        try {
            aggregation.evaluate(String.format(
                    "SELECT %s FROM table1 WHERE " +
                            "s1Field1=G AND " +
                            "s1Field2=84148994",
                    "s1Field1,s1Field3-s2Field2," +
                            "s1Field6-s2Field2"));
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void main(String[] args) {
        DwHouse dwHouse = new DwHouse();
        dwHouse.test8();
    }
}