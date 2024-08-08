package simulator.factories;

import org.json.JSONObject;

import simulator.model.SelectYoungest;
import simulator.model.SelectionStrategy;

public class SelectYoungestBuilder extends Builder<SelectionStrategy> {

	public SelectYoungestBuilder() {
		super("youngest", "Select youngest builder"); 
	}

	@Override
	protected SelectYoungest create_instance(JSONObject data) {
		return new SelectYoungest();
	}

}
