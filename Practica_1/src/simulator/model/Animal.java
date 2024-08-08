package simulator.model;

import org.json.JSONObject;
import simulator.misc.Utils;
import simulator.misc.Vector2D;

public abstract class Animal implements Animalnfo, Entity {

	protected static final double INITIAL_SPEED_PARAMETER = 0.1;
	protected static final double INITIAL_ENERGY = 100.0;
	protected static final double INITIAL_BABY_POS_PARAMETER = 60.0;
	protected static final double INITIAL_BABY_SITE_PARAMETER = 0.2;
	protected static final double INITIAL_BABY_SPEED_PARAMETER = 0.2;
	protected static final double FIRST_SPEED_PARAMETER = 2.0;
	protected static final double SECOND_SPEED_PARAMETER = 100.0;
	protected static final double THIRD_SPEED_PARAMETER = 0.007;
	protected static final double MAX_DISTANCE_TO_DEST = 8.0;
	protected static final double SPEED_PARAM_WHEN_EXCITED = 3.0;

	protected final static double MIN_ENERGY = 0.0;
	protected final static double MIN_DESIRE = 0.0;
	protected final static double MAX_ENERGY = 100.0;
	protected final static double MAX_DESIRE = 100.0;

	protected String _genetic_code;
	protected Diet _diet;
	protected State _state;
	protected Vector2D _pos;
	protected Vector2D _dest;
	protected double _energy;
	protected double _speed;
	protected double _age;
	protected double _desire;
	protected double _sight_range;
	protected Animal _mate_target;
	protected Animal _baby;
	protected AnimalMapView _region_mngr;
	protected SelectionStrategy _mate_strategy;

	// OBJECTOS INICIALES
	protected Animal(String genetic_code, Diet diet, double sight_range, double init_speed,
			SelectionStrategy mate_strategy, Vector2D pos) {

		// MANEJO DE EXCEPCIONES
		if (genetic_code.isEmpty())
			throw new IllegalArgumentException("The animal's genetic code can't be an empty string");
		else if (sight_range <= 0)
			throw new IllegalArgumentException("The animal's sight range must be a positive number");
		else if (init_speed <= 0)
			throw new IllegalArgumentException("The animal's initial speed must be a positive number");
		else if (mate_strategy == null)
			throw new IllegalArgumentException("The animal's mate strategy can't be null");

		this._genetic_code = genetic_code;
		this._diet = diet;
		this._sight_range = sight_range;
		this._pos = pos;
		this._mate_strategy = mate_strategy;
		this._speed = Utils.get_randomized_parameter(init_speed, INITIAL_SPEED_PARAMETER);

		this._state = State.NORMAL;
		this._energy = INITIAL_ENERGY;
		this._desire = 0.0;

		this._dest = null;
		this._mate_target = null;
		this._baby = null;
		this._region_mngr = null;
	}

	// CUANDO NAZCA UN ANIMAL A PARTIR DE OTROS
	protected Animal(Animal p1, Animal p2) {
		this._state = State.NORMAL;
		this._desire = 0.0;

		this._genetic_code = p1._genetic_code;
		this._diet = p1._diet;
		this._energy = (p1._energy + p2._energy) / 2;
		this._pos = p1.get_position().plus(
				Vector2D.get_random_vector(-1, 1).scale(INITIAL_BABY_POS_PARAMETER * (Utils._rand.nextGaussian() + 1)));
		this._sight_range = Utils.get_randomized_parameter((p1.get_sight_range() + p2.get_sight_range()) / 2,
				INITIAL_BABY_SITE_PARAMETER);
		this._speed = Utils.get_randomized_parameter((p1.get_speed() + p2.get_speed()) / 2,
				INITIAL_BABY_SPEED_PARAMETER);

		this._dest = null;
		this._mate_target = null;
		this._baby = null;
		this._region_mngr = null;
	}

	@Override
	public State get_state() {
		return this._state;
	}

	public void set_state(State nextState) {
		this._state = nextState;
	}

	@Override
	public double get_speed() {
		return this._speed;
	}

	@Override
	public double get_sight_range() {
		return this._sight_range;
	}

	@Override
	public Vector2D get_position() {
		return this._pos;
	}

	@Override
	public String get_genetic_code() {
		return this._genetic_code;
	}

	@Override
	public Diet get_diet() {
		return this._diet;
	}

	@Override
	public double get_energy() {
		return this._energy;
	}

	@Override
	public double get_age() {
		return this._age;
	}

	public void set_age(double dt) {
		this._age += dt;
	}

	public Double get_desire() {
		return this._desire;
	}

	@Override
	public Vector2D get_destination() {
		return this._dest;
	}

	@Override
	public boolean is_pregnent() {
		return (this._baby != null);
	}

	protected void init(AnimalMapView reg_mngr) {

		this._region_mngr = reg_mngr;

		// OBTENER DIMENSIONES DE LA REGION
		int map_width = this._region_mngr.get_width();
		int map_height = this._region_mngr.get_height();

		if (this._pos == null) {
			// ELEGIR POSICION ALEATORIA
			double pos_x = Utils._rand.nextDouble(map_width - 1);
			double pos_y = Utils._rand.nextDouble(map_height - 1);
			this._pos = new Vector2D(pos_x, pos_y);
		} else {
			adjust_position();
		}

		// ELEGIR DESTINO ALEATORIO
		double dest_x = Utils._rand.nextDouble(map_width - 1);
		double dest_y = Utils._rand.nextDouble(map_height - 1);
		this._dest = new Vector2D(dest_x, dest_y);

	}

	public Animal deliver_baby() {
		Animal _aux = _baby;
		this._baby = null;
		return _aux;

	}

	void adjust_position() {
		// OBTENER LAS COORDENADAS DE LA POS ACTUAL
		double x_coordinate = this._pos.getX();
		double y_coordinate = this._pos.getY();

		// OBTENER DIMENSIONES DE LA REGION
		int region_width = this._region_mngr.get_width();
		int region_height = this._region_mngr.get_height();

		// AJUSTAR LA POSICION ACTUAL
		while (x_coordinate >= region_width)
			x_coordinate = (x_coordinate - region_width);
		while (x_coordinate < 0)
			x_coordinate = (x_coordinate + region_width);
		while (y_coordinate >= region_height)
			y_coordinate = (y_coordinate - region_height);
		while (y_coordinate < 0)
			y_coordinate = (y_coordinate + region_height);

		this._pos = new Vector2D(x_coordinate, y_coordinate);

	}

	protected void pick_new_dest() {
		if (get_position().distanceTo(get_destination()) < MAX_DISTANCE_TO_DEST) {
			double x = Utils._rand.nextDouble(this._region_mngr.get_width());
			double y = Utils._rand.nextDouble(this._region_mngr.get_height());
			this._dest = new Vector2D(x, y);
		}
	}

	protected double speed_to_move_with(double dt) {
		return FIRST_SPEED_PARAMETER * _speed * dt
				* Math.exp((_energy - SECOND_SPEED_PARAMETER) * THIRD_SPEED_PARAMETER);
	}

	protected double speed_when_excited(double dt) {
		return SPEED_PARAM_WHEN_EXCITED * _speed * dt
				* Math.exp((_energy - SECOND_SPEED_PARAMETER) * THIRD_SPEED_PARAMETER);
	}

	protected void move(double speed) {
		_pos = _pos.plus(_dest.minus(_pos).direction().scale(speed));
	}

	public JSONObject as_JSON() {
		JSONObject animal_JSON = new JSONObject();
		double[] coordinates = { this._pos.getX(), this._pos.getY() };

		animal_JSON.put("pos", coordinates);
		animal_JSON.put("gcode", this._genetic_code);
		animal_JSON.put("diet", this._diet.toString());
		animal_JSON.put("state", this._state.toString());

		return animal_JSON;
	}

	@Override
	public void update(double dt) {
		// IMPLEMENTACION VACIA

	}

	//METODOS ABSTRACTOS:
	
	protected abstract void mandatory_update(double dt);

	protected abstract void normal_partial_update(double dt);

	protected abstract void advance_when_excited(double dt);

	protected abstract void normal_state_change();

	protected abstract void condtions_mate_state(double dt);

	protected abstract void mate_state_change();

	protected abstract void conditions_danger_state(double dt);

	protected abstract void danger_state_change();

	protected abstract void conditions_hunger_state(double dt);

	protected abstract void hunger_state_change();

	protected abstract void change_to_normal_state();

	protected abstract void change_to_mate_state();

	protected abstract void change_to_danger_state();

	protected abstract void change_to_hunger_state();

}
