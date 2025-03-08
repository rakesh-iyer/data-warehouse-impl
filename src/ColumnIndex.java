import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ColumnIndex {
    Type type;
    PlainEncoder encoder;
    // The following are all per-page lists.
    //
    // null pages are used to indicate the validity of the min and max values.
    // In memory we will actually store null corresponding to those, so it is
    // most useful when we serialize to the file.
    List<Boolean> nullPages = new ArrayList<>();
    List<Object> minValues = new ArrayList<>();
    List<Object> maxValues = new ArrayList<>();
    BoundaryOrder boundaryOrder;
    // Keeps track of all the null values in each page.
    List<Integer> nullCounts = new ArrayList<>();
    // The following are optional statistics.
    // Concatenated repetition level histograms for each page.
    // Keeps track of how many values where seen at each repetition level.
    // Length should always be (number of pages * (max_repetition_level + 1)).
    List<Integer> repLevelHistogram = new ArrayList<>();
    // Concatenated definition level histograms for each page.
    List<Integer> defLevelHistogram = new ArrayList<>();

    ColumnIndex(Type type, PlainEncoder encoder) {
        this.type = type;
        this.encoder = encoder;
        this.boundaryOrder = BoundaryOrder.Unordered;
    }

    void addNewPage() {
        minValues.add(null);
        maxValues.add(null);
    }

    void write(DataOutputStream dataOutputStream) throws IOException {
        for (Object nullPage: nullPages) {
            byte[] encodedValue = encoder.encode(nullPage, type);
            dataOutputStream.write(encodedValue);
        }
        for (Object minValue: minValues) {
            // we dont support repeateds yet so skip them for now.
            if (minValue == null) {
                continue;
            }
            byte[] encodedValue = encoder.encode(minValue, type);
            dataOutputStream.write(encodedValue);
        }
        for (Object maxValue: maxValues) {
            // we dont support repeateds yet so skip them for now.
            if (maxValue == null) {
                continue;
            }
            byte[] encodedValue = encoder.encode(maxValue, type);
            dataOutputStream.write(encodedValue);
        }
        dataOutputStream.write(boundaryOrder.value);
        for (Object nullCount: nullCounts) {
            byte[] encodedValue = encoder.encode(nullCount, type);
            dataOutputStream.write(encodedValue);
        }
        for (Object repLevelHistogramEntry: repLevelHistogram) {
            byte[] encodedValue = encoder.encode(repLevelHistogramEntry, type);
            dataOutputStream.write(encodedValue);
        }
        for (Object defLevelHistogramEntry: repLevelHistogram) {
            byte[] encodedValue = encoder.encode(defLevelHistogramEntry, type);
            dataOutputStream.write(encodedValue);
        }
    }

    <T extends Comparable<T>> void compareAndAssign(Integer pageNumber,
                                                    Object object) {
        T value = (T)object;
        T minValue = (T)minValues.get(pageNumber);
        T maxValue = (T)maxValues.get(pageNumber);
        if (minValue == null || minValue.compareTo(value) > 0) {
            minValues.set(pageNumber, object);
        }
        if (maxValue == null || maxValue.compareTo(value) < 0) {
            maxValues.set(pageNumber, object);
        }
    }

    <T extends Comparable<T>> List<Integer> pagesInRange(Object startRange,
                                                    Object endRange) {
        List<Integer> pagesInRange = new ArrayList<>();
        int numberOfPages = minValues.size();
        // Its an interval overlap check practically.
        for (int pageNumber = 0; pageNumber < numberOfPages; pageNumber++) {
            T minValue = (T) minValues.get(pageNumber);
            T maxValue = (T) maxValues.get(pageNumber);
            T startValue = (T) startRange;
            T endValue = (T) endRange;
            // exclude pages where min and max are out of range
            if (minValue.compareTo(endValue) > 0 ||
                    maxValue.compareTo(startValue) < 0) {
                continue;
            }
            pagesInRange.add(pageNumber);
        }
        return pagesInRange;
    }

    void updateIndexEntry(Integer pageNumber, Object object) throws IOException {
        if (object instanceof Character) {
            compareAndAssign(pageNumber, (Character)object);
        } else if (object instanceof Integer) {
            compareAndAssign(pageNumber, (Integer)object);
        } else if (object instanceof String) {
            compareAndAssign(pageNumber, (String)object);
        } else {
            throw new IOException("Wrong type of object.");
        }
    }

    List<Integer> getPagesInRange(Object startValue, Object endValue) throws IOException {
        if (startValue instanceof Character && endValue instanceof Character) {
            return pagesInRange((Character)startValue, (Character)endValue);
        } else if (startValue instanceof Integer && endValue instanceof Integer) {
            return pagesInRange((Integer)startValue, (Integer)endValue);
        } else if (startValue instanceof String && endValue instanceof String) {
            return pagesInRange((String)startValue, (String)endValue);
        }
        throw new IOException("Wrong type of object.");
    }

    void print() {
        Utils.printObject(this);
    }
}
