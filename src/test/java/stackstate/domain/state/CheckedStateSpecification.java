package stackstate.domain.state;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import stackstate.domain.enumeration.StateValue;
import stackstate.domain.event.Event;
import org.junit.Test;
import stackstate.domain.state.CheckedState;

public class CheckedStateSpecification {

  @Test
  public void shouldReturnNoDataWhenAskedForCheckedStateThatIsNotBeingTracked() {
    CheckedState checkedState = CheckedState.with("memory", StateValue.CLEAR);

    assertThat(checkedState.isTracking("app"), is(false));
    assertThat(checkedState.valueOf("app"), is(StateValue.NO_DATA));
  }

  @Test
  public void shouldBuildCheckedStatesWithSentValues() {
    CheckedState checkedState = CheckedState
        .with("memory", StateValue.CLEAR)
        .and("nosql-db", StateValue.WARNING);

    assertThat(checkedState.isTracking("memory"), is(true));
    assertThat(checkedState.valueOf("memory"), is(StateValue.CLEAR));

    assertThat(checkedState.isTracking("nosql-db"), is(true));
    assertThat(checkedState.valueOf("nosql-db"), is(StateValue.WARNING));
  }

  @Test
  public void shouldReturnHighestStateValueIsNoDataWhenCheckedStateIsEmpty() {
    CheckedState emptyCheckedState = CheckedState.dataless();

    assertThat(emptyCheckedState.getHighestState(), is(StateValue.NO_DATA));
  }

  @Test
  public void shouldReturnHighestStateValueWhenCheckedStateIsNonEmpty() {
    CheckedState checkedState = CheckedState
        .with("memory", StateValue.CLEAR)
        .and("sql-db", StateValue.ALERT)
        .and("nosql-db", StateValue.WARNING);

    assertThat(checkedState.getHighestState(), is(StateValue.ALERT));
  }

  @Test
  public void shouldAddNewCheckedState() {
    CheckedState checkedState = CheckedState
        .with("memory", StateValue.CLEAR)
        .and("nosql-db", StateValue.WARNING);

    checkedState.updateGiven(Event.builder()
        .checkState("sql-db")
        .state(StateValue.ALERT)
        .build());

    assertThat(checkedState.isTracking("sql-db"), is(true));
    assertThat(checkedState.valueOf("sql-db"), is(StateValue.ALERT));
  }

  @Test
  public void shouldUpdateExistingCheckedState() {
    CheckedState checkedState = CheckedState
        .with("memory", StateValue.CLEAR)
        .and("nosql-db", StateValue.WARNING);

    checkedState.updateGiven(Event.builder()
        .checkState("nosql-db")
        .state(StateValue.ALERT)
        .build());

    assertThat(checkedState.valueOf("nosql-db"), is(not(StateValue.WARNING)));
    assertThat(checkedState.valueOf("nosql-db"), is(StateValue.ALERT));
  }

}
