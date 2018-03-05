package stackstate.io;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Set;
import org.junit.Test;
import stackstate.StackState;
import stackstate.domain.Component;
import stackstate.domain.enumeration.StateValue;
import stackstate.domain.event.Event;
import stackstate.domain.event.EventChain;
import stackstate.domain.state.CheckedState;
import stackstate.domain.state.DerivedState;
import stackstate.domain.state.OwnState;
import utils.Any;

public class ExternalJsonFileReaderSpecification {

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowExceptionWhenStackStateJsonFileIsMalformed() {
    String stateFile = getFileFullPath("./stack-state-malformed.json");

    StackStateReader reader = new ExternalJsonFileReader(stateFile, Any.fileName());
    reader.readInitialState();
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowExceptionWhenEventsJsonFileIsMalformed() {
    String eventsFile = getFileFullPath("./events-malformed.json");

    StackStateReader reader = new ExternalJsonFileReader(Any.fileName(), eventsFile);
    reader.readEvents();
  }

  @Test
  public void shouldBuildEmptyStackStateWhenStackStateJsonFileIsEmpty() {
    String stateFile = getFileFullPath("./stack-state-empty.json");

    StackStateReader reader = new ExternalJsonFileReader(stateFile, Any.fileName());
    StackState stackState = reader.readInitialState();

    assertThat(stackState.size(), is(equalTo(0)));
  }

  @Test
  public void shouldBuildEmptyEventChainWhenEventsJsonFileIsEmpty() {
    String eventsFile = getFileFullPath("./events-empty.json");

    StackStateReader reader = new ExternalJsonFileReader(Any.fileName(), eventsFile);
    EventChain events = reader.readEvents();

    assertThat(events.size(), is(equalTo(0)));
  }

  @Test
  public void shouldBuildStackStateWhenStackStateJsonFileIsNotEmpty() {
    String stateFile = getFileFullPath("./stack-state-non-empty.json");

    StackStateReader reader = new ExternalJsonFileReader(stateFile, Any.fileName());
    StackState stackState = reader.readInitialState();

    assertThat(stackState.size(), is(equalTo(2)));

    Component appComponent = stackState.getComponent("app").get();

    assertThat(appComponent, is(equalTo(Component.builder()
        .id("app")
        .ownState(OwnState.of(StateValue.NO_DATA))
        .derivedState(DerivedState.of(StateValue.NO_DATA))
        .checkedState(CheckedState.builder()
            .and("CPU load", StateValue.NO_DATA)
            .and("RAM usage", StateValue.NO_DATA)
            .build())
        .build())));

    assertThat(appComponent.getDependencies(), hasSize(1));
    assertThat(appComponent.getDependents(), hasSize(0));

    Component dependency = getItsOnlyElement(appComponent.getDependencies());
    assertThat(dependency.getId(), is(equalTo("db")));

    Component dbComponent = stackState.getComponent("db").get();
    assertThat(dbComponent, is(equalTo(Component.builder()
        .id("db")
        .ownState(OwnState.of(StateValue.NO_DATA))
        .derivedState(DerivedState.of(StateValue.NO_DATA))
        .checkedState(CheckedState.builder()
            .and("CPU load", StateValue.NO_DATA)
            .and("RAM usage", StateValue.NO_DATA)
            .build())
        .build())));

    assertThat(dbComponent.getDependencies(), hasSize(0));
    assertThat(dbComponent.getDependents(), hasSize(1));

    Component dependent = getItsOnlyElement(dbComponent.getDependents());
    assertThat(dependent.getId(), is(equalTo("app")));
  }

  @Test
  public void shouldBuildEventChainWhenEventsJsonFileIsNotEmpty() {
    String eventsFile = getFileFullPath("./events-non-empty.json");

    StackStateReader reader = new ExternalJsonFileReader(Any.fileName(), eventsFile);
    EventChain eventChain = reader.readEvents();

    assertThat(eventChain.size(), is(equalTo(2)));

    assertThat(eventChain.getEvent(0), is(equalTo(Event.builder()
        .timestamp(1)
        .component("db")
        .checkState("CPU load")
        .state(StateValue.WARNING)
        .build())));

    assertThat(eventChain.getEvent(1), is(equalTo(Event.builder()
        .timestamp(2)
        .component("app")
        .checkState("CPU load")
        .state(StateValue.CLEAR)
        .build())));

  }

  private Component getItsOnlyElement(Set<Component> components) {
    return components.toArray(new Component[]{})[0];
  }

  private static String getFileFullPath(String file) {
    return ExternalJsonFileReaderSpecification.class
        .getClassLoader()
        .getResource(file)
        .getPath()
        .substring(1);
  }

}
