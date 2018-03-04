package stackstate.domain.state;

public interface UpdatableState<T, U extends UpdatableState> {

  /**
   * Updates the current state
   *
   * @param stateModifier data from which the current state will be updated
   * @return old state
   */
  U updateGiven(T stateModifier);

}
