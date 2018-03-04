package stackstate.domain.event;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Builder
@AllArgsConstructor
public class EventChain implements Iterable<Event> {

  private final List<Event> events;

  public static EventChain empty() {
    return EventChain.builder()
        .events(List.of())
        .build();
  }

  public static EventChain withEvent(Event event) {
    return EventChain.withEvents(event);
  }

  public static EventChain withEvents(Event... events) {
    return EventChain.builder()
        .events(Arrays.asList(events))
        .build();
  }

  @Override
  public Iterator<Event> iterator() {
    return events.iterator();
  }

  public Stream<Event> stream() {
    return events.stream();
  }

}
