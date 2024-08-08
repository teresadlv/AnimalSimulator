package simulator.model;

import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.Predicate;

import org.json.JSONObject;
import org.json.JSONArray;

import java.util.LinkedHashMap;

public class RegionManager implements AnimalMapView {

	public int _height;
	public int _width;
	public int _columns;
	public int _rows;
	public int _region_height;
	public int _region_width;

	protected Region[][] _regions;
	protected Map<Animal, Region> _animal_region;

	public RegionManager(int cols, int rows, int width, int height) {
		//MANEJO DE EXCEPCIONES
		if(cols<= 0 | rows <= 0 | width <= 0 | height <= 0) {
			throw new IllegalArgumentException("These parameters must be positive integers!");
		}
		_height = height;
		_width = width;
		_columns = cols;
		_rows = rows;
		_region_height = _height / _rows;
		_region_width = _width / _columns;
		_animal_region = new LinkedHashMap<Animal, Region>();
		init_regions();

	}

	private void init_regions() {
		_regions = new Region[this._rows][this._columns];
		for (int i = 0; i < this._rows; i++) {
			for (int j = 0; j < this._columns; j++) {
				this._regions[i][j] = new DefaultRegion();
			}
		}
	}

	@Override
	public int get_cols() {
		return this._columns;
	}

	@Override
	public int get_rows() {
		return this._rows;
	}

	@Override
	public int get_width() {
		return this._width;
	}

	@Override
	public int get_height() {
		return this._height;
	}

	@Override
	public int get_region_width() {
		return this._region_width;
	}

	@Override
	public int get_region_height() {
		return this._region_height;
	}

	public void set_region(int row, int col, Region r) {
		// GUARDAMOS EL VALOR EN UN AUXILIAR
		Region aux = this._regions[row][col];

		// METEMOS LA NUEVA REGION EN _REGION
		this._regions[row][col] = r;

		// METEMOS LOS ANIMALES Y ACTUALIZAMOS _ANIMAL_REGION
		for (int i = 0; i < aux._list_animals.size(); i++) {
			this._regions[row][col].add_animal(aux._list_animals.get(i));
			this._animal_region.put(aux._list_animals.get(i), r);
		}
	}

	private Region find_animal_region(Animal a) {
		int region_row = (int) a.get_position().getY() / this._region_height;
		int region_col = (int) a.get_position().getX() / this._region_width;

		return this._regions[region_row][region_col];
	}

	public void register_animal(Animal a) {

		// INICIALIZA LA REGION, LA POSICION Y EL DESTINO DEL ANIMAL
		a.init(this);

		// ENCUENTRA LA REGION A LA QUE DEBE PERTENECER EN BASE A SU POSICION
		Region expected_region = this.find_animal_region(a);
		expected_region.add_animal(a);

		// AÃ‘ADE AL ANIMAL JUNTO CON LA REGION EN LA QUE ESTA AL MAPA
		this._animal_region.put(a, expected_region);

	}

	public void unregister_animal(Animal a) { // COMPROBAR
		Region current_region = find_animal_region(a);
		current_region.remove_animal(a);
		this._animal_region.remove(current_region, a);
	}

	public void update_animal_region(Animal a) {
		Region expected_region = find_animal_region(a);
		Region r_aux = this._animal_region.get(a);
		if (expected_region != r_aux) {
			expected_region.add_animal(a);
			this._animal_region.get(a).remove_animal(a);
			this._animal_region.put(a, expected_region);
		}
		
	}

	@Override
	public double get_food(Animal a, double dt) {
		Region region = find_animal_region(a);

		return region.get_food(a, dt);
	}

	public void update_all_regions(double dt) {
		for (int i = 0; i < this._rows; i++) {
			for (int j = 0; j < this._columns; j++) {
				this._regions[i][j].update(dt);
			}
		}
	}

	@Override
	public List<Animal> get_animals_in_range(Animal a, Predicate<Animal> filter) {
		List<Animal> animals_in_range_list = new LinkedList<>();
		List<Animal> aux_list = new LinkedList<>();
		double distance = 0.0;

		for (int i = 0; i < this._rows; i++) {
			for (int j = 0; j < this._columns; j++) {
				aux_list = _regions[i][j]._list_animals;
				for(Animal animal_aux: aux_list) {
					distance = a.get_position().distanceTo(animal_aux.get_position());
					if (distance <= a.get_sight_range() && filter.test(animal_aux)) {
						animals_in_range_list.add(animal_aux);
					}

				}
			}
		}

		return animals_in_range_list;
	}

	public JSONObject asJSON() {
		JSONObject resul = new JSONObject();

		JSONArray regiones = new JSONArray();

		for (int i = 0; i < _rows; i++) {
			for (int j = 0; j < _columns; j++) {
				JSONObject o = new JSONObject();
				o.put("row", i);
				o.put("col", j);
				o.put("data", _regions[i][j].as_JSON());
				regiones.put(o);
			}
		}

		resul.put("regiones", regiones);

		return resul;
	}

}
