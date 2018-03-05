package stackstate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import stackstate.domain.event.EventChain;
import stackstate.io.reader.ExternalJsonFileReader;
import stackstate.io.reader.StackStateReader;
import stackstate.io.writer.ConsoleWriter;
import stackstate.io.writer.StackStateWriter;

public class Application {

  private static final ObjectMapper objectMapper = new ObjectMapper()
      .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
      .configure(SerializationFeature.INDENT_OUTPUT, true);

  public static void main(String[] args) {
    if (args.length < 2) {
      throw new IllegalArgumentException("Two json files are required");
    }

    StackStateReader reader = new ExternalJsonFileReader(args[0], args[1], objectMapper);

    StackState stackState = reader.readInitialState();
    EventChain events = reader.readEvents();
    StackState finalState = new StateCalculator().processEvents(stackState, events);

    StackStateWriter writer = new ConsoleWriter(finalState, objectMapper);
    writer.write();
  }

}
