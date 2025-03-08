import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class SchemaElement {
    Type type;
    int typeLength;
    FieldRepetitionType repetitionType;
    String name;
    int numChilds;
    ConvertedType convertedType;
    SchemaElement(Type type,
                  int typeLength,
                  FieldRepetitionType repetitionType,
                  String name,
                  int numChilds,
                  ConvertedType convertedType) {
        this.type = type;
        this.typeLength = typeLength;
        this.repetitionType = repetitionType;
        this.name = name;
        this.numChilds = numChilds;
        this.convertedType = convertedType;
    }

    void write(DataOutputStream dataOutputStream) throws IOException {
        if (type != null) {
            dataOutputStream.write(type.value);
        } else {
            dataOutputStream.write((byte)0);
        }
        dataOutputStream.writeInt(typeLength);
        dataOutputStream.write(repetitionType.value);
        dataOutputStream.write(name.getBytes());
        dataOutputStream.writeInt(numChilds);
        if (convertedType != null) {
            dataOutputStream.write(convertedType.value);
        } else {
            dataOutputStream.write((byte)0);
        }
    }

    boolean isNested() {
        return numChilds > 0;
    }
}
