package stackstate.domain.state;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import stackstate.domain.enumeration.StateValue;
import utils.Any;

public class OwnStateSpecification {

  @Test
  public void shouldBuildNoDataOwnStateWhenDatalessFactoryMethodIsCalled() {
    OwnState noDataOwnState = OwnState.dataless();

    assertThat(noDataOwnState.value(), is(StateValue.NO_DATA));
  }

  @Test
  public void shouldUpdateOwnStateToTheHighestCheckedStateWhenThereIsAtLeastOneCheckedState() {
    CheckedState checkedState = CheckedState
        .with("memory", StateValue.WARNING)
        .and("sql-db", StateValue.ALERT)
        .and("queue", StateValue.CLEAR)
        .build();

    OwnState ownState = Any.ownState().updateGiven(checkedState);

    assertThat(ownState.value(), is(StateValue.ALERT));
  }

  @Test
  public void shouldUpdateOwnStateToNoDataStateWhenThereAreNotAnyCheckedStates() {
    CheckedState emptyCheckedState = CheckedState.dataless();

    OwnState ownState = Any.ownState().updateGiven(emptyCheckedState);

    assertThat(ownState.value(), is(StateValue.NO_DATA));
  }

  @Test
  public void shouldBeImmutable() {
    CheckedState checkedState = CheckedState.withJust("sql-db", StateValue.ALERT);

    OwnState originalOwnState = OwnState.of(StateValue.CLEAR);
    OwnState newOwnState = originalOwnState.updateGiven(checkedState);

    assertThat(originalOwnState.value(), is(StateValue.CLEAR));
    assertThat(newOwnState.value(), is(StateValue.ALERT));

  }

}
