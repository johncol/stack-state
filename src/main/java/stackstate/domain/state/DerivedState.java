package stackstate.domain.state;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import stackstate.domain.Component;
import stackstate.domain.enumeration.StateValue;

@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DerivedState implements UpdatableState<Component, DerivedState>, Cloneable {

  private final StateValue state;

  public static DerivedState of(StateValue state) {
    return new DerivedState(state);
  }

  public static DerivedState dataless() {
    return DerivedState.of(StateValue.NO_DATA);
  }

  @Override
  public DerivedState updateGiven(Component component) {
    StateValue dependenciesStateValue = obtainDependenciesStateValue(component);
    StateValue ownState = obtainOwnState(component);
    return DerivedState.of(StateValue.highestOf(dependenciesStateValue, ownState));
  }

  public StateValue value() {
    return state;
  }

  private StateValue obtainDependenciesStateValue(Component component) {
    return component.getDependencies().stream()
        .map(Component::getDerivedState)
        .map(DerivedState::value)
        .filter(StateValue::warningOrHigher)
        .sorted(StateValue.REVERSED_COMPARATOR)
        .findFirst()
        .orElse(StateValue.NO_DATA);
  }

  private StateValue obtainOwnState(Component component) {
    StateValue ownState = component.getOwnState().value();
    if (ownState.equals(StateValue.CLEAR)) {
      ownState = StateValue.NO_DATA;
    }
    return ownState;
  }

  @Override
  public DerivedState clone() {
    return DerivedState.of(state);
  }
}
