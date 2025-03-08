// We use Little Endian scheme for encoding.
// When deserializing from byte[] use unsigned conversions to remove effects of
// signed bits.
public class PlainEncoder {
    byte[] intToByteArray(int intValue) {
        byte[] intBuffer = new byte[4];
        intBuffer[0] = (byte)(intValue & 0xff);
        intBuffer[1] = (byte)(intValue >> 8 & 0xff);
        intBuffer[2] = (byte)(intValue >> 16 & 0xff);
        intBuffer[3] = (byte)(intValue >> 24 & 0xff);
        return intBuffer;
    }

    int makeUnsignedInt(byte b) {
        return (int)b & 0xff;
    }

    int byteArrayToInt(byte[] valueBuffer, int offset) {
        int intValue = makeUnsignedInt(valueBuffer[offset]) +
                (makeUnsignedInt(valueBuffer[offset+1]) << 8) +
                (makeUnsignedInt(valueBuffer[offset+2]) << 16) +
                (makeUnsignedInt(valueBuffer[offset+3]) << 24);
        return intValue;
    }

    byte[] longToByteArray(long longValue) {
        byte[] longBuffer = new byte[8];
        longBuffer[0] = (byte)(longValue & 0xff);
        longBuffer[1] = (byte)(longValue >> 8 & 0xff);
        longBuffer[2] = (byte)(longValue >> 16 & 0xff);
        longBuffer[3] = (byte)(longValue >> 24 & 0xff);
        longBuffer[4] = (byte)(longValue >> 32 & 0xff);
        longBuffer[5] = (byte)(longValue >> 40 & 0xff);
        longBuffer[6] = (byte)(longValue >> 48 & 0xff);
        longBuffer[7] = (byte)(longValue >> 56 & 0xff);
        return longBuffer;
    }

    long makeUnsignedLong(long b) {
        return (long)b & 0xff;
    }

    long byteArrayToLong(byte[] valueBuffer, int offset) {
        long longValue = makeUnsignedLong(valueBuffer[offset]) +
                (makeUnsignedLong(valueBuffer[offset+1]) << 8) +
                (makeUnsignedLong(valueBuffer[offset+2]) << 16) +
                (makeUnsignedLong(valueBuffer[offset+3]) << 24) +
                (makeUnsignedLong(valueBuffer[offset+4]) << 32) +
                (makeUnsignedLong(valueBuffer[offset+5]) << 40) +
                (makeUnsignedLong(valueBuffer[offset+6]) << 48) +
                (makeUnsignedLong(valueBuffer[offset+7]) << 56);
        return longValue;
    }

    byte[] floatToByteArray(float value){
        byte[] floatBuffer = new byte[4];
        Integer floatRep = Float.floatToIntBits(value);
        floatBuffer[0] = (byte)(floatRep & 0xff);
        floatBuffer[1] = (byte)(floatRep >> 8 & 0xff);
        floatBuffer[2] = (byte)(floatRep >> 16 & 0xff);
        floatBuffer[3] = (byte)(floatRep >> 24 & 0xff);
        return floatBuffer;
    }

    float byteArrayToFloat(byte[] valueBuffer, int offset) {
        int floatIntBitsValue = (int)valueBuffer[offset] +
                ((int)valueBuffer[offset+1] << 8) +
                ((int)valueBuffer[offset+2] << 16) +
                ((int)valueBuffer[offset+3] << 24);
        return Float.intBitsToFloat(floatIntBitsValue);
    }

    byte[] doubleToByteArray(double doubleValue) {
        byte[] doubleBuffer = new byte[4];
        Long doubleRep = Double.doubleToLongBits(doubleValue);
        doubleBuffer[0] = (byte)(doubleRep & 0xff);
        doubleBuffer[1] = (byte)(doubleRep >> 8 & 0xff);
        doubleBuffer[2] = (byte)(doubleRep >> 16 & 0xff);
        doubleBuffer[3] = (byte)(doubleRep >> 24 & 0xff);
        doubleBuffer[4] = (byte)(doubleRep >> 32 & 0xff);
        doubleBuffer[5] = (byte)(doubleRep >> 40 & 0xff);
        doubleBuffer[6] = (byte)(doubleRep >> 48 & 0xff);
        doubleBuffer[7] = (byte)(doubleRep >> 56 & 0xff);
        return doubleBuffer;
    }

    double byteArrayToDouble(byte[] valueBuffer, int offset) {
        long doubleLongBitsValue = (long)valueBuffer[offset] +
                ((long)valueBuffer[offset+1] << 8) +
                ((long)valueBuffer[offset+2] << 16) +
                ((long)valueBuffer[offset+3] << 24) +
                ((long)valueBuffer[offset+4] << 32) +
                ((long)valueBuffer[offset+5] << 40) +
                ((long)valueBuffer[offset+6] << 48) +
                ((long)valueBuffer[offset+7] << 56);
        return Double.longBitsToDouble(doubleLongBitsValue);
    }

    byte[] encode(Object value, Type type) {
        switch (type) {
            case Type.Boolean:
                byte[] booleanBuffer = new byte[1];
                booleanBuffer[0] = (byte)value;
                return booleanBuffer;
            case Type.Int32:
                return intToByteArray((Integer)value);
            case Type.Int64:
                return longToByteArray((Long)value);
            case Type.Float:
                return floatToByteArray((Float)value);
            case Type.Double:
                return doubleToByteArray((Double)value);
            case Type.ByteArray:
                String stringValue = (String)value;
                byte[] lengthBuffer = intToByteArray(stringValue.length());
                byte[] stringBuffer =
                        new byte[stringValue.length()+lengthBuffer.length];
                System.arraycopy(lengthBuffer, 0, stringBuffer, 0,
                        lengthBuffer.length);
                System.arraycopy(stringValue.getBytes(), 0, stringBuffer,
                        lengthBuffer.length, stringValue.length());
                return stringBuffer;
            // This is only used for characters atm.
            case Type.FixedLengthByteArray:
                byte[] charBuffer = new byte[1];
                charBuffer[0] = (byte)((Character)value).charValue();
                return charBuffer;
        }
        return new byte[0];
    }

    Object decode(byte[] valueBuffer, int offset, Type type) {
        switch (type) {
            case Type.Boolean:
                boolean booleanValue = valueBuffer[offset] != 0;
                return booleanValue;
            case Type.Int32:
                // Easy to ignore parentheses around the shift and add ops.
                return byteArrayToInt(valueBuffer, offset);
            case Type.Int64:
                return byteArrayToLong(valueBuffer, offset);
            case Type.Float:
                return byteArrayToFloat(valueBuffer, offset);
            case Type.Double:
                return byteArrayToDouble(valueBuffer, offset);
            case Type.ByteArray:
                // TBA:: We can verify the length if needed.
                int stringLength = byteArrayToInt(valueBuffer, 0);
                return new String(valueBuffer, 4, stringLength);
                // This is only used for Character for now.
            case Type.FixedLengthByteArray:
                Character charValue = (char)valueBuffer[offset];
                return charValue;
        }
        return null;
    }
}
