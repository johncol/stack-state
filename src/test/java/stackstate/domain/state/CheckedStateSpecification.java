package stackstate.domain.state;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import stackstate.domain.enumeration.StateValue;
import stackstate.domain.event.Event;

public class CheckedStateSpecification {

  @Test
  public void shouldReturnNoDataWhenAskedForCheckedStateThatIsNotBeingTracked() {
    CheckedState checkedState = CheckedState.withJust("memory", StateValue.CLEAR);

    assertThat(checkedState.isTracking("app"), is(false));
    assertThat(checkedState.valueOf("app"), is(StateValue.NO_DATA));
  }

  @Test
  public void shouldBuildCheckedStatesWithSentValues() {
    CheckedState checkedState = CheckedState
        .with("memory", StateValue.CLEAR)
        .and("nosql-db", StateValue.WARNING)
        .build();

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
        .and("nosql-db", StateValue.WARNING)
        .build();

    assertThat(checkedState.getHighestState(), is(StateValue.ALERT));
  }

  @Test
  public void shouldAddNewCheckedState() {
    CheckedState checkedState = CheckedState
        .with("memory", StateValue.CLEAR)
        .and("nosql-db", StateValue.WARNING)
        .build();

    CheckedState newCheckedState = checkedState.updateGiven(Event.builder()
        .checkState("sql-db")
        .state(StateValue.ALERT)
        .build());

    assertThat(newCheckedState.isTracking("sql-db"), is(true));
    assertThat(newCheckedState.valueOf("sql-db"), is(StateValue.ALERT));
  }

  @Test
  public void shouldUpdateExistingCheckedState() {
    CheckedState checkedState = CheckedState
        .with("memory", StateValue.CLEAR)
        .and("nosql-db", StateValue.WARNING)
        .build();

    CheckedState newCheckedState = checkedState.updateGiven(Event.builder()
        .checkState("nosql-db")
        .state(StateValue.ALERT)
        .build());

    assertThat(newCheckedState.valueOf("nosql-db"), is(not(StateValue.WARNING)));
    assertThat(newCheckedState.valueOf("nosql-db"), is(StateValue.ALERT));
  }

  @Test
  public void shouldBeImmutable() {
    CheckedState originalCheckedState = CheckedState.withJust("memory", StateValue.CLEAR);

    CheckedState newCheckedState = originalCheckedState.updateGiven(Event.builder()
        .checkState("nosql-db")
        .state(StateValue.WARNING)
        .build());

    assertThat(originalCheckedState.isTracking("nosql-db"), is(false));
    assertThat(newCheckedState.isTracking("nosql-db"), is(true));
  }

}
