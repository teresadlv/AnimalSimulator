package simulator.model;

import simulator.misc.Utils;

public class DynamicSupplyRegion extends Region implements FoodSupplier, Entity {

	private static final double FIRST_PARAMETER = 60.0;
	private static final double SECOND_PARAMETER = 5.0;
	private static final double THIRD_PARAMETER = 2.0;
	private static final double FOOD_SUPPLY_PROBABILITY = 0.5;

	private double _food;
	private double _factor;

	public DynamicSupplyRegion(double initial_food_amount, double growth_factor) {
		if (initial_food_amount <= 0)
			throw new IllegalArgumentException("Initial food amount must be a positive number");
		else if (growth_factor < 0)
			throw new IllegalArgumentException("Growth factor can't be a negative number");

		this._food = initial_food_amount;
		this._factor = growth_factor;
	}

	@Override
	public double get_food(Animal a, double dt) {

		if (a.get_diet() != Diet.HERBIVORE) {
			return 0.0;
		} else {
			double val_to_return = Math.min(_food, FIRST_PARAMETER
					* Math.exp(-Math.max(0, get_amount_animals_by_diet(a.get_diet()) - SECOND_PARAMETER) * THIRD_PARAMETER) * dt);
			decrease_Food(val_to_return);
			return val_to_return;

		}
	}

	@Override
	public void update(double dt) {
		if (Utils._rand.nextDouble() > FOOD_SUPPLY_PROBABILITY)
			increase_Food(dt);

	}

	private void decrease_Food(double value) {
		this._food -= value;
	}

	private void increase_Food(double dt) {
		this._food += (dt * _factor);
	}
	
	@Override
	public String toString() {
		return "Dynamic region";
	}

}
