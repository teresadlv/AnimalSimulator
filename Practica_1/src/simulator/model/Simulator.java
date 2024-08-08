package simulator.model;

import java.util.List;

import org.json.JSONObject;

import simulator.factories.*;
import java.util.LinkedList;

public class Simulator implements JSONable {

	Factory<Animal> _animal_factory;
	Factory<Region> _regions_factory;
	RegionManager _region_manager;
	List<Animal> _animal_list;
	double _current_time;

	public Simulator(int cols, int rows, int width, int height, Factory<Animal> animals_factory,
			Factory<Region> regions_factory) {
		this._animal_factory = animals_factory;
		this._regions_factory = regions_factory;
		this._region_manager = new RegionManager(cols, rows, width, height);
		this._animal_list = new LinkedList<>();
		this._current_time = 0.0;
	}

	private void set_region(int row, int col, Region r) {
		_region_manager.set_region(row, col, r);

	}

	public void _set_region(int row, int col, JSONObject r_json) {
		Region region = _regions_factory.create_instance(r_json);
		;
		set_region(row, col, region);

	}

	public void add_animal(JSONObject a_json) {
		Animal animal = _animal_factory.create_instance(a_json);
		add_animal(animal);

	}

	private void add_animal(Animal a) {
		this._animal_list.add(a);
		_region_manager.register_animal(a);
	}

	public MapInfo get_map_info() {
		return _region_manager; // DEVUELVE GESTOR DE REGIONES

	}

	public List<? extends Animalnfo> get_animals() { // DEVUELVE LISTA DE ANIMALES
		return this._animal_list;

	}

	public double get_time() {
		return this._current_time;

	}

	private void update_an_animal(double dt) {
		for(Animal a: _animal_list) {
			a.update(dt);
		}
	}

	private void remove_dead_animals() {
		List<Animal> dead_list = new LinkedList<>();
		for (Animal a : _animal_list) {
			if (a.get_state() == State.DEAD) {
				this._region_manager.unregister_animal(a);
				dead_list.add(a);
			}
		}
		_animal_list.removeAll(dead_list);
	}

	private void give_birth() {
		List<Animal> new_list = new LinkedList<>();
		for (Animal a : _animal_list) {
			if (a.is_pregnent())
				new_list.add(a.deliver_baby());
		}
		for (Animal a : new_list) {
			add_animal(a);
		}

	}

	public void advance(double dt) {
		this._current_time += dt;
		// SI NO SE PUEDE MODIFICAR LA LISTA, COMO SE HACE?
		remove_dead_animals();
		update_an_animal(dt);
		give_birth();
		this._region_manager.update_all_regions(dt);

	}

	public JSONObject as_JSON() {
		JSONObject sim_JSON = new JSONObject();
		sim_JSON.put("time", this._current_time);
		sim_JSON.put("state", this._region_manager.as_JSON());
		return sim_JSON;
	}

}
