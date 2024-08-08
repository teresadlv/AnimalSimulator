package simulator.model;

import simulator.misc.Vector2D;

public interface Animalnfo extends JSONable{
	public State get_state();
	public Vector2D get_position();
	public String get_genetic_code();
	public Diet get_diet();
	public double get_speed();
	public double get_sight_range();
	public double get_energy();
	public double get_age();
	public Vector2D get_destination();
	public boolean is_pregnent();

}
