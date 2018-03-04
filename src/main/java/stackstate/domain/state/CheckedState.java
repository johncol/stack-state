package stackstate.domain.state;

import stackstate.domain.enumeration.StateValue;
import stackstate.domain.event.Event;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CheckedState implements UpdatableState<Event>, Cloneable {

  private final Map<String, StateValue> values;

  public CheckedState() {
    values = new HashMap<>();
  }

  public static CheckedState dataless() {
    return new CheckedState();
  }

  public static CheckedState with(String checkedState, StateValue state) {
    CheckedState checkedStates = new CheckedState();
    checkedStates.updateMap(checkedState, state);
    return checkedStates;
  }

  public CheckedState and(String checkedState, StateValue state) {
    updateMap(checkedState, state);
    return this;
  }

  @Override
  public void updateGiven(Event event) {
    updateMap(event.getCheckState(), event.getState());
  }

  public boolean isTracking(String checkState) {
    return values.containsKey(checkState);
  }

  public StateValue valueOf(String checkedState) {
    return values.getOrDefault(checkedState, StateValue.NO_DATA);
  }

  public StateValue getHighestState() {
    return values.values()
        .stream()
        .sorted(StateValue.REVERSED_COMPARATOR)
        .findFirst()
        .orElse(StateValue.NO_DATA);
  }

  private void updateMap(String checkedState, StateValue state) {
    values.put(checkedState, state);
  }

  @Override
  public CheckedState clone() {
    return new CheckedState(Collections.unmodifiableMap(values));
  }
}
