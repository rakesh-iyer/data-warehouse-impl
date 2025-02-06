import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    Map<String, List<FieldInfo>> fieldInfoListMap = new HashMap<>();

    List<FieldInfo> getFieldInfoList(String messageType) {
        return fieldInfoListMap.get(messageType);
    }

    List<FieldInfo> parseFieldInfoList(String fields) throws Exception {
        List<FieldInfo> fieldInfoList = new ArrayList<>();
        for (String field : fields.split(";")) {
            // split the type from the field name.
            String[] splitField = field.trim().split(" ");
            FieldInfo fieldInfo;
            if (splitField.length == 2) {
                fieldInfo = new FieldInfo(splitField[0],
                        splitField[1]);
            } else if (splitField.length == 3 && splitField[0].equals(
                    "repeated")) {
                fieldInfo = new FieldInfo(splitField[0] + "_" + splitField[1],
                        splitField[2]);
            } else {
                throw new Exception("Could not parse the field info list.");
            }
            fieldInfoList.add(fieldInfo);
        }
        return fieldInfoList;
    }

    void addMessageFieldInfo(String messageDef) throws Exception {
        Pattern pattern = Pattern.compile("message (\\w+)\s+\\{ ([\\w\\s;]+)" +
                " " +
                "\\}");
        Matcher matcher = pattern.matcher(messageDef);
        if (matcher.matches()) {
            // we can find the struct name and what the body is like
            String structName = matcher.group(1);
            String fields = matcher.group(2);
            List<FieldInfo> fieldInfoList = parseFieldInfoList(fields);
            fieldInfoListMap.put(structName, fieldInfoList);
        } else {
            throw new Exception("Could not parse the message");
        }
    }

}
