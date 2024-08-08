package simulator.model;

import java.util.List;

public interface EcoSysObserver {
	void onRegister(double time, MapInfo map, List<Animalnfo> animals);
	void onReset(double time, MapInfo map, List<Animalnfo> animals);
	void onAnimalAdded(double time, MapInfo map, List<Animalnfo> animals, Animalnfo a);
	void onRegionSet(int row, int col, MapInfo map, RegionInfo r);
	void onAdvance (double time, MapInfo map, List<Animalnfo> animals, double dt);

}
