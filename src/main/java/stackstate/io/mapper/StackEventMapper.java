package stackstate.io.mapper;

import java.util.List;
import java.util.stream.Collectors;
import stackstate.domain.enumeration.StateValue;
import stackstate.domain.event.Event;
import stackstate.domain.event.EventChain;
import stackstate.io.dto.event.EventDto;
import stackstate.io.dto.event.StackEventDto;
import stackstate.io.exception.IllegalEventConfigurationException;

public class StackEventMapper {

  public EventChain map(StackEventDto dto) {
    List<Event> events = dto.getEvents()
        .stream()
        .map(event -> Event.builder()
            .component(event.getComponent())
            .timestamp(mapToTimestamp(event))
            .checkState(event.getCheckState())
            .state(mapToStateValue(event.getState()))
            .build())
        .collect(Collectors.toList());

    if (events.stream().map(Event::getTimestamp).distinct().count() < events.size()) {
      throw new IllegalEventConfigurationException("Timestamp number must be unique across the event chain");
    }

    return EventChain.withEvents(events);
  }

  private long mapToTimestamp(EventDto event) {
    try {
      return Long.parseLong(event.getTimestamp());
    } catch (NumberFormatException e) {
      throw new IllegalEventConfigurationException("Timestamp needs to be a number");
    }
  }

  private StateValue mapToStateValue(String state) {
    try {
      StateValue stateValue = StateValue.valueOf(state.toUpperCase());
      return stateValue;
    } catch (IllegalArgumentException e) {
      throw new IllegalEventConfigurationException("State '" + state + "' is not a valid state");
    }
  }
}
