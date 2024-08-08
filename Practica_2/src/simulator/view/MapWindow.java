package simulator.view;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.List;

import javax.swing.*;

import simulator.control.Controller;
import simulator.model.Animalnfo;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.RegionInfo;

@SuppressWarnings("serial")
public class MapWindow extends JFrame implements EcoSysObserver {

	private Controller _ctrl;
	private AbstractMapViewer _viewer;
	private Frame _parent;

	public MapWindow(Frame parent, Controller ctrl) {
		super("[MAP VIEWER]");
		this._ctrl = ctrl;
		this._parent = parent;
		initGUI();
		// REGISTRAMOS COMO OBSERVADOR
		this._ctrl.addObserver(this);

	}

	private void initGUI() {
		// CREAMOS MAINPANEL
		JPanel mainPanel = new JPanel(new BorderLayout());
		// PONEMOS MAINPANEL COMO CONTENT PANE
		setContentPane(mainPanel);

		// INICIALIZAMOS VIEWER Y LO AÑADIMOS AL MAINPANEL
		this._viewer = new MapViewer();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(new JScrollPane(this._viewer, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);

		// EN WINDOWCLOSING, ELIMINAMOS MAPWINDOW.THIS DE LOS OBSERVERS
		addWindowListener(new WindowListener() {

			@Override
			public void windowOpened(WindowEvent e) {
				// VACIO

			}

			@Override
			public void windowClosing(WindowEvent e) {
				MapWindow.this._ctrl.removeObserver(MapWindow.this);
			}

			@Override
			public void windowClosed(WindowEvent e) {
				// VACIO

			}

			@Override
			public void windowIconified(WindowEvent e) {
				// VACIO

			}

			@Override
			public void windowDeiconified(WindowEvent e) {
				// VACIO

			}

			@Override
			public void windowActivated(WindowEvent e) {
				// VACIO

			}

			@Override
			public void windowDeactivated(WindowEvent e) {
				// VACIO

			}
		});

		pack();
		if (this._parent != null) {
			setLocation(_parent.getLocation().x + _parent.getWidth() / 2 - getWidth() / 2,
					_parent.getLocation().y + _parent.getHeight() / 2 - getHeight() / 2);
			setResizable(false);
			setVisible(true);
		}
	}

	@Override
	public void onRegister(double time, MapInfo map, List<Animalnfo> animals) {
		SwingUtilities.invokeLater(() -> {
			this._viewer.reset(time, map, animals);
			pack();
		});
	}

	@Override
	public void onAdvance(double time, MapInfo map, List<Animalnfo> animals, double dt) {
		SwingUtilities.invokeLater(() -> {
			this._viewer.update(animals, time);
			pack();
		});

	}

	@Override
	public void onReset(double time, MapInfo map, List<Animalnfo> animals) {
		SwingUtilities.invokeLater(() -> {
			this._viewer.reset(time, map, animals);
			pack();
		});

	}

	@Override
	public void onAnimalAdded(double time, MapInfo map, List<Animalnfo> animals, Animalnfo a) {

	}

	@Override
	public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {

	}
}
