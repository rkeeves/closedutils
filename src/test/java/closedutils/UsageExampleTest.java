package closedutils;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static closedutils.Closed.closed;
import static closedutils.Closed.empty;
import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;

class UsageExampleTest {

    enum Day { Dawn, Morning, Forenoon, Noon, Afternoon, Evening, Night }
    record Person(String name, Closed<Day> availability) {}

    static Stream<Person> people() {
        return Stream.of(
                new Person("Ann", closed(Day.Dawn, Day.Noon)),
                new Person("Bob", closed(Day.Morning, Day.Evening)),
                new Person("Cat", closed(Day.Dawn, Day.Afternoon)),
                new Person("Dan", closed(Day.Forenoon, Day.Noon)),
                new Person("Eve", closed(Day.Forenoon, Day.Evening)),
                new Person("Fyn", closed(Day.Dawn, Day.Noon)),
                new Person("Gil", closed(Day.Forenoon, Day.Evening)),
                new Person("Han", closed(Day.Morning, Day.Afternoon))
        );
    }

    @Test
    void whenAreAllAvailable() {
        assertEquals(
                closed(Day.Forenoon, Day.Noon),
                people().map(Person::availability).reduce(closed(Day.Dawn, Day.Night), Closed::intersect)
        );
    }

    @Test
    void whenIsAnyoneAvailable() {
        assertEquals(closed(Day.Dawn, Day.Evening),
                people().map(Person::availability).reduce(empty(), Closed::convexHull)
        );
    }

    @Test
    void whoIsAvailableAroundMorningTillAfternoon() {
        final var MORNING_AFTERNOON = Closed.closed(Day.Morning, Day.Afternoon);
        assertEquals(
                List.of("Bob", "Cat", "Han"),
                people().filter(p -> p.availability.containsRange(MORNING_AFTERNOON)).map(Person::name).toList()
        );
    }

    @Test
    void whenCanAnnFynAndEveWorkTogether() {
        final var DREAM_TEAM = Set.of("Ann", "Fyn", "Eve");
        assertEquals(
                closed(Day.Forenoon, Day.Noon),
                people().filter(p -> DREAM_TEAM.contains(p.name)).map(Person::availability).reduce(closed(Day.Dawn, Day.Night), Closed::intersect)
        );
    }

    @Test
    void howManyPeopleCanAGivenPersonBabysit() {
        assertEquals(
                Map.ofEntries(
                        entry("Ann", 2L),
                        entry("Bob", 4L),
                        entry("Cat", 4L),
                        // Dan cannot babysit anyone.
                        entry("Eve", 2L),
                        entry("Fyn", 2L),
                        entry("Gil", 2L),
                        entry("Han", 1L)
                ),
                people()
                        .flatMap(x -> people().filter(y -> !y.name.equals(x.name)).map(y -> List.of(x, y)))
                        .filter(xy -> xy.get(0).availability.containsRange(xy.get(1).availability))
                        .collect(Collectors.groupingBy((xy) -> xy.get(0).name, Collectors.counting()))
        );
    }
}
