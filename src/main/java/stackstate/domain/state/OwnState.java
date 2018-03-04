package stackstate.domain.state;

import stackstate.domain.enumeration.StateValue;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OwnState implements UpdatableState<CheckedState>, Cloneable {

  private StateValue state;

  public static OwnState of(StateValue state) {
    return new OwnState(state);
  }

  public static OwnState dataless() {
    return OwnState.of(StateValue.NO_DATA);
  }

  @Override
  public void updateGiven(CheckedState checkedState) {
    state = checkedState.getHighestState();
  }

  public StateValue value() {
    return state;
  }

  @Override
  public OwnState clone() {
    return OwnState.of(state);
  }
}
