package feature_test;

import java.util.Arrays;

public class StringTuple {
    private final String[] elements;

    public StringTuple(String... parts) {
        elements = parts;
    }

    public int size() {
        return elements.length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StringTuple that = (StringTuple) o;

        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(elements, that.elements);

    }

    @Override
    public int hashCode() {
        return elements != null ? Arrays.hashCode(elements) : 0;
    }
}
