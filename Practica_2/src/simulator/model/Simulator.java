package simulator.model;

import java.util.List;
import java.util.ArrayList;

import org.json.JSONObject;

import simulator.factories.*;
import java.util.LinkedList;

public class Simulator implements JSONable, Observable<EcoSysObserver> {

	Factory<Animal> _animal_factory;
	Factory<Region> _regions_factory;
	RegionManager _region_manager;
	List<Animal> _animal_list;
	double _current_time;
	List<EcoSysObserver> _observer_list;

	public Simulator(int cols, int rows, int width, int height, Factory<Animal> animals_factory,
			Factory<Region> regions_factory) {
		this._animal_factory = animals_factory;
		this._regions_factory = regions_factory;
		this._region_manager = new RegionManager(cols, rows, width, height);
		this._animal_list = new LinkedList<>();
		this._observer_list = new LinkedList<>();
		this._current_time = 0.0;
	}

	private void set_region(int row, int col, Region r) {
		_region_manager.set_region(row, col, r);

	}

	public void _set_region(int row, int col, JSONObject r_json) {
		Region region = _regions_factory.create_instance(r_json);

		set_region(row, col, region);

		// LLAMO A ONREGIONSET PARA TODOS LOS OBSERVADORES EN OBSERVER LIST
		notify_on_region_set(row, col, region);

	}

	public void add_animal(JSONObject a_json) {
		Animal animal = _animal_factory.create_instance(a_json);
		add_animal(animal);

		// LLAMO A ONANIMALADDED PARA TODOS LOS OBSERVADORES EN OBSERVER LIST
		notify_on_animal_added(animal);
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
		for (Animal a : _animal_list) {
			a.update(dt);
			_region_manager.update_animal_region(a);
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

		remove_dead_animals();
		update_an_animal(dt);
		this._region_manager.update_all_regions(dt);
		give_birth();

		// LLAMO A ONADVANCE PARA TODOS LOS OBSERVADORES EN OBSERVER LIST
		notify_on_advance(dt);
	}

	public JSONObject as_JSON() {
		JSONObject sim_JSON = new JSONObject();
		sim_JSON.put("time", this._current_time);
		sim_JSON.put("state", this._region_manager.as_JSON());
		return sim_JSON;
	}

	public void reset(int cols, int rows, int width, int height) {
		// VACIA LA LISTA DE ANIMALES
		this._animal_list.clear();
		// CREA UN NUEVO REGIONMANAGER
		this._region_manager = new RegionManager(cols, rows, width, height);
		// TIEMPO A 0.0
		this._current_time = 0.0;
		// LLAMO A ONRESET PARA TODOS LOS OBSERVERS DE LA LISTA
		notify_on_reset();
	}

	@Override
	public void addObserver(EcoSysObserver o) {

		if (this._observer_list.contains(o))
			throw new IllegalArgumentException("Observer already added to the list");

		this._observer_list.add(o);

		notify_on_register(o);

	}

	@Override
	public void removeObserver(EcoSysObserver o) {
		if (!this._observer_list.contains(o))
			throw new IllegalArgumentException("Observer not added to the list");

		this._observer_list.remove(o);
		notify_on_register(o);
	}

	// METODOS PARA LOS OBSERVERS

	private void notify_on_advance(double dt) {
		List<Animalnfo> animals = new ArrayList<>(_animal_list);
		for (EcoSysObserver o : _observer_list) {
			o.onAdvance(_current_time, _region_manager, animals, dt);
		}
	}

	private void notify_on_register(EcoSysObserver o) {
		List<Animalnfo> animals = new ArrayList<>(_animal_list);
		o.onRegister(_current_time, _region_manager, animals);
	}

	private void notify_on_reset() {
		List<Animalnfo> animals = new ArrayList<>(_animal_list);
		for (EcoSysObserver o : _observer_list) {
			o.onReset(_current_time, _region_manager, animals);
		}
	}

	private void notify_on_animal_added(Animal a) {
		List<Animalnfo> animals = new ArrayList<>(_animal_list);
		for (EcoSysObserver o : _observer_list) {
			o.onAnimalAdded(_current_time, _region_manager, animals, a);
		}
	}

	private void notify_on_region_set(int row, int col, Region r) {
		for (EcoSysObserver o : _observer_list) {
			o.onRegionSet(row, col, _region_manager, r);
		}
	}

}
