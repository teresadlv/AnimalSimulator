package simulator.view;

import java.util.List;

import javax.swing.JComponent;

import simulator.model.Animalnfo;
import simulator.model.MapInfo;

@SuppressWarnings("serial")
public abstract class AbstractMapViewer extends JComponent {

	public abstract void update(List<Animalnfo> objs, Double time);

	public abstract void reset(double time, MapInfo map, List<Animalnfo> animals);
}
