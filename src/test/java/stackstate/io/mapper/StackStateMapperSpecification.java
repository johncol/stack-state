package stackstate.io.mapper;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.Test;
import stackstate.StackState;
import stackstate.domain.Component;
import stackstate.domain.enumeration.StateValue;
import stackstate.domain.state.CheckedState;
import stackstate.domain.state.DerivedState;
import stackstate.domain.state.OwnState;
import stackstate.io.dto.state.ComponentDto;
import stackstate.io.dto.state.GraphDto;
import stackstate.io.dto.state.StackStateDto;
import stackstate.io.exception.IllegalComponentConfigurationException;

public class StackStateMapperSpecification {

  private final StackStateMapper mapper = new StackStateMapper();

  @Test(expected = IllegalComponentConfigurationException.class)
  public void shouldThrowExceptionWhenTwoOrMoreComponentsShareTheSameId() {
    mapper.map(StackStateDto.builder()
        .graph(GraphDto.builder()
            .components(List.of(
                ComponentDto.builder()
                    .id("APP")
                    .ownState("clear")
                    .derivedState("clear")
                    .checkStates(Map.of())
                    .dependencyOf(List.of())
                    .dependsOn(List.of())
                    .build(),
                ComponentDto.builder()
                    .id("APP")
                    .ownState("warning")
                    .derivedState("warning")
                    .checkStates(Map.of())
                    .dependencyOf(List.of())
                    .dependsOn(List.of())
                    .build()
            ))
            .build())
        .build());
  }

  @Test(expected = IllegalComponentConfigurationException.class)
  public void shouldThrowExceptionWhenOwnStateValueDoNotMatchAnyOfTheExpectedValues() {
    mapper.map(StackStateDto.builder()
        .graph(GraphDto.builder()
            .components(List.of(
                ComponentDto.builder()
                    .id("APP")
                    .ownState("god help us all")
                    .derivedState("clear")
                    .checkStates(Map.of())
                    .dependencyOf(List.of())
                    .dependsOn(List.of())
                    .build()
            ))
            .build())
        .build());
  }

  @Test(expected = IllegalComponentConfigurationException.class)
  public void shouldThrowExceptionWhenDerivedStateValueDoNotMatchAnyOfTheExpectedValues() {
    mapper.map(StackStateDto.builder()
        .graph(GraphDto.builder()
            .components(List.of(
                ComponentDto.builder()
                    .id("APP")
                    .derivedState("danger")
                    .ownState("clear")
                    .checkStates(Map.of())
                    .dependencyOf(List.of())
                    .dependsOn(List.of())
                    .build()
            ))
            .build())
        .build());
  }

  @Test(expected = IllegalComponentConfigurationException.class)
  public void shouldThrowExceptionWhenACheckedStateValueDoNotMatchAnyOfTheExpectedValues() {
    mapper.map(StackStateDto.builder()
        .graph(GraphDto.builder()
            .components(List.of(
                ComponentDto.builder()
                    .id("APP")
                    .derivedState("no_data")
                    .ownState("no_data")
                    .checkStates(Map.of("temperature", "really high"))
                    .dependencyOf(List.of())
                    .dependsOn(List.of())
                    .build()
            ))
            .build())
        .build());
  }

  @Test
  public void shouldMapStackState() {
    StackState stackState = mapper.map(StackStateDto.builder()
        .graph(GraphDto.builder()
            .components(List.of(
                ComponentDto.builder()
                    .id("APP")
                    .ownState("clear")
                    .derivedState("clear")
                    .checkStates(Map.of("memory", "clear"))
                    .dependencyOf(List.of())
                    .dependsOn(List.of())
                    .build(),
                ComponentDto.builder()
                    .id("DB")
                    .ownState("warning")
                    .derivedState("alert")
                    .checkStates(Map.of("memory", "warning"))
                    .dependencyOf(List.of())
                    .dependsOn(List.of())
                    .build()
            ))
            .build())
        .build());

    assertThat(stackState.size(), is(equalTo(2)));

    assertThat(stackState.getComponent("APP").get(), is(equalTo(Component.builder()
        .id("APP")
        .ownState(OwnState.of(StateValue.CLEAR))
        .derivedState(DerivedState.of(StateValue.CLEAR))
        .checkedState(CheckedState.withJust("memory", StateValue.CLEAR))
        .build())));

    assertThat(stackState.getComponent("DB").get(), is(equalTo(Component.builder()
        .id("DB")
        .ownState(OwnState.of(StateValue.WARNING))
        .derivedState(DerivedState.of(StateValue.ALERT))
        .checkedState(CheckedState.withJust("memory", StateValue.WARNING))
        .build())));
  }

}
