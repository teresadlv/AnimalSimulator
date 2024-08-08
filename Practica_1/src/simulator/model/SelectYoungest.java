package simulator.model;

import java.util.List;

public class SelectYoungest implements SelectionStrategy {

	@Override
	public Animal select(Animal a, List<Animal> as) {
		if (as.isEmpty())
			return null;
		else {
			Animal youngest_animal = as.get(0);
			for (Animal animal_aux : as) {
				if (animal_aux.get_age() < youngest_animal.get_age())
					youngest_animal = animal_aux;
			}
			return youngest_animal;
		}
	}

}