package simulator.factories;

import org.json.JSONObject;

import simulator.model.*;

public class DynamicSupplyRegionBuilder extends Builder<Region> {

	public DynamicSupplyRegionBuilder() {
		super("dynamic", "Dynamic supply builder");
	}

	@Override
	protected DynamicSupplyRegion create_instance(JSONObject data) {
		double factor = data.optDouble("factor", 1000.0);
		double food = data.optDouble("food", 2.0);

		return new DynamicSupplyRegion(factor, food);
	}

}
