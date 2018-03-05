package stackstate.io.dto.state;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StackStateDto implements Serializable {

  private static final long serialVersionUID = -8620024090981235897L;

  private GraphDto graph;

  @JsonIgnore
  public Stream<ComponentDto> componentsStream() {
    return getGraph().getComponents().stream();
  }

}
