package stackstate.io.mapper;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;
import org.junit.Test;
import stackstate.domain.enumeration.StateValue;
import stackstate.domain.event.Event;
import stackstate.domain.event.EventChain;
import stackstate.io.dto.event.EventDto;
import stackstate.io.dto.event.StackEventDto;
import stackstate.io.exception.IllegalEventConfigurationException;

public class StackEventMapperSpecification {

  private final StackEventMapper mapper = new StackEventMapper();

  @Test(expected = IllegalEventConfigurationException.class)
  public void shouldThrowExceptionWhenTwoOrMoreEventsShareTheSameTimestamp() {
    StackEventDto dto = StackEventDto.builder()
        .events(List.of(
            EventDto.builder()
                .component("APP")
                .timestamp("1")
                .checkState("memory")
                .state("warning")
                .build(),
            EventDto.builder()
                .component("DB")
                .timestamp("1")
                .checkState("CPU usage")
                .state("clear")
                .build()))
        .build();

    mapper.map(dto);
  }

  @Test(expected = IllegalEventConfigurationException.class)
  public void shouldThrowExceptionWhenStateValueDoNotMatchAnyOfTheExpectedValues() {
    StackEventDto dto = StackEventDto.builder()
        .events(List.of(
            EventDto.builder()
                .component("APP")
                .timestamp("1")
                .checkState("memory")
                .state("unknown")
                .build()))
        .build();

    mapper.map(dto);
  }

  @Test(expected = IllegalEventConfigurationException.class)
  public void shouldThrowExceptionWhenTimestampIsNotANumber() {
    StackEventDto dto = StackEventDto.builder()
        .events(List.of(
            EventDto.builder()
                .component("APP")
                .timestamp("one")
                .checkState("memory")
                .state("unknown")
                .build()))
        .build();

    mapper.map(dto);
  }

  @Test
  public void shouldMapEventChain() {
    StackEventDto dto = StackEventDto.builder()
        .events(List.of(
            EventDto.builder()
                .component("APP")
                .timestamp("1")
                .checkState("memory")
                .state("warning")
                .build(),
            EventDto.builder()
                .component("DB")
                .timestamp("2")
                .checkState("CPU usage")
                .state("clear")
                .build()))
        .build();

    EventChain events = mapper.map(dto);

    assertThat(events.size(), is(equalTo(2)));

    assertThat(events.getEvent(0), is(equalTo(Event.builder()
        .component("APP")
        .timestamp(1)
        .checkState("memory")
        .state(StateValue.WARNING)
        .build())));

    assertThat(events.getEvent(1), is(equalTo(Event.builder()
        .component("DB")
        .timestamp(2)
        .checkState("CPU usage")
        .state(StateValue.CLEAR)
        .build())));
  }

}
