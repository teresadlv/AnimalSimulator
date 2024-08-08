package simulator.view;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.*;

import simulator.control.Controller;

@SuppressWarnings("serial")
public class MainWindow extends JFrame {

	private Controller _ctrl;

	SpeciesTableModel _species_model;
	RegionsTableModel _regions_model;

	private InfoTable _species_table;
	private InfoTable _regions_table;

	public MainWindow(Controller ctrl) {
		super("[ECOSYSTEM SIMULATOR]");
		this._ctrl = ctrl;
		initGUI();
	}

	private void initGUI() {
		// PANEL PRINCIPAL
		JPanel mainPanel = new JPanel(new BorderLayout());
		setContentPane(mainPanel);

		// CONTROLPANEL
		ControlPanel control_panel = new ControlPanel(this._ctrl);
		mainPanel.add(control_panel, BorderLayout.PAGE_START);

		// STATUSBAR
		StatusBar status_bar = new StatusBar(this._ctrl);
		mainPanel.add(status_bar, BorderLayout.PAGE_END);

		// CONTENTPANEL
		JPanel content_panel = new JPanel();
		content_panel.setLayout(new BoxLayout(content_panel, BoxLayout.Y_AXIS));
		mainPanel.add(content_panel, BorderLayout.CENTER);

		// TABLA DE ESPECIES
		// MODEL
		_species_model = new SpeciesTableModel(_ctrl);
		// TABLE
		_species_table = new InfoTable("Species", _species_model);
		content_panel.add(new JScrollPane(_species_table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
		// AJUSTAMOS SU TAMAÑO
		_species_table.setPreferredSize(new Dimension(500, 250));

		// TABLA DE REGIONES
		// MODEL
		_regions_model = new RegionsTableModel(_ctrl);
		// TABLE
		_regions_table = new InfoTable("Regions", _regions_model);
		content_panel.add(new JScrollPane(_regions_table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
		// AJUSTAMOS SU TAMAÑO
		_regions_table.setPreferredSize(new Dimension(500, 250));

		// LLAMAMOS A ViewUtils.quit(MainWindow.this)EN WINDOW CLOSING
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				ViewUtils.quit(MainWindow.this);
			}
		});

		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		pack();
		setLocationRelativeTo(null);//para que aparezca en el centro
		setVisible(true);

	}

}
