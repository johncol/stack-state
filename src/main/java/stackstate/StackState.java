package stackstate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import stackstate.domain.Component;

@Builder
@AllArgsConstructor
public class StackState {

  private List<Component> components;

  public static StackState withComponent(Component component) {
    return withComponents(component);
  }

  public static StackState withComponents(Component... components) {
    return StackState.builder()
        .components(Arrays.asList(components))
        .build();
  }

  public Optional<Component> getComponent(String componentId) {
    return components.stream()
        .filter(component -> component.getId().equals(componentId))
        .findFirst();
  }
}
