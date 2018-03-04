package stackstate.domain.state;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Set;
import org.junit.Test;
import stackstate.domain.Component;
import stackstate.domain.enumeration.StateValue;
import utils.Any;

public class DerivedStateSpecification {

  @Test
  public void shouldBuildNoDataDerivedStateWhenDatalessFactoryMethodIsCalled() {
    DerivedState noDataDerivedState = DerivedState.dataless();

    assertThat(noDataDerivedState.value(), is(StateValue.NO_DATA));
  }

  @Test
  public void shouldSetDerivedStateToNoDataWhenOwnStateAndDependentsDerivedStateAreAnyCombinationOfClearAndNoState() {
    Set<Component> dependencies = Set.of(
        Any.componentWithDerivedState(Any.of(StateValue.NO_DATA, StateValue.CLEAR)),
        Any.componentWithDerivedState(Any.of(StateValue.NO_DATA, StateValue.CLEAR)),
        Any.componentWithDerivedState(Any.of(StateValue.NO_DATA, StateValue.CLEAR))
    );

    Component component = Component.builder()
        .ownState(OwnState.of(Any.of(StateValue.NO_DATA, StateValue.CLEAR)))
        .dependencies(dependencies)
        .build();

    DerivedState derivedState = Any.derivedState().updateGiven(component);

    assertThat(derivedState.value(), is(StateValue.NO_DATA));
  }

  @Test
  public void shouldSetDerivedStateToOwnStateValueWhenOwnStateIsHigherThanDependentsDerivedState() {
    Set<Component> dependencies = Set.of(
        Any.componentWithDerivedState(Any.of(StateValue.NO_DATA, StateValue.CLEAR, StateValue.WARNING)),
        Any.componentWithDerivedState(Any.of(StateValue.NO_DATA, StateValue.CLEAR, StateValue.WARNING)),
        Any.componentWithDerivedState(Any.of(StateValue.NO_DATA, StateValue.CLEAR, StateValue.WARNING))
    );
    Component component = Component.builder()
        .ownState(OwnState.of(StateValue.ALERT))
        .dependencies(dependencies)
        .build();

    DerivedState derivedState = Any.derivedState().updateGiven(component);

    assertThat(derivedState.value(), is(StateValue.ALERT));
  }

  @Test
  public void shouldSetDerivedStateToDependentsDerivedStateValueWhenDependentsDerivedStateIsHigherThanOwnState() {
    Set<Component> dependencies = Set.of(
        Any.componentWithDerivedState(StateValue.WARNING),
        Any.componentWithDerivedState(StateValue.ALERT)
    );
    Component component = Component.builder()
        .ownState(OwnState.of(Any.of(StateValue.NO_DATA, StateValue.CLEAR, StateValue.WARNING)))
        .dependencies(dependencies)
        .build();

    DerivedState derivedState = Any.derivedState().updateGiven(component);

    assertThat(derivedState.value(), is(StateValue.ALERT));
  }

}
