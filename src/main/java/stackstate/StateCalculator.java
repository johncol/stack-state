package stackstate;

import stackstate.domain.event.EventChain;

public class StateCalculator {

  public StackState processEvents(StackState initialState, EventChain eventChain) {
    eventChain.forEach(event ->
        initialState
            .getComponent(event.getComponent())
            .ifPresent(component -> component.apply(event))
    );
    return initialState;
  }
}
