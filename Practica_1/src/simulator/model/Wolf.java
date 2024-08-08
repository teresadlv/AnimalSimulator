package simulator.model;

import java.util.List;

import simulator.misc.Utils;
import simulator.misc.Vector2D;

public class Wolf extends Animal {

	private final static String GENETIC_CODE = "Wolf";
	private final static double INITIAL_SIGHT_RANGE = 50.0;
	private final static double INITIAL_SPEED = 60.0;
	private final static double LIFESPAN = 14.0;
	private final static double DECREASE_ENERGY_BY = 18.0;
	private final static double DECREASE_ENERGY_WHEN_EXCITED_BY = 1.2;
	private final static double DECREASE_ENERGY_AFTER_PREGNANCY_BY = 10.0;
	private final static double ENERGY_TO_GO_HUNGER_STATE = 50.0;
	private final static double INCREASE_DESIRE_BY = 30.0;
	private final static double STATE_CHANGER_DESIRE = 65.0;
	private final static double PROBABILITY_OF_PREGNANCY = 0.9;

	private Animal _hunt_target;
	private SelectionStrategy _hunting_strategy;

	// VARIABLES PARA EL UPDATE
	List<Animal> animals_to_mate;
	List<Animal> hunted_animals;

	public Wolf(SelectionStrategy mate_strategy, SelectionStrategy hunting_strategy, Vector2D pos) {
		super(GENETIC_CODE, Diet.CARNIVORE, INITIAL_SIGHT_RANGE, INITIAL_SPEED, mate_strategy, pos);
		this._hunting_strategy = hunting_strategy;
	}

	protected Wolf(Wolf p1, Animal p2) {
		super(p1, p2);
		this._hunting_strategy = p1._hunting_strategy;
		this._hunt_target = null;
	}

	@Override
	public void update(double dt) {

		// Lista de animales para emparejarse
		animals_to_mate = this._region_mngr.get_animals_in_range(this, (a) -> a.get_genetic_code() == GENETIC_CODE);

		// Lista de depredadores
		hunted_animals = this._region_mngr.get_animals_in_range(this, (a) -> a.get_diet() == Diet.HERBIVORE);

		switch (this._state) {

		case NORMAL:

			normal_partial_update(dt);
			normal_state_change();

			break;
		case MATE:

			condtions_mate_state(dt);
			mate_state_change();

			break;
		case HUNGER:

			conditions_hunger_state(dt);
			hunger_state_change();

			break;
		case DANGER:
			// NUNCA ESTA EN PELIGRO
			break;
		default:
			return;

		}

		mandatory_update(dt);

	}

	// METODOS AUXILIARES PARA EL UPDATE:

	@Override
	protected void mandatory_update(double dt) {
		// PARTE OBLIGATORIA DEL UPDATE
		this.adjust_position();
		if (this._energy == MIN_ENERGY || this._age > LIFESPAN)
			this._state = State.DEAD;
		if (this._state != State.DEAD) {
			this._energy += this._region_mngr.get_food(this, dt);
			if (this._energy > MAX_ENERGY)
				this._energy = MAX_ENERGY;
		}
	}

	@Override
	protected void normal_partial_update(double dt) {
		pick_new_dest();
		move(speed_to_move_with(dt));
		set_age(dt);

		this._energy -= DECREASE_ENERGY_BY * dt;
		if (this._energy < MIN_ENERGY)
			this._energy = MIN_ENERGY;

		this._desire += INCREASE_DESIRE_BY * dt;
		if (this._desire > MAX_DESIRE)
			this._desire = MAX_DESIRE;
	}

	@Override
	protected void advance_when_excited(double dt) {
		move(speed_when_excited(dt));
		set_age(dt);

		this._energy -= DECREASE_ENERGY_WHEN_EXCITED_BY * DECREASE_ENERGY_BY * dt;
		if (this._energy < MIN_ENERGY)
			this._energy = MIN_ENERGY;

		this._desire += INCREASE_DESIRE_BY * dt;
		if (this._desire > MAX_DESIRE)
			this._desire = MAX_DESIRE;
	}

	@Override
	protected void normal_state_change() {
		// CAMBIO DE ESTADO

		if (this._energy < ENERGY_TO_GO_HUNGER_STATE)
			this.change_to_hunger_state();
		else {
			if (this._desire > STATE_CHANGER_DESIRE)
				this.change_to_mate_state();
		}
	}

	@Override
	protected void condtions_mate_state(double dt) {
		// SI YA TIENE ANIMAL PARA EMPAREJARSE
		if (this._mate_target != null && this._mate_target.get_state() == State.DEAD
				|| !animals_to_mate.contains(this._mate_target))
			this._mate_target = null;

		// SI NO TIENE ANIMAL PARA EMPAREJARSE
		if (this._mate_target == null) {

			// BUSCAR PAREJA
			this._mate_target = this._mate_strategy.select(this, animals_to_mate);

			// SI NO ENCUENTRA ANIMAL CON QUIEN EMPAREJARSE
			if (this._mate_target == null) {
				this.normal_partial_update(dt);
			}
		}

		// SI TIENE AINMAL PARA EMPAREJARSE
		if (this._mate_target != null) {
			this._dest = this._mate_target.get_position();
			this.advance_when_excited(dt);

			// SI CONSIGUE ACERCARSE AL _mate_target, SE EMPAREJA CON EL
			if (this._pos.distanceTo(this._mate_target.get_position()) < MAX_DISTANCE_TO_DEST) {
				this._desire = MIN_DESIRE;
				this._mate_target._desire = MIN_DESIRE;
				double pregnant_posibility = Utils._rand.nextDouble();
				if (pregnant_posibility <= PROBABILITY_OF_PREGNANCY && this._baby == null) {
					this._baby = new Wolf(this, this._mate_target);
					this._baby._mate_strategy = this._mate_strategy;
					this._energy -= DECREASE_ENERGY_AFTER_PREGNANCY_BY;
					if (this._energy < MIN_ENERGY)
						this._energy = MIN_ENERGY;
					this._mate_target = null;
				}
			}
		}

	}

	@Override
	protected void mate_state_change() {
		// CAMBIO DE ESTADO

		if (this._energy < ENERGY_TO_GO_HUNGER_STATE)
			this.change_to_hunger_state();
		else {
			if (this._desire < STATE_CHANGER_DESIRE)
				this.change_to_normal_state();
		}

	}

	@Override
	protected void conditions_danger_state(double dt) {
		// IMPLEMENTACION VACIA

	}

	@Override
	protected void danger_state_change() {
		// IMPLEMENTACION VACIA

	}

	@Override
	protected void conditions_hunger_state(double dt) {
		// BUSCA ALGUNA PRESA
		if ((this._hunt_target == null) || (this._hunt_target != null && this._hunt_target.get_state() == State.DEAD)
				|| (!hunted_animals.contains(this._hunt_target))) {
			this._hunt_target = this._hunting_strategy.select(this, hunted_animals);
		}

		// NO ENCUENTRA PRESA
		if (this._hunt_target == null)
			this.normal_partial_update(dt);

		// ENCUENTRA PRESA
		else {
			this._dest = this._hunt_target.get_destination();
			this.advance_when_excited(dt);

			// SI CONSIGUE ACERCARSE A LA PRESA, LA CAZA
			if (this._pos.distanceTo(this._hunt_target.get_position()) < MAX_DISTANCE_TO_DEST) {
				this._hunt_target._state = State.DEAD;
				this._hunt_target = null;
				this._energy += 100;
				if (this._energy > MAX_ENERGY)
					this._energy = MAX_ENERGY;
			}
		}

	}

	@Override
	protected void hunger_state_change() {
		// CAMBIO DE ESTADO
		if (this._energy > ENERGY_TO_GO_HUNGER_STATE) {
			if (this._desire < STATE_CHANGER_DESIRE)
				this.change_to_normal_state();
			else
				this.change_to_mate_state();
		}

	}

	// METODOS AUXILIARES PARA EL CAMBIO DE ESTADO:
	@Override
	protected void change_to_normal_state() {
		this._state = State.NORMAL;
		this._hunt_target = null;
		this._mate_target = null;
	}

	@Override
	protected void change_to_mate_state() {
		this._state = State.MATE;
		this._hunt_target = null;
	}

	@Override
	protected void change_to_hunger_state() {
		this._state = State.HUNGER;
		this._hunt_target = null;
	}

	@Override
	protected void change_to_danger_state() {
		// IMPLEMENTACION VACIA

	}

}