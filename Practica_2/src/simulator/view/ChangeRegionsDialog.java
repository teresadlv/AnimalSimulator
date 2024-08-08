package simulator.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import org.json.JSONObject;
import org.json.JSONArray;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.table.DefaultTableModel;

import simulator.control.Controller;
import simulator.launcher.Main;
import simulator.model.Animalnfo;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.RegionInfo;

@SuppressWarnings("serial")
public class ChangeRegionsDialog extends JDialog implements EcoSysObserver {

	// ATTRIBUTES, COMPONENTS AND CONSTANTS

	private DefaultComboBoxModel<String> _regionsModel;
	private DefaultComboBoxModel<String> _fromRowModel;
	private DefaultComboBoxModel<String> _toRowModel;
	private DefaultComboBoxModel<String> _fromColModel;
	private DefaultComboBoxModel<String> _toColModel;

	private DefaultTableModel _dataTableModel;
	private JTable _regionsTable;
	private Controller _ctrl;
	private List<JSONObject> _regionsInfo;

	private String[] _headers = { "Key", "Value", "Description" };
	private final static String HELP_TEXT = "Select a region type, the rows/cols interval, "
			+ "and provide values for the parameters in the Value column default values " + "\r"
			+ "are used for parameters with no value).";

	private final static String REGION_TYPE_LABEL = "Region type:";
	private final static String ROW_SELECTION_LABEL = "Row from/to:";
	private final static String COL_SELECTION_LABEL = "Column from/to:";

	private final static int EDITABLE_COLUMN_INDEX = 1;

	// FUNCTIONS

	public ChangeRegionsDialog() {
	}

	ChangeRegionsDialog(Controller ctrl) {
		super((Frame) null, true);
		this._ctrl = ctrl;
		initGUI();
		this._ctrl.addObserver(this);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void initGUI() {
		setTitle("Change Regions");
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		setContentPane(mainPanel);

		// CREATES THE FOUR PANELS IN WHICH THE MAIN PANEL IS DIVIDED, NAMELY:
		// INFORMATION PANEL, TABLE PANEL, COBOBOX PANEL AND BUTTONS PANEL
		JPanel helpPanel = new JPanel();
		JPanel tablePanel = new JPanel();
		JPanel comboBoxPanel = new JPanel();
		JPanel buttonsPanel = new JPanel();

		// ----------------------------------------------- //

		// CONFIGURES HELP PANEL

		JLabel helpInfoLabel = new JLabel();
		helpInfoLabel.setText(HELP_TEXT);
		helpPanel.add(helpInfoLabel);
		mainPanel.add(helpPanel);

		// ----------------------------------------------- //

		// CONFIGURES REGIONS' TABLE

		// _regionsInfo se usarÃ¡ para establecer la informaciÃ³n en la tabla
		this._regionsInfo = Main.region_factory.get_info();

		// _dataTableModel es un modelo de tabla que incluye todos los parametros de
		// la region
		this._dataTableModel = new DefaultTableModel() {
			@Override
			public boolean isCellEditable(int row, int column) {
				// ENSURE THAT ONLY THE SECOND COLUMN OF THE TABLE IS EDITABLE (EXCLUDING THE
				// FIRST ROW)
				return column == EDITABLE_COLUMN_INDEX;
			}
		};
		this._dataTableModel.setColumnIdentifiers(this._headers);
		this._regionsTable = new JTable(this._dataTableModel);

		// CREATES A JSCROLLPANE SO THAT IT'S POSSIBLE TO SEE THE TABLE'S HEADERS
		JScrollPane dialogTable = new JScrollPane();
		dialogTable.getViewport().add(this._regionsTable, null);
		tablePanel.setLayout(new BorderLayout());
		tablePanel.add(dialogTable);
		mainPanel.add(tablePanel);

		// ----------------------------------------------- //

		// COFIGURES THE COMBOBOXES AS WELL AS THEIR MODELS

		// INITIALIZES _regionsModel AS A COMBOBOX MODEL THAT INCLUDES THE DIFFERENT
		// TYPES OF REGIONS
		this._regionsModel = new DefaultComboBoxModel<>();

		// INITIALIZES ALL THE JLABELS PRESENT IN THE COMBOBOX PANEL
		JLabel regionTypeLabel = new JLabel(REGION_TYPE_LABEL);
		JLabel rowSelectLabel = new JLabel(ROW_SELECTION_LABEL);
		JLabel colSelectLabel = new JLabel(COL_SELECTION_LABEL);

		// ADD ALL EXISTING REGION TYPES TO THE _regionsModel COMBOBOX MODEL
		for (JSONObject jO : this._regionsInfo) {
			String regionType = jO.getString("type");
			this._regionsModel.addElement(regionType);
		}
		JComboBox regionsComboBox = new JComboBox(this._regionsModel);
		regionsComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// RESETEAMOS LA TABLA PARA QUE NO SE ACULUMEN VALORES
				ChangeRegionsDialog.this._dataTableModel.setRowCount(0);

				int regionIndex = regionsComboBox.getSelectedIndex();
				ChangeRegionsDialog.this.setTableBySelectedRegion(regionIndex,
						ChangeRegionsDialog.this._dataTableModel);
			}
		});

		// CREATES THE 4 NECESSARY COMBOBOX MODELS
		this._fromRowModel = new DefaultComboBoxModel<>();
		this._toRowModel = new DefaultComboBoxModel<>();
		this._fromColModel = new DefaultComboBoxModel<>();
		this._toColModel = new DefaultComboBoxModel<>();

		// INITIALIZES 4 COMBOBOX THAT USE THESE MODELS
		JComboBox fromRowComboBox = new JComboBox(this._fromRowModel);
		JComboBox toRowComboBox = new JComboBox(this._toRowModel);
		JComboBox fromColComboBox = new JComboBox(this._fromColModel);
		JComboBox toColComboBox = new JComboBox(this._toColModel);

		// ADDS THE COMBOBOXES AND THEIR RESPECTIVE JLABELS TO THE COMBOBOX PANEL,
		// AND ADDS THE COMBOBOX PANEL TO THE MAIN PANEL AS WELL
		comboBoxPanel.add(regionTypeLabel);
		comboBoxPanel.add(regionsComboBox);
		comboBoxPanel.add(rowSelectLabel);
		comboBoxPanel.add(fromRowComboBox);
		comboBoxPanel.add(toRowComboBox);
		comboBoxPanel.add(colSelectLabel);
		comboBoxPanel.add(fromColComboBox);
		comboBoxPanel.add(toColComboBox);
		mainPanel.add(comboBoxPanel);

		// ----------------------------------------------- //

		// CONFIGURES THE BUTTONS

		// CREATES THE "OK" AND "Cancel" BUTTONS
		JButton cancelButton = new JButton("Cancel");
		JButton okButton = new JButton("OK");

		// ACTIONS TO TAKE PLACE WHEN THE "Cancel" BUTTON IS PRESSED
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ChangeRegionsDialog.this.setVisible(false);
			}
		});

		// ACTIONS TO TAKE PLACE WHEN THE "OK" BUTTON IS PRESSED
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					ChangeRegionsDialog.this.tableToJSONObject();
					ChangeRegionsDialog.this.setVisible(false);
				} catch (IllegalArgumentException exc) {
					ViewUtils.showErrorMsg("Unable to properly configure the regions.");
				}
			}
		});

		// ADDS BOTH BUTTONS TO THE BUTTONS PANEL
		buttonsPanel.add(cancelButton);
		buttonsPanel.add(okButton);
		mainPanel.add(buttonsPanel);

		// ----------------------------------------------- //

		setPreferredSize(new Dimension(900, 400)); // puedes usar otro tamaÃ±o
		setLocationRelativeTo(null);//para que aparezca en el centro
		pack();
		setResizable(false);
		setVisible(false);
	}

	public void open(Frame parent) {
		setLocation(//
				parent.getLocation().x + parent.getWidth() / 2 - getWidth() / 2, //
				parent.getLocation().y + parent.getHeight() / 2 - getHeight() / 2);
		pack();
		setVisible(true);
	}

	@Override
	public void onRegister(double time, MapInfo map, List<Animalnfo> animals) {

		// ADDS THE EXISTING ROWS AND COLUMNS TO THEIR CORRESPONDING COMBOBOX MODELS
		for (int i = 0; i < map.get_rows(); i++) {
			this._fromRowModel.addElement(String.valueOf(i));
			this._toRowModel.addElement(String.valueOf(i));
		}

		for (int i = 0; i < map.get_cols(); i++) {
			this._fromColModel.addElement(String.valueOf(i));
			this._toColModel.addElement(String.valueOf(i));
		}

	}

	@Override
	public void onReset(double time, MapInfo map, List<Animalnfo> animals) {

		// ADDS THE EXISTING ROWS AND COLUMNS TO THEIR CORRESPONDING COMBOBOX MODELS
		for (int i = 0; i < map.get_rows(); i++) {
			this._fromRowModel.addElement(String.valueOf(i));
			this._toRowModel.addElement(String.valueOf(i));
		}

		for (int i = 0; i < map.get_cols(); i++) {
			this._fromColModel.addElement(String.valueOf(i));
			this._toColModel.addElement(String.valueOf(i));
		}

	}

	@Override
	public void onAnimalAdded(double time, MapInfo map, List<Animalnfo> animals, Animalnfo a) {

	}

	@Override
	public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {

	}

	@Override
	public void onAdvance(double time, MapInfo map, List<Animalnfo> animals, double dt) {

	}

	// AUXILIARY FUNCTIONS

	private void setTableBySelectedRegion(int regionIndex, DefaultTableModel dialogTableModel) {
		// GET THE "data" FIELD FROM THE OBTAINED JSON
		JSONObject data = new JSONObject();
		data = this._regionsInfo.get(regionIndex).getJSONObject("data");
		for (String param : data.keySet()) {
			// CREATE AND ADD TO THE TABLE MODEL A NEW ROW FOR EACH PARAMETER
			String[] newRow = { param, "", data.getString(param) };
			dialogTableModel.addRow(newRow);
		}

	}

	private void tableToJSONObject() {

		JSONArray regionsJSONArray = new JSONArray();
		JSONObject currentRegion = new JSONObject();
		JSONObject regionsJSON = new JSONObject();

		// SETS THE region_data JSONOBJECT
		JSONObject region_data = new JSONObject();
		for (int i = 0; i < this._regionsTable.getRowCount() / 2; i++) {

			// GET FACTOR KEY AND VALUE
			String factor_key = this._regionsTable.getValueAt(i, 0).toString();
			Double factor_value = Double.valueOf(this._regionsTable.getValueAt(i, 1).toString());

			// GET FOOD KEY AND VALUE
			String food_key = this._regionsTable.getValueAt(i + 1, 0).toString();
			Double food_value = Double.valueOf(this._regionsTable.getValueAt(i + 1, 1).toString());

			// PUT BOTH ROWS' INFORMATION INTO THE region_data JSONOBJECT
			region_data.put(factor_key, factor_value);
			region_data.put(food_key, food_value);

		}

		// OBTAINS THE CURRENT REGION'S TYPE
		String region_type = this._regionsModel.getSelectedItem().toString();

		// CREATES THE "spec" JSONOBJECT WITH THE JSONOBJECT AND THE STRING GENERATED
		// BEFORE
		JSONObject regionSpecifications = new JSONObject();
		regionSpecifications.put("type", region_type);
		regionSpecifications.put("data", region_data);

		// CONFIGURE THE rows AND cols JSONARRAYS
		JSONArray rows = new JSONArray();
		JSONArray cols = new JSONArray();
		this.getRowsAndCols(rows, cols);

		// ENTERS ALL THE ELEMENTS TO THE currentRegion JSONOBJECT
		currentRegion.put("row", rows);
		currentRegion.put("col", cols);
		currentRegion.put("spec", regionSpecifications);

		// ADDS THE CURRENT REGION TO THE ARRAY OF REGIONS
		regionsJSONArray.put(currentRegion);

		// PASS THE regionsJSON JSONOBJECT TO THE set_regions FUNCTION
		regionsJSON.put("regions", regionsJSONArray);
		this._ctrl.set_regions(regionsJSON);

	}

	private void getRowsAndCols(JSONArray rows, JSONArray cols) {

		// WE SUBSTRACT ONE TO THE VALUE OBTAINED BECAUSE THE COMBOBOX SHOWS ROW AND
		// COLUMN NUMBERS GREATER THAN 0 (SO AS NOT TO DISPLAY ROW N."0" OR COL N."0")
		int row_from = Integer.valueOf(this._fromRowModel.getSelectedItem().toString());
		int row_to = Integer.valueOf(this._toRowModel.getSelectedItem().toString());
		int col_from = Integer.valueOf(this._fromColModel.getSelectedItem().toString());
		int col_to = Integer.valueOf(this._toColModel.getSelectedItem().toString());

		rows.put(row_from);
		rows.put(row_to);
		cols.put(col_from);
		cols.put(col_to);
	}

}
