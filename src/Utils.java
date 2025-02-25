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

    static boolean isTypeNested(Class type) {
        return !(isTypeOptional(type) || isTypeAtomic(type) || isTypeRepeated(type));
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
}
