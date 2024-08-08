package simulator.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.json.JSONObject;
import org.json.JSONTokener;

import simulator.control.Controller;
import simulator.launcher.Main;

@SuppressWarnings("serial")
public class ControlPanel extends JPanel {

	private static final String STEPS_LABEL_TXT = "Steps:";
	private static final String DELTA_TIME_LABEL_TXT = "Delta-Time";
	private static final String FILE_BOTTON_TEXT = "Load an input file into the simulator";
	private static final String MAP_BOTTON_TEXT = "Map Viewer";
	private static final String DIALOG_BUTTON_TEXT = "Change Regions";
	private static final String RUN_BUTTON_TEXT = "Run the simulator";
	private static final String STOP_BUTTON_TEXT = "Stop the simulator";
	private static final String STEPS_SPINNER_TEXT = "Simulator steps to run: ";
	private static final String DELTA_TIME_TEXT = "Real time (seconds) corresponding to a step";
	private static final String QUIT_BUTTON_TEXT = "Exit";

	private static final int MAX_STEPS = 10000;
	private static final int MIN_STEPS = 1;

	private Controller _ctrl;
	private ChangeRegionsDialog _changeRegionsDialog;

	private JToolBar _toolaBar;
	private JFileChooser _fc;

	private double _delta_time;
	private int _steps = MAX_STEPS;
	private JSpinner _steps_spinner;
	private JLabel _steps_label;
	private JTextField _delta_time_txt;
	private JLabel _dt_label;

	private boolean _stopped = true;
	private JButton _loadButton;
	private JButton _viewerButton;
	private JButton _quitButton;
	private JButton _changeDialogButton;
	private JButton _stopButton;
	private JButton _runButton;

	public ControlPanel(Controller ctrl) {
		this._ctrl = ctrl;
		this._delta_time = Main._delta_time;
		initGUI();

	}

	private void initGUI() {
		setLayout(new BorderLayout());
		this._toolaBar = new JToolBar();
		add(this._toolaBar, BorderLayout.PAGE_START);

		// LOAD BUTTON
		createLoadButton();
		this._toolaBar.addSeparator();

		// VIEWER BUTTON
		createMapWindowButton();

		// REGIONS BUTTON
		createRegionsButton();
		this._toolaBar.addSeparator();

		// RUN BUTTON
		createRunButton();

		// STOP BUTTON
		createStopButton();

		// STEPS_SPINNER
		this._steps_label = new JLabel();
		this._steps_label.setText(STEPS_LABEL_TXT);
		this._toolaBar.add(_steps_label);
		createStepsSpinner();

		// DELTA TIME JTEXTFIELD
		this._dt_label = new JLabel();
		this._dt_label.setText(DELTA_TIME_LABEL_TXT);
		this._toolaBar.add(_dt_label);
		createDtTextField();

		// QUIT BUTTON
		createExitButton();

		// INICIALIZAMOS _FC
		_fc = new JFileChooser();
		_fc.setCurrentDirectory(new File(System.getProperty("user.dir") + "/resources/examples"));

		// INICIALIZAMOS _CHANGEREGIONSDIALOG
		_changeRegionsDialog = new ChangeRegionsDialog(this._ctrl);
	}

	// RUN SIMULATION

	private void run_sim(int n, double dt) {
		if (n > 0 && !_stopped) {
			try {
				_ctrl.advance(dt);
				SwingUtilities.invokeLater(() -> run_sim(n - 1, dt));
			} catch (Exception e) {
				ViewUtils.showErrorMsg("Couldnt run the simulation");
				enableToolBar(true);
				_stopped = true;
			}
		} else {
			enableToolBar(true);
			_stopped = true;
		}
	}

	// METODOS AUXILIARES

	private void enableToolBar(Boolean b) {
		this._loadButton.setEnabled(b);
		_viewerButton.setEnabled(b);
		_changeDialogButton.setEnabled(b);
		_steps_spinner.setEnabled(b);
		_delta_time_txt.setEnabled(b);
	}

	// METODOS AUXILIARES PARA CREAR BOTONES

	private void createExitButton() {
		this._toolaBar.add(Box.createGlue()); // THIS ALIGNS THE BUTTON TO THE RIGHT
		this._toolaBar.addSeparator();
		this._quitButton = new JButton();
		this._quitButton.setToolTipText(QUIT_BUTTON_TEXT);
		this._quitButton.setIcon(new ImageIcon("resources/icons/exit.png"));
		this._quitButton.addActionListener((e) -> ViewUtils.quit(this));
		this._toolaBar.add(this._quitButton);
	}

	private void createLoadButton() {
		this._fc = new JFileChooser("resources/examples");

		this._loadButton = new JButton();
		this._loadButton.setToolTipText(FILE_BOTTON_TEXT);
		this._loadButton.setIcon(new ImageIcon("resources/icons/open.png"));
		this._loadButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (_fc.showOpenDialog(ViewUtils.getWindow(_fc)) == JFileChooser.APPROVE_OPTION) {
					File file = _fc.getSelectedFile();
					InputStream input;
					try {
						input = new FileInputStream(file);
						JSONObject obj = new JSONObject(new JSONTokener(input));
						int width = obj.getInt("width");
						int height = obj.getInt("height");
						int rows = obj.getInt("rows");
						int cols = obj.getInt("cols");

						_ctrl.reset(cols, rows, width, height);
						_ctrl.load_data(obj);

					} catch (FileNotFoundException e1) {
						JOptionPane.showMessageDialog(null, "an error has happened", "ERROR",
								JOptionPane.ERROR_MESSAGE);
					}
				}

			}

		});
		this._toolaBar.add(this._loadButton);
	}

	private void createMapWindowButton() {
		this._viewerButton = new JButton();
		this._viewerButton.setToolTipText(MAP_BOTTON_TEXT);
		this._viewerButton.setIcon(new ImageIcon("resources/icons/viewer.png"));
		this._viewerButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				MapWindow map = new MapWindow(ViewUtils.getWindow(ControlPanel.this), _ctrl);
				map.setPreferredSize(new Dimension(700, 550));
			}

		});
		this._toolaBar.add(this._viewerButton);
	}

	private void createStopButton() {
		this._stopButton = new JButton();
		this._stopButton.setToolTipText(STOP_BUTTON_TEXT);
		this._stopButton.setIcon(new ImageIcon("resources/icons/stop.png"));
		this._stopButton.addActionListener((e) -> _stopped = true);
		this._toolaBar.add(this._stopButton);
	}

	private void createRegionsButton() {
		this._changeDialogButton = new JButton();
		this._changeDialogButton.setToolTipText(DIALOG_BUTTON_TEXT);
		this._changeDialogButton.setIcon(new ImageIcon("resources/icons/regions.png"));
		this._changeDialogButton
				.addActionListener((e) -> _changeRegionsDialog.open(ViewUtils.getWindow(ControlPanel.this)));
		this._toolaBar.add(this._changeDialogButton);
	}

	private void createRunButton() {
		this._runButton = new JButton();
		this._runButton.setToolTipText(RUN_BUTTON_TEXT);
		this._runButton.setIcon(new ImageIcon("resources/icons/run.png"));
		this._runButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// DESHABILITAMOS TODOS LOS BOTONES Y PONEMOS _STOPPED A FALSE
				enableToolBar(false);
				_stopped = false;

				// LLAMA A RUN_SIM CON LOS VALORES CORRESPONDIENTES
				ControlPanel.this.run_sim(ControlPanel.this._steps, ControlPanel.this._delta_time);

			}

		});
		this._toolaBar.add(this._runButton);
	}

	private void createStepsSpinner() {
		_steps_spinner = new JSpinner(new SpinnerNumberModel(_steps, 1, _steps, 100));
		_steps_spinner.setMaximumSize(new Dimension(200, 30));
		_steps_spinner.setMinimumSize(new Dimension(80, 30));
		_steps_spinner.setPreferredSize(new Dimension(80, 30));
		_steps_spinner.setToolTipText(STEPS_SPINNER_TEXT + MIN_STEPS + "-" + MAX_STEPS);

		_steps_spinner.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {

				ControlPanel.this._steps = Integer.valueOf(_steps_spinner.getValue().toString());

			}

		});
		this._toolaBar.add(this._steps_spinner);
	}

	private void createDtTextField() {
		this._delta_time_txt = new JTextField(String.valueOf(Main._delta_time));
		_delta_time_txt.setMaximumSize(new Dimension(200, 30));
		_delta_time_txt.setMinimumSize(new Dimension(80, 30));
		_delta_time_txt.setPreferredSize(new Dimension(80, 30));
		_delta_time_txt.setToolTipText(DELTA_TIME_TEXT);

		_delta_time_txt.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				ControlPanel.this._delta_time = Main._delta_time;

			}
		});

		this._toolaBar.add(_delta_time_txt);
	}

}
