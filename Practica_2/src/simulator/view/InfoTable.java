package simulator.view;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableModel;
import javax.swing.JTable;

@SuppressWarnings("serial")
public class InfoTable extends JPanel {

	String _title;
	TableModel _tableModel;

	public InfoTable(String title, TableModel tableModel) {
		this._title = title;
		this._tableModel = tableModel;
		initGUI();
	}

	private void initGUI() {
		// CAMBIAMOS EL LAYOUT A BORDER LAYOUT
		this.setLayout(new BorderLayout());

		// AÑADIMOS UN BORDE CON TITULO AL JPANEL CON EL TEXTO _TITLE
		this.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black, 2), this._title,
				TitledBorder.LEFT, TitledBorder.TOP));

		// AÑADIMOS JTABLE CON BARRA DE DESPLAZAMIENTO VERTICAL
		JTable table = new JTable(this._tableModel);
		this.add(new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));

	}

}
