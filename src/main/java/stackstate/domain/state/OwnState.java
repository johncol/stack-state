package stackstate.domain.state;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import stackstate.domain.enumeration.StateValue;

@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OwnState implements UpdatableState<CheckedState, OwnState>, Cloneable {

  private final StateValue state;

  public static OwnState of(StateValue state) {
    return new OwnState(state);
  }

  public static OwnState dataless() {
    return OwnState.of(StateValue.NO_DATA);
  }

  @Override
  public OwnState updateGiven(CheckedState checkedState) {
    return OwnState.of(checkedState.getHighestState());
  }

  public StateValue value() {
    return state;
  }

  @Override
  public OwnState clone() {
    return OwnState.of(state);
  }
}
