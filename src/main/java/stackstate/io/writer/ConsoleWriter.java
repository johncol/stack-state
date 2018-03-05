package stackstate.io.writer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import stackstate.StackState;
import stackstate.io.dto.state.StackStateDto;
import stackstate.io.mapper.StackStateMapper;

@AllArgsConstructor
public class ConsoleWriter implements StackStateWriter {

  private final StackStateMapper stackStateMapper = new StackStateMapper();

  private final StackState stackState;
  private final ObjectMapper objectMapper;

  @Override
  public void write() {
    try {
      StackStateDto stackStateDto = stackStateMapper.map(stackState);
      String result = objectMapper.writeValueAsString(stackStateDto);
      System.out.println(result);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
  }
}
