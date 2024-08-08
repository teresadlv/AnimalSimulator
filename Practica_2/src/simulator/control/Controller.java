package simulator.control;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;

import simulator.model.*;
import simulator.view.SimpleObjectViewer;
import simulator.view.SimpleObjectViewer.ObjInfo;

public class Controller {

	Simulator _sim;

	public Controller(Simulator sim) {
		this._sim = sim;
	}

	public void load_data(JSONObject data) {

		// REGIONES
		if (data.has("regions")) {
			set_regions(data);
		}

		// ANIMALES
		JSONArray animals_JSONArray = data.getJSONArray("animals");

		for (int i = 0; i < animals_JSONArray.length(); i++) {
			int amount = animals_JSONArray.getJSONObject(i).getInt("amount");
			JSONObject a_json = animals_JSONArray.getJSONObject(i);
			for (int j = 0; j < amount; j++) {
				this._sim.add_animal(a_json.getJSONObject("spec"));
			}
		}

	}

	public void run(double t, double dt, boolean sv, OutputStream out) {
		SimpleObjectViewer view = null;
		JSONObject controller_JSON = new JSONObject();
		controller_JSON.put("in", this._sim.as_JSON());
		if (sv) {
			MapInfo m = _sim.get_map_info();
			view = new SimpleObjectViewer("[ECOSYSTEM]", m.get_width(), m.get_height(), m.get_cols(), m.get_rows());
			view.update(this.to_animals_info(this._sim.get_animals()), this._sim.get_time(), dt);
		}
		while (this._sim.get_time() <= t) {
			this._sim.advance(dt);
			view.update(this.to_animals_info(this._sim.get_animals()), this._sim.get_time(), dt);
		}
		controller_JSON.put("out", this._sim.as_JSON());
		PrintStream p = new PrintStream(out);
		p.println(controller_JSON);
	}

	private List<ObjInfo> to_animals_info(List<? extends Animalnfo> animals) {
		List<ObjInfo> ol = new ArrayList<>(animals.size());
		for (Animalnfo a : animals)
			ol.add(new ObjInfo(a.get_genetic_code(), (int) a.get_position().getX(), (int) a.get_position().getY(),
					(int) Math.round(a.get_age()) + 2));
		return ol;
	}

	public void reset(int cols, int rows, int width, int height) {
		_sim.reset(cols, rows, width, height);
	}

	public void set_regions(JSONObject rs) {
		JSONArray regions = rs.getJSONArray("regions");
		// FOR POR SI HAY MAS DE UNA
		for (int i = 0; i < regions.length(); i++) {
			JSONObject regionObject = regions.getJSONObject(i);

			// OBTENEMOS LOS VALORES DE ROW, COL Y SPEC
			JSONArray row = regionObject.getJSONArray("row");
			JSONArray col = regionObject.getJSONArray("col");
			JSONObject spec = regionObject.getJSONObject("spec");

			int rows_min = row.getInt(0);
			int rows_max = row.getInt(1);
			int col_min = col.getInt(0);
			int col_max = col.getInt(1);
			// BUCLE ANIDADO PARA LLAMAR A _SIM._SET-REGION CON RF<=R<=RT Y CF<=C<=CT
			for (int j = rows_min; j <= rows_max; j++) {
				for (int k = col_min; k <= col_max; k++) {
					_sim._set_region(j, k, spec);
				}
			}
		}

	}

	public void advance(double dt) {
		_sim.advance(dt);
	}

	public void addObserver(EcoSysObserver o) {
		_sim.addObserver(o);
	}

	public void removeObserver(EcoSysObserver o) {
		_sim.removeObserver(o);
	}

}
