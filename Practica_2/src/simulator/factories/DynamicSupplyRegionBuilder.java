package simulator.factories;

import org.json.JSONObject;

import simulator.model.*;

public class DynamicSupplyRegionBuilder extends Builder<Region> {
	
	public static final String FACTOR_INFO = "food increase factor (optional, default 2.0)";
	public static final String FOOD_INFO = "initial amount of food (optional, default 100.0";

	public DynamicSupplyRegionBuilder() {
		super("dynamic", "Dynamic supply builder");
	}

	@Override
	protected DynamicSupplyRegion create_instance(JSONObject data) {
		double factor = data.optDouble("factor", 1000.0);
		double food = data.optDouble("food", 2.0);

		return new DynamicSupplyRegion(factor, food);
	}
	
	@Override
	protected void fill_in_data(JSONObject o) {
		o.put("factor", FACTOR_INFO);
		o.put("food", FOOD_INFO);
	}

}
