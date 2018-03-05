package stackstate.io.dto.event;

import java.io.Serializable;
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
@NoArgsConstructor
@AllArgsConstructor
public class EventDto implements Serializable {

  private static final long serialVersionUID = 4794750781477957222L;

  private String timestamp;

  private String component;

  private String checkState;

  private String state;

}
