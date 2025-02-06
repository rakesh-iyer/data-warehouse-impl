import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class MessageProcessor {
    Parser parser;

    private MessageProcessor(Parser parser) {
        this.parser = parser;
    }

    int processMessageBytes(String messageType, byte[] bytes,
                            int start, int tabs) {
        String tab = "";
        for (int i = 0; i < tabs; i++) {
            tab = "\t" + tab;
        }
        System.out.println(tab + messageType + " = {");
        List<FieldInfo> fieldInfoList = parser.getFieldInfoList(messageType);
        int i = start;
        ListIterator<FieldInfo> fieldInfoIter = fieldInfoList.listIterator();
        //for (FieldInfo fieldInfo : fieldInfoList) {
        for (;fieldInfoIter.hasNext();) {
            FieldInfo fieldInfo = fieldInfoIter.next();
            switch (fieldInfo.fieldType) {
                case "int":
                    // consume 4 bytes.
                    // using network order i.e. big endian for a change.
                    int intValue =
                            bytes[i] << 24 | bytes[i + 1] << 16 | bytes[i + 2] << 8 | bytes[i + 3];
                    i += 4;
                    System.out.println(tab + "\t" + fieldInfo.fieldName + "=" + intValue);
                    break;
                case "char":
                    char charValue = (char) bytes[i];
                    i++;
                    // consume 1 byte.
                    System.out.println(tab + "\t" + fieldInfo.fieldName + "=" + charValue);
                    break;
                case "string": {
                        byte size = (byte) bytes[i];
                        // we allow a 255 byte long string
                        String stringValue = new String(bytes, i + 1, size);
                        i += size + 1;
                        System.out.println(tab + "\t" + fieldInfo.fieldName + "=" + stringValue);
                    }
                    break;
                default: {
                        if (fieldInfo.fieldType.startsWith("repeated_")) {
                            byte size = (byte) bytes[i];
                            // replace repeated with the actual fieldinfo almost
                            // as if its inlaying it.
                            FieldInfo repeatedFieldInfo =
                                    new FieldInfo(fieldInfo.fieldType.replace(
                                            "repeated_", ""),
                                            fieldInfo.fieldName);
                            fieldInfoIter.remove();
                            for (int j = 0; j < size; j++) {
                                fieldInfoIter.add(repeatedFieldInfo);
                                // We want to process all the added elements.
                                fieldInfoIter.previous();
                            }
                            i++;
                            continue;
                        }
                        int processed = processMessageBytes(fieldInfo.fieldType,
                                bytes, i,
                                tabs + 1);
                        i += processed;
                    }
                    break;
            }
        }
        System.out.println(tab + "}");
        return i - start;
    }

    static MessageProcessor createInstance(Parser parser) {
        return new MessageProcessor(parser);
    }
}
