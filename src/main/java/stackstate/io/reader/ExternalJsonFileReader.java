package stackstate.io.reader;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import lombok.AllArgsConstructor;
import stackstate.StackState;
import stackstate.domain.event.EventChain;
import stackstate.io.dto.event.StackEventDto;
import stackstate.io.dto.state.StackStateDto;
import stackstate.io.mapper.StackEventMapper;
import stackstate.io.mapper.StackStateMapper;

@AllArgsConstructor
public class ExternalJsonFileReader implements StackStateReader {

  private final StackStateMapper stackStateMapper = new StackStateMapper();
  private final StackEventMapper stackEventMapper = new StackEventMapper();

  private final String stateFilePath;
  private final String eventsFilePath;
  private final ObjectMapper objectMapper;

  @Override
  public StackState readInitialState() {
    StackStateDto stackStateDto = read(() -> {
      File file = new File(stateFilePath);
      return objectMapper.readValue(file, StackStateDto.class);
    });
    return stackStateMapper.map(stackStateDto);
  }

  @Override
  public EventChain readEvents() {
    StackEventDto stackStateDto = read(() -> {
      File file = new File(eventsFilePath);
      return objectMapper.readValue(file, StackEventDto.class);
    });
    return stackEventMapper.map(stackStateDto);
  }

  private <T> T read(InputOutputDtoSupplier<T> readSupplier) {
    try {
      return readSupplier.get();
    } catch (JsonMappingException e) {
      e.printStackTrace();
      throw new IllegalArgumentException("Error mapping json" + ": " + e.getMessage());
    } catch (JsonParseException e) {
      e.printStackTrace();
      throw new IllegalArgumentException("Error parsing json" + ": " + e.getMessage());
    } catch (IOException e) {
      e.printStackTrace();
      throw new IllegalArgumentException("Error reading json" + ": " + e.getMessage());
    }
  }

}

@FunctionalInterface
interface InputOutputDtoSupplier<T> {
  T get() throws IOException;
}
