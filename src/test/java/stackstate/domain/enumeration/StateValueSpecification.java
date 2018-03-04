package stackstate.domain.enumeration;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import stackstate.domain.enumeration.StateValue;
import utils.Any;

public class StateValueSpecification {

  @Test
  public void shouldReturnClearStateIsHigherThanNoDataState() {
    StateValue highest = StateValue.highestOf(StateValue.NO_DATA, StateValue.CLEAR);

    assertThat(highest, is(StateValue.CLEAR));
  }

  @Test
  public void shouldReturnWarningStateIsHigherThanClearState() {
    StateValue highest = StateValue.highestOf(StateValue.WARNING, StateValue.CLEAR);

    assertThat(highest, is(StateValue.WARNING));
  }

  @Test
  public void shouldReturnAlertStateIsHigherThanAnyOtherState() {
    StateValue anyStateButAlert = Any.of(StateValue.NO_DATA, StateValue.CLEAR, StateValue.WARNING);
    StateValue highest = StateValue.highestOf(StateValue.ALERT, anyStateButAlert);

    assertThat(highest, is(StateValue.ALERT));
  }

  @Test
  public void shouldReturnNoDataAndClearStatesDoNotSatisfyTheWarningOrHigherVerification() {
    boolean warningOrHigher = Any.of(StateValue.NO_DATA, StateValue.CLEAR).warningOrHigher();

    assertThat(warningOrHigher, is(false));
  }

  @Test
  public void shouldReturnWarningAndAlertStatesSatisfyTheWarningOrHigherVerification() {
    boolean warningOrHigher = Any.of(StateValue.WARNING, StateValue.ALERT).warningOrHigher();

    assertThat(warningOrHigher, is(true));
  }

  @Test
  public void shouldOrderStateValuesFromLowerToHigherWhenUsingStateValueComparator() {
    List<StateValue> states = Arrays.stream(StateValue.values())
        .sorted(StateValue.COMPARATOR)
        .collect(Collectors.toList());

    assertThat(states, hasSize(4));

    assertThat(states.get(0), is(StateValue.NO_DATA));
    assertThat(states.get(1), is(StateValue.CLEAR));
    assertThat(states.get(2), is(StateValue.WARNING));
    assertThat(states.get(3), is(StateValue.ALERT));
  }

}
