package stackstate.domain.event;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import stackstate.domain.enumeration.StateValue;

@ToString
@Getter
@Builder
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Event implements Comparable<Event> {

  private final long timestamp;
  private final String component;
  private final String checkState;
  private final StateValue state;

  public static Event of(long timestamp, String component, String checkState, StateValue state) {
    return Event.builder()
        .timestamp(timestamp)
        .component(component)
        .checkState(checkState)
        .state(state)
        .build();
  }

  public static Event of(String component, String checkState, StateValue state) {
    return Event.of(1, component, checkState, state);
  }

  @Override
  public int compareTo(Event other) {
    return Long.compare(timestamp, other.timestamp);
  }
}
