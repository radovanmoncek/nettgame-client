package cz.radovanmoncek.ship.parents.builders;

public interface Builder<T> {

    T build();

    Builder<T> reset();
}
