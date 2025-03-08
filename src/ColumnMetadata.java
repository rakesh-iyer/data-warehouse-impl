import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

public class ColumnMetadata {
    Type type;
    List<Encoding> encodings; // why list of encodings?
    List<String> pathInSchema; // interesting we store this.
    CompressionCodec compressionCodec;
    int numValues;
    int totalUncompressedSize;
    int totalCompressedSize;
    List<KeyValue> keyValueMetadata = new ArrayList<>();
    int firstDataPageOffset;
    int firstIndexPageOffset;
    int dictionaryPageOffset;
    Map<Type, Integer> typeNumBytesMap = Map.ofEntries(
            Map.entry(Type.Int32, 4),
            Map.entry(Type.Int64, 8),
            Map.entry(Type.Boolean, 1),
            Map.entry(Type.Float, 4),
            Map.entry(Type.Double, 8),
            // Lets set this up by default.
            Map.entry(Type.ByteArray, 1),
            Map.entry(Type.FixedLengthByteArray, 1)
    );

    ColumnMetadata(Type type, String pathInSchema) {
        this.type = type;
        this.pathInSchema = Arrays.asList(pathInSchema.split("."));
        this.compressionCodec = CompressionCodec.Uncompressed;
    }


    void write(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.write(type.value);
        dataOutputStream.write(compressionCodec.value);
        dataOutputStream.writeInt(numValues);
        dataOutputStream.writeInt(totalUncompressedSize);
        dataOutputStream.writeInt(totalCompressedSize);
        for (KeyValue keyValue: keyValueMetadata) {
            dataOutputStream.write(keyValue.key.getBytes());
            dataOutputStream.write(keyValue.value.getBytes());
        }
        dataOutputStream.writeInt(firstDataPageOffset);
        dataOutputStream.writeInt(firstIndexPageOffset);
        dataOutputStream.writeInt(dictionaryPageOffset);
    }

    void updateSizes(int size) {
        totalUncompressedSize += size;
        totalCompressedSize += size;
    }

    int getSize() {
        return typeNumBytesMap.get(type);
    }

    void addValue() {
    }
}
