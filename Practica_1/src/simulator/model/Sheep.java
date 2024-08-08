package simulator.model;

import java.util.List;

import simulator.misc.Utils;
import simulator.misc.Vector2D;

public class Sheep extends Animal implements Entity {

	private static final String GENETIC_CODE = "Sheep";
	private static final double INITIAL_SIGHT_RANGE = 40.0;
	private static final double INITIAL_SPEED = 35.0;
	private static final double LIFESPAN = 8.0;
	private static final double DECREASE_ENERGY_BY = 20.0;
	private static final double DECREASE_ENERGY_WHEN_EXCITED_BY = 1.2;
	private static final double INCREASE_DESIRE_BY = 40.0;
	private static final double STATE_CHANGER_DESIRE = 65.0;
	private static final double PROBABILITY_OF_PREGNANCY = 0.9;

	protected Animal _danger_source;
	protected SelectionStrategy _danger_strategy;

	// VARIABLES PARA EL UPDATE
	List<Animal> animals_to_mate;
	List<Animal> dangerous_animals;

	public Sheep(SelectionStrategy mate_strategy, SelectionStrategy danger_strategy, Vector2D pos) {
		super(GENETIC_CODE, Diet.HERBIVORE, INITIAL_SIGHT_RANGE, INITIAL_SPEED, mate_strategy, pos);
		_danger_source = null;
		_danger_strategy = danger_strategy;
	}

	protected Sheep(Sheep p1, Animal p2) {
		super(p1, p2);
		_danger_strategy = p1._danger_strategy;
		_danger_source = null;
	}

	public Animal get_danger_source() {
		return this._danger_source;
	}

	@Override
	public void update(double dt) {
		// Lista de animales para emparejarse
		animals_to_mate = this._region_mngr.get_animals_in_range(this, (a) -> a.get_genetic_code() == GENETIC_CODE);
		// Lista de depredadores
		dangerous_animals = this._region_mngr.get_animals_in_range(this, (a) -> a.get_diet() == Diet.CARNIVORE);

		switch (this._state) {
		case NORMAL:
			normal_partial_update(dt);
			normal_state_change();

			break;
		case MATE:
			condtions_mate_state(dt);
			mate_state_change();

			break;
		case DANGER:
			conditions_danger_state(dt);
			danger_state_change();

			break;
		case HUNGER:
			// NO TIENE ESTE ESTADO
			break;
		default:
			return;

		}
		mandatory_update(dt);
	}

	// METODOS AUXILIARES PARA EL UDPATE:
	@Override
	protected void mandatory_update(double dt) {
		// PARTE OBLIGATORIA DEL UPDATE PARA TODOS LOS ESTADOS
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
		if (this._desire > MAX_ENERGY)
			this._desire = MAX_ENERGY;
	}

	@Override
	protected void normal_state_change() {
		// CAMBIO DE ESTADO

		// BUSCA UN NUEVO ANIMAL PELIGROSO
		if (this._danger_source == null)
			this._danger_source = this._danger_strategy.select(this, dangerous_animals);

		// ENCUENTRA UN NUEVO DEPREDADOR
		if (this._danger_source != null)
			this.change_to_danger_state();

		// NO ENCUENTRA UN ANIMAL PELIGROSO CERCA
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

			// BUSCA PAREJA
			this._mate_target = this._mate_strategy.select(this, animals_to_mate);

			// SI NO ENCUENTRA ANIMAL PARA EMPAREJARSE
			if (this._mate_target == null) {
				this.normal_partial_update(dt);
			}
		}

		// SI TIENE ANIMAL PARA EMPAREJARSE
		if (this._mate_target != null) {
			this._dest = this._mate_target.get_position();
			this.advance_when_excited(dt);

			// SI CONSIGUE ACERCARSE AL _mate_target, SE EMPAREJA CON EL
			if (this._pos.distanceTo(this._mate_target.get_position()) < MAX_DISTANCE_TO_DEST) {
				this._desire = MIN_DESIRE;
				this._mate_target._desire = MIN_DESIRE;
				double pregnant_posibility = Utils._rand.nextDouble();
				if (pregnant_posibility <= PROBABILITY_OF_PREGNANCY && this._baby == null) {
					this._baby = new Sheep(this, this._mate_target);
					this._baby._mate_strategy = this._mate_strategy;
					this._mate_target = null;
				}
			}
		}
	}

	@Override
	protected void mate_state_change() {
		// CAMBIO DE ESTADO

		// BUSCA ALGUN DEPREDADOR
		if (this._danger_source == null)
			this._danger_source = this._danger_strategy.select(this, dangerous_animals);

		// ENCUENTRA UN NUEVO DEPREDADOR
		if (this._danger_source != null)
			this.change_to_danger_state();

		// NO ENCUENTRA UN ANIMAL PELIGROSO CERCA
		else {
			if (this._desire < STATE_CHANGER_DESIRE)
				this.change_to_normal_state();
		}

	}

	@Override
	protected void conditions_danger_state(double dt) {
		if (this._danger_source != null && this._danger_source.get_state() == State.DEAD)
			this._danger_source = null;

		if (this._danger_source == null)
			this.normal_partial_update(dt);

		else {
			this._dest = this._pos.plus(_pos.minus(_danger_source.get_position()).direction());
			this.advance_when_excited(dt);
		}
	}

	@Override
	protected void danger_state_change() {
		// CAMBIO DE ESTADO

		if (this._danger_source == null || !dangerous_animals.contains(this._danger_source)) {
			this._danger_source = this._danger_strategy.select(this, dangerous_animals);
			if (this._danger_source != null) {
				if (this._desire < STATE_CHANGER_DESIRE)
					this.change_to_normal_state();
				else
					this.change_to_mate_state();
			}
		}
	}

	@Override
	protected void conditions_hunger_state(double dt) {
		// IMPLEMENTACION VACIA
	}

	protected void hunger_state_change() {
		// IMPLEMENTACION VACIA
	}

	// METODOS AUXILIARES PARA EL CAMBIO DE ESTADO:
	@Override
	protected void change_to_normal_state() {
		this._state = State.NORMAL;
		this._danger_source = null;
		this._mate_target = null;
	}

	@Override
	protected void change_to_mate_state() {
		this._state = State.MATE;
		this._danger_source = null;
	}

	@Override
	protected void change_to_danger_state() {
		this._state = State.DANGER;
		this._mate_target = null;
	}

	@Override
	protected void change_to_hunger_state() {
		// IMPLEMENTACION VACIA

	}

}
