package stackstate.io.dto.state;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@JsonInclude(Include.NON_NULL)
@EqualsAndHashCode
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ComponentDto implements Serializable {

  private static final long serialVersionUID = 7076851739813044026L;

  private String id;

  private String ownState;

  private String derivedState;

  private Map<String, String> checkStates = new HashMap<>();

  private List<String> dependsOn = new ArrayList<>();

  private List<String> dependencyOf = new ArrayList<>();

}
