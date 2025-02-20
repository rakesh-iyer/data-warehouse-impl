import java.util.Optional;

public class Schema1 {
    public char s1Field1;
    public int s1field2;
    public Schema2 s1Field3;
    public String[] s1Field4;
    public Optional<Integer> s1Field5;
    public Schema2[] s1Field6;
    public static class Schema2 {
        public char s2Field1;
        public char[] s2Field2;
    }
}
