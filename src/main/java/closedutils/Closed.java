package closedutils;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public sealed class Closed<T extends Comparable<T>> {

    public static <T extends Comparable<T>> Closed<T> empty() {
        return new Empty<>();
    }

    public static <T extends Comparable<T>> Closed<T> closed(T l, T r) {
        return Ord.lte(l, r) ? new Interval<>(l, r) : new Interval<>(r, l);
    }

    public final <U> U match(Supplier<U> onEmpty, BiFunction<T, T, U> onInterval) {
        return this instanceof Interval<T> i ? onInterval.apply(i.l, i.r) : onEmpty.get();
    }

    public final boolean contains(T x) {
        return match(
                () -> false,
                (l, r) -> Ord.lte(l, x) && Ord.lte(x, r));
    }

    public final boolean isEmpty() {
        return match(
                () -> true,
                (l, r) -> false);
    }

    public final boolean isDisjoint(Closed<T> o) {
        return match(
                () -> true,
                (l, r) -> o.match(
                        () -> true,
                        (ll, rr) -> Ord.lt(r, ll) || Ord.lt(rr, l)
                ));
    }

    public final boolean isOverlapping(Closed<T> o) {
        return !isDisjoint(o);
    }

    public final boolean containsRange(Closed<T> o) {
        return o.match(
                () -> true,
                (ll, rr) -> match(
                        () -> false,
                        (l, r) -> Ord.lte(l, ll) && Ord.lte(rr, r)
                )
        );
    }

    public final Closed<T> convexHull(Closed<T> o) {
        return match(
                () -> o,
                (l, r) -> o.match(
                        () -> this,
                        (ll, rr) -> closed(Ord.min(l, ll), Ord.max(r, rr))
                ));
    }

    public final Closed<T> intersect(Closed<T> o) {
        return this == o ? this : match(
                () -> this,
                (l, r) -> o.match(
                        () -> o,
                        (ll, rr) -> isDisjoint(o) ? empty() : closed(Ord.max(l, ll), Ord.min(r, rr))
                ));
    }

    @SafeVarargs
    public final Closed<T> intersect(Closed<T>... o) {
        return Arrays.stream(o).reduce(this, Closed::intersect);
    }

    @Override
    public final int hashCode() {
        return match(
                () -> 0,
                (l, r) -> 17 * (17 * 5 + l.hashCode()) + r.hashCode());
    }

    @Override
    public final boolean equals(Object o) {
        if (o instanceof Closed<?> c) {
            return match(
                    () -> c.match(
                            () -> true,
                            (ll, rr) -> false),
                    (l, r) -> c.match(
                            () -> false,
                            (ll, rr) -> Objects.equals(l, ll) && Objects.equals(r, rr)
                    )
            );
        }
        return false;
    }

    @Override
    public final String toString() {
        return match(
                () -> "[EMPTY]",
                (l, r) -> String.format("[%s,%s]", l, r));
    }

    public static final class Empty<T extends Comparable<T>> extends Closed<T> { }

    public static final class Interval<T extends Comparable<T>> extends Closed<T> {
        private final T l, r;
        public Interval(T l, T r) { this.l = l; this.r = r; }
    }
}
