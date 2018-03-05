package stackstate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Builder;
import stackstate.domain.Component;

@Builder
@AllArgsConstructor
public class StackState {

  private final List<Component> components;

  public static StackState withComponent(Component component) {
    return withComponents(component);
  }

  public static StackState withComponents(Component... components) {
    return StackState.withComponents(Arrays.asList(components));
  }

  public static StackState withComponents(List<Component> components) {
    return StackState.builder()
        .components(components)
        .build();
  }

  public Optional<Component> getComponent(String componentId) {
    return components.stream()
        .filter(component -> component.getId().equals(componentId))
        .findFirst();
  }

  public int size() {
    return components.size();
  }

  public Stream<Component> stream() {
    return components.stream();
  }

}
