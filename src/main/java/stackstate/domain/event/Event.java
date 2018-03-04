package stackstate.domain.event;

import stackstate.domain.enumeration.StateValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class Event {

  private final String component;
  private final String checkState;
  private final StateValue state;

  public static Event of(String component, String checkState, StateValue state) {
    return Event.builder()
        .component(component)
        .checkState(checkState)
        .state(state)
        .build();
  }
}
