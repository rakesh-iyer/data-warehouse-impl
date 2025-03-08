import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.nio.ByteBuffer;
import java.util.Optional;

public class Utils {
    static boolean isTypeOptional(Class type) {
        return type == Optional.class;
    }

    static boolean isTypeAtomic(Class type) {
        return type == int.class || type == char.class || type == String.class;
    }

    static boolean isTypeRepeated(Class type) {
        return type.isArray();
    }

    static boolean isTypeRepeatedAtomic(Class type) {
        return type.isArray() && isTypeAtomic(type.componentType());
    }

    static boolean isTypeNested(Class type) {
        return !(isTypeOptional(type) || isTypeAtomic(type) || isTypeRepeatedAtomic(type));
    }

    static Character parseCharacter(String input) throws Exception {
        if (input.length() != 1) {
            throw new Exception("Not a character.");
        }
        return input.charAt(0);
    }

    static String constructFullyQualifiedChildFieldName(String fullyQualifiedName,
                                                 String childFieldName) {
        if (fullyQualifiedName.isEmpty()) {
            return childFieldName;
        } else {
            return String.format(
                    "%s-%s",
                    fullyQualifiedName, childFieldName);
        }
    }

    static byte[] getBytes(ByteBuffer byteBuffer) {
        byte[] serializedData = new byte[byteBuffer.position()];
        byteBuffer.flip();
        byteBuffer.get(serializedData);
        return serializedData;
    }

    static void printObject(Object object) {
        System.out.println(ReflectionToStringBuilder.toString(object,
                ToStringStyle.MULTI_LINE_STYLE));
    }

    static ByteBuffer getSmallByteBuffer() {
        return ByteBuffer.allocate(100);
    }
}
