package simulator.model;

import java.util.List;

public class SelectClosest implements SelectionStrategy {

	@Override
	public Animal select(Animal a, List<Animal> as) {
		if (as.isEmpty())
			return null;
		else {
			Animal closest_animal = as.get(0);
			for (Animal animal_aux : as) {
				if (a.get_position().distanceTo(animal_aux.get_position()) < a.get_position()
						.distanceTo(closest_animal.get_position()) && !a.equals(animal_aux))
					closest_animal = animal_aux;
			}
			return closest_animal;
		}
	}

}
