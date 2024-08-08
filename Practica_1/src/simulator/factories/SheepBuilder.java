package simulator.factories;

import org.json.JSONArray;
import org.json.JSONObject;

import simulator.misc.Utils;
import simulator.misc.Vector2D;
import simulator.model.*;

public class SheepBuilder extends Builder<Animal> {

	Factory<SelectionStrategy> _selection_strategy_factory;

	public SheepBuilder(Factory<SelectionStrategy> selection_strategy_factory) {
		super("sheep", "its a sheep builder");
		this._selection_strategy_factory = selection_strategy_factory;
	}

	@Override
	protected Sheep create_instance(JSONObject data) {
		double minX = 0.0, minY = 0.0;
		double maxX = 0.0, maxY = 0.0;
		double x = 0.0, y = 0.0;
		Vector2D position;

		/*
		 * USAMOS OPTJSONOBJECT YA QUE TANTO POS, MATE_STRATEGY Y DANGER_STRATEGY SON
		 * OPCIONALES, POR LO QUE, SI ES EL CASO, DEVOLVERA NULL Y PODREMOS ASIGNARLE
		 * LOS VALORES CORRESPONDIENTES
		 */
		SelectionStrategy mate_strategy;
		JSONObject mate_strategy_JSON = data.optJSONObject("mate_strategy");
		if (mate_strategy_JSON == null) {
			mate_strategy = new SelectFirst();
		} else {
			mate_strategy = _selection_strategy_factory.create_instance(mate_strategy_JSON);
		}

		SelectionStrategy danger_strategy;
		JSONObject danger_strategy_JSON = data.optJSONObject("danger_strategy");
		if (danger_strategy_JSON == null) {
			danger_strategy = new SelectFirst();
		} else {
			danger_strategy = _selection_strategy_factory.create_instance(danger_strategy_JSON);
		}

		JSONObject pos = data.optJSONObject("pos");

		if (pos != null) {
			JSONArray x_range = pos.getJSONArray("x_range");
			minX = x_range.getDouble(0);
			maxX = x_range.getDouble(1);

			x = Utils._rand.nextDouble(minX,maxX);

			JSONArray y_range = pos.getJSONArray("y_range");
			minY = y_range.getDouble(0);
			maxY = y_range.getDouble(1);

			y = Utils._rand.nextDouble(minY,maxY);

			position = new Vector2D(x, y);

		} else {
			position = null;
		}

		return new Sheep(mate_strategy, danger_strategy, position);
	}

}
