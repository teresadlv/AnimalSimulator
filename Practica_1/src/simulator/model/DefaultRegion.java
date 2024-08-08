package simulator.model;

public class DefaultRegion extends Region implements FoodSupplier, Entity {

	private static final double FIRST_PARAMETER = 60.0;
	private static final double SECOND_PARAMETER = 5.0;
	private static final double THIRD_PARAMETER = 2.0;

	@Override
	public double get_food(Animal a, double dt) {
		if (a.get_diet() != Diet.HERBIVORE)
			return 0.0;

		else
			return FIRST_PARAMETER * Math.exp(-Math.max(0, get_amount_animals_by_diet(a.get_diet()) - SECOND_PARAMETER) * THIRD_PARAMETER) * dt;

	}

	@Override
	public void update(double dt) {
		// IMPLEMENTACION VACIA
	}

}
