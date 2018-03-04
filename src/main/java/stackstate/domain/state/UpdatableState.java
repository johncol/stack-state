package stackstate.domain.state;

public interface UpdatableState<T> {

  void updateGiven(T stateModifier);

}
