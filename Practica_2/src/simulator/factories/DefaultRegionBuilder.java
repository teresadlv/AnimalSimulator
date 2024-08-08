package simulator.factories;

import org.json.JSONObject;

import simulator.model.DefaultRegion;
import simulator.model.Region;

public class DefaultRegionBuilder extends Builder<Region> {

	public DefaultRegionBuilder() {
		super("default", "Its a default region builder");
	}

	@Override
	protected Region create_instance(JSONObject data) {

		return new DefaultRegion();
	}

	@Override
	protected void fill_in_data(JSONObject o) {
		//IMPLEMENTACION VACIA
	}
	
}
