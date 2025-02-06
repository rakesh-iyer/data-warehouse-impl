import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        Parser parser = new Parser();
        MessageProcessor messageProcessor =
                MessageProcessor.createInstance(parser);
        byte [] bytes = {0x47, 0x02, 0x03, 0x04, 0x05, 0x46, 0x07, 0x08, 0x09
                , 0x0A, 0x02, 0x03, 0x41, 0x42, 0x43, 0x04, 0x41, 0x42, 0x43,
                0x44 };
        String testStruct = "message msg2 { char field1; int field2; }";
        String testStruct1 = "message msg { char field1; int field2; " +
                "msg2 message2; repeated string field4;" +
                " }";
        try {
            parser.addMessageFieldInfo(testStruct);
            parser.addMessageFieldInfo(testStruct1);
            messageProcessor.processMessageBytes("msg", bytes, 0, 0);
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}