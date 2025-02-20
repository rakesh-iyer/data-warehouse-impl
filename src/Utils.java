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
}
