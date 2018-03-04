package stackstate;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import stackstate.domain.Component;
import stackstate.domain.enumeration.StateValue;
import stackstate.domain.event.Event;
import stackstate.domain.event.EventChain;
import stackstate.domain.state.CheckedState;
import stackstate.domain.state.DerivedState;
import stackstate.domain.state.OwnState;

public class StateCalculatorSpecification {

  private final StateCalculator stateCalculator = new StateCalculator();

  @Test
  public void shouldNotChangeInitialStateWhenNoEventsAreSent() {
    StackState initialState = StackState.withComponent(Component.withId("APP"));
    EventChain emptyEventChain = EventChain.empty();

    StackState finalState = stateCalculator.processEvents(initialState, emptyEventChain);

    assertThat(finalState, is(equalTo(initialState)));
  }

  @Test
  public void shouldNotChangeInitialStateWhenEventsSentDoNotTargetAnySystemComponent() {
    StackState initialState = StackState.withComponents(
        Component.withId("APP"),
        Component.withId("DB")
    );
    EventChain emptyEventChain = EventChain.withEvents(
        Event.of("queue", "memory", StateValue.WARNING),
        Event.of("nosql-db", "memory", StateValue.ALERT)
    );

    StackState finalState = stateCalculator.processEvents(initialState, emptyEventChain);

    assertThat(finalState, is(equalTo(initialState)));
  }

  @Test
  public void shouldUpdateComponentStateWhenEventSentTargetsIt() {
    String memoryCheckState = "memory";

    Component appComponent = Component.withId("APP");
    StackState initialState = StackState.withComponent(appComponent);

    Event appMemoryWarning = Event.of("APP", memoryCheckState, StateValue.WARNING);
    EventChain eventChain = EventChain.withEvent(appMemoryWarning);

    StackState finalState = stateCalculator.processEvents(initialState, eventChain);

    assertThat(finalState.getComponent("APP").get(), is(equalTo(Component.builder()
        .id("APP")
        .checkedState(CheckedState.withJust(memoryCheckState, StateValue.WARNING))
        .ownState(OwnState.of(StateValue.WARNING))
        .derivedState(DerivedState.of(StateValue.WARNING))
        .build())));
  }

  @Test
  public void shouldUpdateDependentComponentStateWhenEventSentTargetsDependencyComponent() {
    String memoryCheckState = "memory";

    Component appComponent = Component.withId("APP");
    Component dbComponent = Component.withId("DB");
    appComponent.addDependencyOn(dbComponent);

    StackState initialState = StackState.withComponents(appComponent, dbComponent);

    Event dbMemoryWarning = Event.of("DB", memoryCheckState, StateValue.WARNING);
    EventChain eventChain = EventChain.withEvent(dbMemoryWarning);

    StackState finalState = stateCalculator.processEvents(initialState, eventChain);

    Component expectedAppComponentState = Component.builder()
        .id("APP")
        .checkedState(CheckedState.dataless())
        .ownState(OwnState.dataless())
        .derivedState(DerivedState.of(StateValue.WARNING))
        .build();
    assertThat(finalState.getComponent("APP").get(), is(equalTo(expectedAppComponentState)));
  }

  @Test
  public void shouldNotUpdateDependencyComponentStateWhenEventSentTargetsDependentComponent() {
    String memoryCheckState = "memory";

    Component appComponent = Component.withId("APP");
    Component dbComponent = Component.withId("DB");
    appComponent.addDependencyOn(dbComponent);

    StackState initialState = StackState.withComponents(appComponent, dbComponent);

    Event appMemoryWarning = Event.of("APP", memoryCheckState, StateValue.WARNING);
    EventChain eventChain = EventChain.withEvent(appMemoryWarning);

    StackState finalState = stateCalculator.processEvents(initialState, eventChain);

    Component expectedDbState = Component.builder()
        .id("DB")
        .checkedState(CheckedState.dataless())
        .ownState(OwnState.dataless())
        .derivedState(DerivedState.dataless())
        .build();
    assertThat(finalState.getComponent("DB").get(), is(equalTo(expectedDbState)));
  }

  @Test
  public void shouldReturnNewStateWithoutRunningIndefinitelyWhenBiDirectionalDependenciesAreFound() {
    String memoryCheckState = "memory";

    Component appComponent = Component.withId("APP");
    Component dbComponent = Component.withId("DB");
    appComponent.addDependencyOn(dbComponent);
    dbComponent.addDependencyOn(appComponent);

    StackState initialState = StackState.withComponents(appComponent, dbComponent);

    Event appMemoryWarning = Event.of("APP", memoryCheckState, StateValue.WARNING);
    Event dbMemoryAlert = Event.of("DB", memoryCheckState, StateValue.ALERT);
    EventChain eventChain = EventChain.withEvents(appMemoryWarning, dbMemoryAlert);

    StackState finalStackState = stateCalculator.processEvents(initialState, eventChain);

    Component expectedAppState = Component.builder()
        .id("APP")
        .ownState(OwnState.of(StateValue.WARNING))
        .derivedState(DerivedState.of(StateValue.ALERT))
        .checkedState(CheckedState.withJust(memoryCheckState, StateValue.WARNING))
        .build();
    assertThat(finalStackState.getComponent("APP").get(), is(equalTo(expectedAppState)));

    Component expectedDbState = Component.builder()
        .id("DB")
        .ownState(OwnState.of(StateValue.ALERT))
        .derivedState(DerivedState.of(StateValue.ALERT))
        .checkedState(CheckedState.withJust(memoryCheckState, StateValue.ALERT))
        .build();
    assertThat(finalStackState.getComponent("DB").get(), is(equalTo(expectedDbState)));
  }

  @Test
  public void shouldCalculateStateForComplexStackStateSystemsWhenOneEventIsSent() {
    String cpuLoadCheckedState = "CPU load";
    String ramUsageCheckedState = "RAM usage";
    String[] checkedStates = {cpuLoadCheckedState, ramUsageCheckedState};

    Component app = Component.withIdAndCheckedStates("APP", checkedStates);
    Component queue = Component.withIdAndCheckedStates("QUEUE", checkedStates);
    Component sqlDb = Component.withIdAndCheckedStates("SQL-DB", checkedStates);
    Component noSqlDb = Component.withIdAndCheckedStates("NOSQL-DB", checkedStates);

    app.addDependencyOn(queue, sqlDb, noSqlDb);
    queue.addDependencyOn(app);
    sqlDb.addDependencyOn(queue);
    noSqlDb.addDependencyOn(queue);

    StackState stackState = StackState.withComponents(app, queue, sqlDb, noSqlDb);

    Event sqlDbWarningEvent = Event.of(1, "SQL-DB", cpuLoadCheckedState, StateValue.WARNING);

    StackState finalStackState = stateCalculator.processEvents(stackState, EventChain.withEvent(sqlDbWarningEvent));

    Component expectedSqlDbComponentState = Component.builder()
        .id("SQL-DB")
        .ownState(OwnState.of(StateValue.WARNING))
        .derivedState(DerivedState.of(StateValue.WARNING))
        .checkedState(CheckedState
            .with(ramUsageCheckedState, StateValue.NO_DATA)
            .and(cpuLoadCheckedState, StateValue.WARNING)
            .build())
        .build();
    assertThat(finalStackState.getComponent("SQL-DB").get(), is(equalTo(expectedSqlDbComponentState)));

    Component expectedAppComponentState = Component.builder()
        .id("APP")
        .ownState(OwnState.of(StateValue.NO_DATA))
        .derivedState(DerivedState.of(StateValue.WARNING))
        .checkedState(CheckedState.dataless(checkedStates))
        .build();
    assertThat(finalStackState.getComponent("APP").get(), is(equalTo(expectedAppComponentState)));

    Component expectedNoSqlDbComponentState = Component.builder()
        .id("NOSQL-DB")
        .ownState(OwnState.of(StateValue.NO_DATA))
        .derivedState(DerivedState.of(StateValue.WARNING))
        .checkedState(CheckedState.dataless(checkedStates))
        .build();
    assertThat(finalStackState.getComponent("NOSQL-DB").get(), is(equalTo(expectedNoSqlDbComponentState)));

    Component expectedQueueComponentState = Component.builder()
        .id("QUEUE")
        .ownState(OwnState.of(StateValue.NO_DATA))
        .derivedState(DerivedState.of(StateValue.WARNING))
        .checkedState(CheckedState.dataless(checkedStates))
        .build();
    assertThat(finalStackState.getComponent("QUEUE").get(), is(equalTo(expectedQueueComponentState)));
  }

  @Test
  public void shouldCalculateStateForComplexStackStateSystemsWhenSeveralEventsAreSent() {
    String cpuLoadCheckedState = "CPU load";
    String ramUsageCheckedState = "RAM usage";
    String[] checkedStates = {cpuLoadCheckedState, ramUsageCheckedState};

    Component app = Component.withIdAndCheckedStates("APP", checkedStates);
    Component queue = Component.withIdAndCheckedStates("QUEUE", checkedStates);
    Component sqlDb = Component.withIdAndCheckedStates("SQL-DB", checkedStates);
    Component noSqlDb = Component.withIdAndCheckedStates("NOSQL-DB", checkedStates);

    app.addDependencyOn(queue, sqlDb, noSqlDb);
    queue.addDependencyOn(app);
    sqlDb.addDependencyOn(queue);
    noSqlDb.addDependencyOn(queue);

    StackState stackState = StackState.withComponents(app, queue, sqlDb, noSqlDb);

    Event sqlDbWarningEvent = Event.of(1, "SQL-DB", cpuLoadCheckedState, StateValue.WARNING);
    Event queueAlertEvent = Event.of(2, "QUEUE", cpuLoadCheckedState, StateValue.ALERT);
    Event appClearEvent = Event.of(3, "APP", ramUsageCheckedState, StateValue.CLEAR);
    Event queueWarningEvent = Event.of(4, "QUEUE", cpuLoadCheckedState, StateValue.WARNING);
    EventChain events = EventChain.withEvents(queueWarningEvent, queueAlertEvent, sqlDbWarningEvent, appClearEvent);

    StackState finalStackState = stateCalculator.processEvents(stackState, events);

    Component expectedAppComponentState = Component.builder()
        .id("APP")
        .ownState(OwnState.of(StateValue.CLEAR))
        .derivedState(DerivedState.of(StateValue.ALERT))
        .checkedState(CheckedState
            .with(ramUsageCheckedState, StateValue.CLEAR)
            .and(cpuLoadCheckedState, StateValue.NO_DATA)
            .build())
        .build();
    assertThat(finalStackState.getComponent("APP").get(), is(equalTo(expectedAppComponentState)));

    Component expectedSqlDbComponentState = Component.builder()
        .id("SQL-DB")
        .ownState(OwnState.of(StateValue.WARNING))
        .derivedState(DerivedState.of(StateValue.ALERT))
        .checkedState(CheckedState
            .with(ramUsageCheckedState, StateValue.NO_DATA)
            .and(cpuLoadCheckedState, StateValue.WARNING)
            .build())
        .build();
    assertThat(finalStackState.getComponent("SQL-DB").get(), is(equalTo(expectedSqlDbComponentState)));

    Component expectedNoSqlDbComponentState = Component.builder()
        .id("NOSQL-DB")
        .ownState(OwnState.of(StateValue.NO_DATA))
        .derivedState(DerivedState.of(StateValue.ALERT))
        .checkedState(CheckedState.dataless(checkedStates))
        .build();
    assertThat(finalStackState.getComponent("NOSQL-DB").get(), is(equalTo(expectedNoSqlDbComponentState)));

    Component expectedQueueComponentState = Component.builder()
        .id("QUEUE")
        .ownState(OwnState.of(StateValue.WARNING))
        .derivedState(DerivedState.of(StateValue.ALERT))
        .checkedState(CheckedState
            .with(ramUsageCheckedState, StateValue.NO_DATA)
            .and(cpuLoadCheckedState, StateValue.WARNING)
            .build())
        .build();
    assertThat(finalStackState.getComponent("QUEUE").get(), is(equalTo(expectedQueueComponentState)));
  }

}
