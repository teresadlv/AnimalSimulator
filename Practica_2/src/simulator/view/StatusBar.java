package simulator.view;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import simulator.control.Controller;
import simulator.model.Animalnfo;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.RegionInfo;

@SuppressWarnings("serial")
public class StatusBar extends JPanel implements EcoSysObserver {

	private JLabel _time;
	private JLabel _time_value;
	private JLabel _n_animals;
	private JLabel _animals_n_value;
	private JLabel _dimension;
	private JLabel _dimension_value_cols;
	private JLabel _dimension_value_rows;
	private JLabel _dimension_value_width;
	private JLabel _dimension_value_height;

	public StatusBar(Controller ctrl) {
		// INITGUI
		initGUI();
		// REGISTRAR THIS COMO OBSERVADOR
		ctrl.addObserver(this);
	}

	private void initGUI() {
		this.setLayout(new FlowLayout(FlowLayout.LEFT));
		this.setBorder(BorderFactory.createBevelBorder(1));

		// SEPARADOR. LO INICIALIZAMOS ARRIBA PARA IR AÑADIENDOLO
		JSeparator s = new JSeparator(JSeparator.VERTICAL);
		s.setPreferredSize(new Dimension(10, 20));

		// JLABEL TIME
		this._time = new JLabel("Time: ");
		this._time_value = new JLabel();
		this.add(_time);
		this.add(_time_value);

		// AÑADIMOS SEPARADOR
		this.add(s);

		// JLABEL NUMERO DE ANIMALES
		this._n_animals = new JLabel("Total Animals: ");
		this._animals_n_value = new JLabel();
		this.add(this._n_animals);
		this.add(_animals_n_value);

		// AÑADIMOS SEPARADOR
		this.add(s);

		// JLABEL DIMENSIONES
		this._dimension = new JLabel("Dimension: ");
		this._dimension_value_cols = new JLabel();
		this._dimension_value_rows = new JLabel();
		this._dimension_value_width = new JLabel();
		this._dimension_value_height = new JLabel();
		this.add(this._dimension);
		this.add(_dimension_value_width);
		this.add(new JLabel("x"));// AÑADIMOS UN SEPARADOR "x"
		this.add(_dimension_value_height);

		// AÑADIMOS SEPARADOR
		this.add(s);

		// ROWS Y COLS
		this.add(_dimension_value_cols);
		this.add(new JLabel("x"));// AÑADIMOS UN SEARADOR "x"
		this.add(_dimension_value_rows);
	}

	// METODOS DEL OBSERVER
	@Override
	public void onReset(double time, MapInfo map, List<Animalnfo> animals) {
		this._time_value.setText("0.000");
		this._dimension_value_cols.setText(String.valueOf(map.get_cols()));
		this._dimension_value_rows.setText(String.valueOf(map.get_rows()));
		this._dimension_value_width.setText(String.valueOf(map.get_width()));
		this._dimension_value_height.setText(String.valueOf(map.get_height()));

	}

	@Override
	public void onAnimalAdded(double time, MapInfo map, List<Animalnfo> animals, Animalnfo a) {
		this._animals_n_value.setText(String.valueOf(animals.size()));

	}

	@Override
	public void onAdvance(double time, MapInfo map, List<Animalnfo> animals, double dt) {
		this._time_value.setText(String.format("%.3f", time));
		this._animals_n_value.setText(String.valueOf(animals.size()));

	}

	// IMPLEMENTACIONES VACIAS
	@Override
	public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {
	}

	@Override
	public void onRegister(double time, MapInfo map, List<Animalnfo> animals) {
	}
}
