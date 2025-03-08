import java.util.Optional;

public class Schema1 {
    public char s1Field1;
    public int s1Field2;
    public Schema2 s1Field3;
    public String[] s1Field4;
    public Optional<Integer> s1Field5;
    public Schema2[] s1Field6;
    public static class Schema2 {
        public char s2Field1;
        public char[] s2Field2;
        Schema2(char s2Field1, char[] s2Field2) {
            this.s2Field1 = s2Field1;
            this.s2Field2 = s2Field2;
        }
    }
    Schema1(char s1Field1, int s1Field2, Schema2 s1Field3, String[] s1Field4,
     Optional<Integer> s1Field5, Schema2[] s1Field6) {
        this.s1Field1 = s1Field1;
        this.s1Field2 = s1Field2;
        this.s1Field3 = s1Field3;
        this.s1Field4 = s1Field4;
        this.s1Field5 = s1Field5;
        this.s1Field6 = s1Field6;
    }
}
