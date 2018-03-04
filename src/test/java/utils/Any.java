package utils;

import com.github.javafaker.Faker;
import com.github.javafaker.GameOfThrones;
import stackstate.domain.Component;
import stackstate.domain.enumeration.StateValue;
import stackstate.domain.event.Event;
import stackstate.domain.state.CheckedState;
import stackstate.domain.state.DerivedState;
import stackstate.domain.state.OwnState;
import java.util.HashSet;
import java.util.Random;
import java.util.stream.Stream;

public class Any {

  private final static Faker faker = new Faker();

  public static <T> T of(T... values) {
    return values[new Random().nextInt(values.length)];
  }

  public static Event event() {
    return Event.of(faker.app().name(), faker.beer().malt(), Any.of(StateValue.values()));
  }

  public static OwnState ownState() {
    return OwnState.of(Any.of(StateValue.values()));
  }

  public static DerivedState derivedState() {
    return DerivedState.of(Any.of(StateValue.values()));
  }

  public static CheckedState checkedState() {
    CheckedState checkedState = CheckedState.dataless();
    GameOfThrones gameOfThrones = faker.gameOfThrones();
    Stream.generate(gameOfThrones::character)
        .limit(3)
        .forEach(character -> checkedState.and(character, Any.of(StateValue.values())));
    return checkedState;
  }

  public static Component componentWithDerivedState(StateValue derivedState) {
    return Component.builder()
        .id(faker.app().name())
        .ownState(Any.ownState())
        .checkedState(Any.checkedState())
        .derivedState(DerivedState.of(derivedState))
        .dependencies(new HashSet<>())
        .dependents(new HashSet<>())
        .build();
  }

}
