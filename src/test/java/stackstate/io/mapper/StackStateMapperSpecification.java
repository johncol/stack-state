package stackstate.io.mapper;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

  @Test
  public void shouldMapStackStateDto() {
    Component appComponent = Component.builder()
        .id("app")
        .ownState(OwnState.of(StateValue.CLEAR))
        .derivedState(DerivedState.of(StateValue.CLEAR))
        .checkedState(CheckedState.withJust("memory", StateValue.CLEAR))
        .dependencies(new HashSet<>())
        .dependents(new HashSet<>())
        .build();
    Component dbComponent = Component.builder()
        .id("db")
        .ownState(OwnState.of(StateValue.WARNING))
        .derivedState(DerivedState.of(StateValue.ALERT))
        .checkedState(CheckedState.withJust("memory", StateValue.WARNING))
        .dependencies(new HashSet<>())
        .dependents(new HashSet<>())
        .build();
    appComponent.addDependencyOn(dbComponent);

    StackState stackState = StackState.withComponents(appComponent, dbComponent);

    StackStateDto dto = mapper.map(stackState);
    List<ComponentDto> components = dto.getGraph().getComponents();

    assertThat(components, hasSize(2));

    ComponentDto appComponentDto = ComponentDto.builder()
        .id("app")
        .ownState("clear")
        .derivedState("clear")
        .checkStates(Map.of("memory", "clear"))
        .dependsOn(List.of("db"))
        .dependencyOf(List.of())
        .build();
    assertThat(components.get(0), is(equalTo(appComponentDto)));

    ComponentDto dbComponentDto = ComponentDto.builder()
        .id("db")
        .ownState("warning")
        .derivedState("alert")
        .checkStates(Map.of("memory", "warning"))
        .dependsOn(List.of())
        .dependencyOf(List.of("app"))
        .build();
    assertThat(components.get(1), is(equalTo(dbComponentDto)));
  }

}
