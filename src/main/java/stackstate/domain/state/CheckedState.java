package stackstate.domain.state;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import stackstate.domain.enumeration.StateValue;
import stackstate.domain.event.Event;

@ToString
@EqualsAndHashCode
public class CheckedState implements UpdatableState<Event, CheckedState>, Cloneable {

  private final Map<String, StateValue> values;

  private CheckedState(Map<String, StateValue> values) {
    this.values = Collections.unmodifiableMap(values);
  }

  public static CheckedState dataless() {
    return new CheckedState(new HashMap<>());
  }

  public static CheckedState dataless(String... checkedStates) {
    CheckedState.Builder builder = new CheckedState.Builder();
    Arrays.stream(checkedStates)
        .forEach(state -> builder.add(state, StateValue.NO_DATA));
    return builder.build();
  }

  public static Builder builder() {
    return new Builder();
  }

  public static Builder with(String checkedState, StateValue state) {
    return new Builder(checkedState, state);
  }

  public static CheckedState withJust(String checkedState, StateValue state) {
    return CheckedState.with(checkedState, state).build();
  }

  @Override
  public CheckedState updateGiven(Event event) {
    Map<String, StateValue> newValues = new HashMap<>(values);
    newValues.put(event.getCheckState(), event.getState());
    return new CheckedState(newValues);
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

  public Map<String, StateValue> getValues() {
    return Collections.unmodifiableMap(values);
  }

  @Override
  public CheckedState clone() {
    return new CheckedState(Collections.unmodifiableMap(values));
  }

  public static class Builder {

    private final Map<String, StateValue> values = new HashMap<>();

    private Builder() {
    }

    private Builder(String checkedState, StateValue state) {
      values.put(checkedState, state);
    }

    public Builder and(String checkedState, StateValue state) {
      add(checkedState, state);
      return this;
    }

    public CheckedState build() {
      return new CheckedState(values);
    }

    private void add(String checkedState, StateValue state) {
      values.put(checkedState, state);
    }

  }

}
