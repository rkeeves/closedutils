package closedutils;

public final class Ord {

    public static <T extends Comparable<T>> boolean lt(T a, T b) {
        return a.compareTo(b) < 0;
    }

    public static <T extends Comparable<T>> boolean lte(T a, T b) {
        return a.compareTo(b) <= 0;
    }

    public static <T extends Comparable<T>> T min(T a, T b) {
        return lte(a, b) ? a : b;
    }

    public static <T extends Comparable<T>> T max(T a, T b) {
        return lte(a, b) ? b : a;
    }
}
