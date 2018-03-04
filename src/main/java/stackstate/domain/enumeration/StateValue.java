package stackstate.domain.enumeration;

import java.util.Comparator;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum StateValue {
  NO_DATA(0),
  CLEAR(1),
  WARNING(2),
  ALERT(3),;

  public static final Comparator<StateValue> COMPARATOR = new StateValueComparator();
  public static final Comparator<StateValue> REVERSED_COMPARATOR = COMPARATOR.reversed();

  private final int severity;

  public static StateValue highestOf(StateValue state1, StateValue state2) {
    return state1.severity > state2.severity ? state1 : state2;
  }

  public boolean warningOrHigher() {
    return severity >= WARNING.severity;
  }

  private static class StateValueComparator implements Comparator<StateValue> {

    @Override
    public int compare(StateValue state1, StateValue state2) {
      return Integer.compare(state1.severity, state2.severity);
    }
  }

}
