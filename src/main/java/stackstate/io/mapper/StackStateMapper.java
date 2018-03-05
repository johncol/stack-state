package stackstate.io.mapper;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import stackstate.StackState;
import stackstate.domain.Component;
import stackstate.domain.enumeration.StateValue;
import stackstate.domain.state.CheckedState;
import stackstate.domain.state.DerivedState;
import stackstate.domain.state.OwnState;
import stackstate.io.dto.state.ComponentDto;
import stackstate.io.dto.state.StackStateDto;
import stackstate.io.exception.IllegalComponentConfigurationException;

public class StackStateMapper {

  public StackState map(StackStateDto dto) {
    List<Component> components = dto.componentsStream()
        .map(this::mapToComponent)
        .collect(Collectors.toList());
    Map<String, ComponentDto> idToComponentDtoMap = buildIdToComponentMap(dto.componentsStream(), ComponentDto::getId);
    Map<String, Component> idToComponentMap = buildIdToComponentMap(components.stream(), Component::getId);

    components.forEach(component ->
        idToComponentDtoMap.get(component.getId())
            .getDependsOn()
            .stream()
            .map(idToComponentMap::get)
            .forEach(component::addDependencyOn));

    return StackState.withComponents(components);
  }

  private Component mapToComponent(ComponentDto componentDto) {
    return Component.builder()
        .id(componentDto.getId())
        .checkedState(mapToCheckedState(componentDto))
        .ownState(OwnState.of(mapToStateValue(componentDto.getOwnState())))
        .derivedState(DerivedState.of(mapToStateValue(componentDto.getDerivedState())))
        .dependencies(new HashSet<>())
        .dependents(new HashSet<>())
        .build();
  }

  private CheckedState mapToCheckedState(ComponentDto componentDto) {
    Map<String, String> checkStates = componentDto.getCheckStates();
    CheckedState.Builder builder = CheckedState.builder();
    checkStates.entrySet()
        .forEach(entry -> builder.and(entry.getKey(), mapToStateValue(entry.getValue())));
    return builder.build();
  }

  private StateValue mapToStateValue(String state) {
    try {
      StateValue stateValue = StateValue.valueOf(state.toUpperCase());
      return stateValue;
    } catch (IllegalArgumentException e) {
      throw new IllegalComponentConfigurationException("State '" + state + "' is not a valid state");
    }
  }

  private <T> Map<String, T> buildIdToComponentMap(Stream<T> components, Function<T, String> keyGenerator) {
    try {
      return components.collect(Collectors.toMap(keyGenerator, Function.identity()));
    } catch (IllegalStateException e) {
      throw new IllegalComponentConfigurationException("It's not allowed to have more than one component with the same ID");
    }
  }

}
