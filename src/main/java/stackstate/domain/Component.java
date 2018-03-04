package stackstate.domain;

import stackstate.domain.event.Event;
import stackstate.domain.state.CheckedState;
import stackstate.domain.state.DerivedState;
import stackstate.domain.state.OwnState;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

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
    return Component.builder()
        .id(id)
        .ownState(OwnState.dataless())
        .derivedState(DerivedState.dataless())
        .checkedState(CheckedState.dataless())
        .dependents(new HashSet<>())
        .dependencies(new HashSet<>())
        .build();
  }

  public void apply(Event event) {
    checkedState.updateGiven(event);
    ownState.updateGiven(checkedState);
    derivedState.updateGiven(this);
    dependents.forEach(Component::reCalculateDerivedState);
  }

  public void addDependencyOn(Component dependency) {
    dependencies.add(dependency);
    dependency.addDependent(this);
  }

  protected void reCalculateDerivedState() {
    derivedState.updateGiven(this);
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
