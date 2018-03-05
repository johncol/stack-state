package stackstate.io.dto.state;

import java.io.Serializable;
import java.util.List;
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
public class GraphDto implements Serializable {

  private static final long serialVersionUID = 7652850273756322349L;

  private List<ComponentDto> components;

}
