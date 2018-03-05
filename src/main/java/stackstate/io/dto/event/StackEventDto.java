package stackstate.io.dto.event;

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
@NoArgsConstructor
@AllArgsConstructor
public class StackEventDto implements Serializable {

  private static final long serialVersionUID = -3884923158196771623L;

  private List<EventDto> events;

}
