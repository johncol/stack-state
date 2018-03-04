package stackstate.domain.state;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import stackstate.domain.enumeration.StateValue;
import org.junit.Test;
import stackstate.domain.state.CheckedState;
import stackstate.domain.state.OwnState;
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
        .and("queue", StateValue.CLEAR);
    OwnState ownState = Any.ownState();

    ownState.updateGiven(checkedState);

    assertThat(ownState.value(), is(StateValue.ALERT));
  }

  @Test
  public void shouldUpdateOwnStateToNoDataStateWhenThereAreNotAnyCheckedStates() {
    CheckedState emptyCheckedState = CheckedState.dataless();
    OwnState ownState = Any.ownState();

    ownState.updateGiven(emptyCheckedState);

    assertThat(ownState.value(), is(StateValue.NO_DATA));
  }

}
