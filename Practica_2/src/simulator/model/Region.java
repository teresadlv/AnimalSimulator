package simulator.model;

import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;

import org.json.JSONObject;
import org.json.JSONArray;

public abstract class Region implements Entity, FoodSupplier, RegionInfo {

	protected List<Animal> _list_animals;

	public Region() {
		_list_animals = new LinkedList<>();
	}

	// METODOS ABSTRACTOS
	@Override
	public abstract void update(double dt);

	@Override
	public abstract double get_food(Animal a, double dt);

	final void add_animal(Animal a) {
		_list_animals.add(a);
	}

	final void remove_animal(Animal a) {
		_list_animals.remove(a);
	}

	final List<Animal> getAnimals() {
		return this._list_animals;
	}

	public JSONObject as_JSON() {
		JSONObject animal_list_JSON = new JSONObject();

		JSONArray animal_list_array = new JSONArray();

		for (int i = 0; i < this._list_animals.size(); i++) {
			animal_list_array.put(_list_animals.get(i).as_JSON());
		}

		animal_list_JSON.put("animals", animal_list_array);

		return animal_list_JSON;
	}

	protected int get_amount_animals_by_diet(Diet diet) {
		int n = 0;
		for (Animal a : this._list_animals) {
			if (a.get_diet() == diet)
				n++;
		}
		return n;
	}
	
	@Override
	public List<Animalnfo> getAnimalsInfo(){
		return new ArrayList<>(_list_animals);
	}
	
	public abstract String toString();

}
