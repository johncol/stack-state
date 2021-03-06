package stackstate.io.reader;

import stackstate.StackState;
import stackstate.domain.event.EventChain;

public interface StackStateReader {

  StackState readInitialState();

  EventChain readEvents();

}
