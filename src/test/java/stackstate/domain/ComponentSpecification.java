package stackstate.domain;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import stackstate.domain.Component;
import stackstate.domain.event.Event;
import stackstate.domain.state.CheckedState;
import stackstate.domain.state.DerivedState;
import stackstate.domain.state.OwnState;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import utils.Any;

public class ComponentSpecification {

  @Test
  public void shouldBuildEmptySystemComponentWithTheProvidedIdWhenFactoryMethodIsCalled() {
    Component component = Component.withId("APP");

    assertThat(component.getId(), is("APP"));
    assertThat(component.getOwnState(), is(equalTo(OwnState.dataless())));
    assertThat(component.getDerivedState(), is(equalTo(DerivedState.dataless())));
    assertThat(component.getCheckedState(), is(equalTo(CheckedState.dataless())));
    assertThat(component.getDependencies(), hasSize(0));
    assertThat(component.getDependents(), hasSize(0));
  }

  @Test
  public void shouldUpdateItsThreeStatesWhenEventIsApplied() {
    OwnState ownState = mock(OwnState.class);
    DerivedState derivedState = mock(DerivedState.class);
    CheckedState checkedState = mock(CheckedState.class);
    Component component = Component.builder()
        .id("APP")
        .ownState(ownState)
        .derivedState(derivedState)
        .checkedState(checkedState)
        .dependents(new HashSet<>())
        .build();

    Event event = Any.event();
    component.apply(event);

    verify(checkedState, times(1)).updateGiven(event);
    verify(ownState, times(1)).updateGiven(checkedState);
    verify(derivedState, times(1)).updateGiven(component);
  }

  @Test
  public void shouldUpdateDependentsDerivedStateWhenEventIsApplied() {
    Set<Component> dependents = Set.of(mock(Component.class), mock(Component.class));
    Component component = Component.builder()
        .id("APP")
        .ownState(mock(OwnState.class))
        .derivedState(mock(DerivedState.class))
        .checkedState(mock(CheckedState.class))
        .dependents(dependents)
        .build();

    component.apply(Any.event());

    dependents.forEach(dependent ->
        verify(dependent, times(1)).reCalculateDerivedState());
  }

  @Test
  public void shouldAddDependency() {
    Component component = Component.withId("APP");
    Component dependency = Component.withId("DB");

    component.addDependencyOn(dependency);

    assertThat(component.getDependencies(), hasSize(1));
    assertThat(component.getDependencies(), contains(dependency));
  }

  @Test
  public void shouldAddItselfAsADependentOfDependenciesWhenDependencyIsAdded() {
    Component component = Component.withId("APP");
    Component dependency = Component.withId("DB");

    component.addDependencyOn(dependency);

    assertThat(dependency.getDependents(), hasSize(1));
    assertThat(dependency.getDependents(), contains(component));
  }

}
