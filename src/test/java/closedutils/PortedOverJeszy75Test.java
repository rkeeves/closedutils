package closedutils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PortedOverJeszy75Test {

    static final class IntRange {

        static final Closed<Integer> EMPTY = Closed.empty();

        static final Closed<Integer> ALL = Closed.closed(Integer.MIN_VALUE, Integer.MAX_VALUE);

        static Closed<Integer> of(int l, int r) {
            return Closed.closed(l, r);
        }
    }

    private Closed<Integer> range;

    void assertIntRange(int expectedMin, int expectedMax, Closed<Integer> range) {
        final var min = range.match(
                Optional::empty,
                (l, r) -> Optional.of(l)
        );
        final var max = range.match(
                Optional::empty,
                (l, r) -> Optional.of(r)
        );
        assertAll("IntRange bounds",
                () -> assertEquals(Optional.of(expectedMin), min),
                () -> assertEquals(Optional.of(expectedMax), max)
        );
    }

    @BeforeEach
    void setUp() {
        range = IntRange.of(15, 30);
    }

    @Test
    void of() {
        assertIntRange(15, 30, IntRange.of(15, 30));
        assertIntRange(15, 30, IntRange.of(30, 15));
        assertIntRange(15, 15, IntRange.of(15, 15));
    }

    @Test
    void isEmpty() {
        assertFalse(range.isEmpty());
        assertTrue(IntRange.EMPTY.isEmpty());
    }

    @Test
    void contains() {
        assertTrue(range.contains(15));
        assertTrue(range.contains(30));
        assertTrue(range.contains(20));
        assertFalse(range.contains(-10));
        assertFalse(range.contains(40));
        assertFalse(IntRange.EMPTY.contains(0));
        assertFalse(IntRange.EMPTY.contains(Integer.MIN_VALUE));
        assertFalse(IntRange.EMPTY.contains(Integer.MAX_VALUE));
    }

    @Test
    void containsRange() {
        assertTrue(range.containsRange(range));
        assertTrue(range.containsRange(IntRange.of(15, 20)));
        assertTrue(range.containsRange(IntRange.of(25, 30)));
        assertTrue(range.containsRange(IntRange.of(20, 25)));
        assertFalse(range.containsRange(IntRange.of(-100, 20)));
        assertFalse(range.containsRange(IntRange.of(25, 100)));
        assertFalse(range.containsRange(IntRange.ALL));
        // BREAKING CHANGE: changed original assertFalse to assertTrue, reason: empty set isSubset of all possible sets
        assertTrue(range.containsRange(IntRange.EMPTY));
        // BREAKING CHANGE: changed original assertFalse to assertTrue, reason: empty set isSubset of all possible sets
        assertTrue(IntRange.EMPTY.containsRange(IntRange.EMPTY));
    }

    @Test
    void isOverlapping() {
        assertTrue(range.isOverlapping(range));
        assertTrue(range.isOverlapping(IntRange.of(15, 20)));
        assertTrue(range.isOverlapping(IntRange.of(25, 30)));
        assertTrue(range.isOverlapping(IntRange.of(20, 25)));
        assertTrue(range.isOverlapping(IntRange.of(-100, 20)));
        assertTrue(range.isOverlapping(IntRange.of(25, 100)));
        assertTrue(range.isOverlapping(IntRange.of(-100, 100)));
        assertFalse(range.isOverlapping(IntRange.of(-100, 0)));
        assertFalse(range.isOverlapping(IntRange.of(40, 100)));
        assertFalse(range.isOverlapping(IntRange.EMPTY));
        assertFalse(IntRange.EMPTY.isOverlapping(IntRange.EMPTY));
    }

    @Test
    void isDisjoint() {
        assertFalse(range.isDisjoint(range));
        assertFalse(range.isDisjoint(IntRange.of(15, 20)));
        assertFalse(range.isDisjoint(IntRange.of(25, 30)));
        assertFalse(range.isDisjoint(IntRange.of(20, 25)));
        assertFalse(range.isDisjoint(IntRange.of(-100, 20)));
        assertFalse(range.isDisjoint(IntRange.of(25, 100)));
        assertFalse(range.isDisjoint(IntRange.of(-100, 100)));
        assertTrue(range.isDisjoint(IntRange.of(-100, 0)));
        assertTrue(range.isDisjoint(IntRange.of(40, 100)));
        assertTrue(range.isDisjoint(IntRange.EMPTY));
        assertTrue(IntRange.EMPTY.isDisjoint(IntRange.EMPTY));
    }

    @Test
    void intersect() {
        assertSame(range, range.intersect(range));
        assertEquals(IntRange.of(15, 20), range.intersect(IntRange.of(15, 20)));
        assertEquals(IntRange.of(25, 30), range.intersect(IntRange.of(25, 30)));
        assertEquals(IntRange.of(20, 25), range.intersect(IntRange.of(20, 25)));
        assertEquals(IntRange.of(15, 20), range.intersect(IntRange.of(-100, 20)));
        assertEquals(IntRange.of(25, 30), range.intersect(IntRange.of(25, 100)));
        assertEquals(range, range.intersect(IntRange.of(-100, 100)));
        assertEquals(IntRange.EMPTY, range.intersect(IntRange.of(-100, 0)));
        assertEquals(IntRange.EMPTY, range.intersect(IntRange.of(40, 100)));
        assertEquals(IntRange.EMPTY, IntRange.EMPTY.intersect(IntRange.ALL));
    }

    @Test
    void intersect_vararg() {
        assertEquals(IntRange.of(20, 25), range.intersect(IntRange.of(10, 25), IntRange.of(20, 35)));
        assertEquals(IntRange.of(20, 25), range.intersect(IntRange.of(-100, 100), IntRange.of(20, 25)));
        assertEquals(IntRange.EMPTY, range.intersect(IntRange.ALL, IntRange.EMPTY));
    }

    @Test
    void testHashCode() {
        assertTrue(range.hashCode() == range.hashCode());
        assertTrue(range.hashCode() == IntRange.of(15, 30).hashCode());
    }

    @Test
    void testEquals() {
        assertTrue(range.equals(range));
        assertTrue(range.equals(IntRange.of(15, 30)));
        assertFalse(range.equals(IntRange.of(-100, 100)));
        assertFalse(range.equals(IntRange.EMPTY));
        assertFalse(range.equals(null));
        assertFalse(range.equals("Hello, World!"));
    }

    @Test
    void testToString() {
        assertEquals("[15,30]", range.toString());
        assertEquals("[EMPTY]", IntRange.EMPTY.toString());
    }
}
