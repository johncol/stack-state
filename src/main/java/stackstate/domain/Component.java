package stackstate.domain;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import stackstate.domain.event.Event;
import stackstate.domain.state.CheckedState;
import stackstate.domain.state.DerivedState;
import stackstate.domain.state.OwnState;

@ToString(exclude = {"dependents", "dependencies"})
@EqualsAndHashCode(exclude = {"dependents", "dependencies"})
@Builder
@AllArgsConstructor
public class Component {

  private String id;
  private CheckedState checkedState;
  private OwnState ownState;
  private DerivedState derivedState;
  private Set<Component> dependents;
  private Set<Component> dependencies;

  public static Component withId(String id) {
    return Component.withIdAndCheckedStates(id);
  }

  public static Component withIdAndCheckedStates(String id, String... checkedStates) {
    return Component.builder()
        .id(id)
        .ownState(OwnState.dataless())
        .derivedState(DerivedState.dataless())
        .checkedState(CheckedState.dataless(checkedStates))
        .dependents(new HashSet<>())
        .dependencies(new HashSet<>())
        .build();
  }

  public void apply(Event event) {
    checkedState = checkedState.updateGiven(event);
    ownState = ownState.updateGiven(checkedState);
    derivedState = derivedState.updateGiven(this);
    dependents.forEach(Component::reCalculateDerivedState);
  }

  public void addDependencyOn(Component dependency) {
    dependencies.add(dependency);
    dependency.addDependent(this);
  }

  public void addDependencyOn(Component... newDependencies) {
    Arrays.stream(newDependencies).forEach(this::addDependencyOn);
  }

  protected void reCalculateDerivedState() {
    DerivedState oldDerivedState = derivedState;
    derivedState = derivedState.updateGiven(this);
    if (!derivedState.equals(oldDerivedState)) {
      dependents.forEach(Component::reCalculateDerivedState);
    }
  }

  private void addDependent(Component dependent) {
    dependents.add(dependent);
  }

  public String getId() {
    return id;
  }

  public CheckedState getCheckedState() {
    return checkedState.clone();
  }

  public OwnState getOwnState() {
    return ownState.clone();
  }

  public DerivedState getDerivedState() {
    return derivedState.clone();
  }

  public Set<Component> getDependents() {
    return Collections.unmodifiableSet(dependents);
  }

  public Set<Component> getDependencies() {
    return Collections.unmodifiableSet(dependencies);
  }
}
