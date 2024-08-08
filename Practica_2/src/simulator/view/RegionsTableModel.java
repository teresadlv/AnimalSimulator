package simulator.view;

import java.util.List;
import java.util.LinkedList;

import javax.swing.table.AbstractTableModel;

import simulator.control.Controller;
import simulator.model.Animalnfo;
import simulator.model.Diet;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.MapInfo.RegionData;
import simulator.model.RegionInfo;

@SuppressWarnings("serial")
public class RegionsTableModel extends AbstractTableModel implements EcoSysObserver {

	// ATRIBUTOS
	private String[] _headers; // CABECERAS
	private String[] _default_headers = { "Row", "Col", "Desc." }; // CABECERAS PREDETERMINADAS
	private Diet _diet[] = Diet.values(); // DIETAS

	static class Info { // CLASE QUE USAREMOS PARA GUARDAR TODA LA INFORMACION
		String _row;
		String _col;
		String _desc;
		int _diet_info[];
	}

	// LISTA CON INFORMACION DE CADA REGION, ESTO NOS INDICA EL NUMERO DE FILAS
	private List<Info> _regions;

	public RegionsTableModel(Controller ctrl) {
		// INICIALIZAMOS ESTRUCTURAS DE DATOS
		this._regions = new LinkedList<>(); // INICIALIZAMOS REGIONES
		initializeHeaders(); // AÑADIMOS LAS DIETAS A LAS CABECERAS
		// REGISTRAMOS THIS COMO OBSERVER
		ctrl.addObserver(this);
	}

	@Override
	public int getRowCount() {
		return this._regions.size();
	}

	@Override
	public int getColumnCount() {
		return _headers.length;
	}

	@Override
	public String getColumnName(int col) {
		return this._headers[col];
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Info info = _regions.get(rowIndex);

		/*
		 * LAS PRIMERAS 3 COLUMNAS SON DE LOS HEADERS DELFAULT ASI QUE DISTINGUIMOS PARA
		 * LA REGION CORRESPONDIENTE, ES DECIR, EN ROW
		 */
		if (columnIndex == 0) {
			return info._row;
		} else if (columnIndex == 1) {
			return info._col;

		} else if (columnIndex == 2) {
			return info._desc;
		} else {
			/*
			 * LE RESTAMOS LAS COLUMNAS QUE NO CORRESPONDEN A DIETAS PARA ASI ACCEDER
			 * DIRECTAMENTE A LA POSICION CORRESPONIENTE DE DIET_INFO
			 */
			int diet_column = columnIndex - _default_headers.length;
			return info._diet_info[diet_column];

		}
	}

	// METODOS AUXILIARES

	private void initializeHeaders() {
		this._headers = new String[_diet.length + _default_headers.length];
		for (int i = 0; i < _default_headers.length; i++) {
			this._headers[i] = _default_headers[i];
		}
		int k = this._default_headers.length;
		for (int j = 0; j < _diet.length; j++) {
			this._headers[k] = _diet[j].toString();
			k++;
		}
	}

	// CREAMOS UN METODO YA QUE REGISTER_REGION Y ONREGIONADDED TIENEN CODIGO
	// PARECIDO
	private void register_by_diet(Info info, List<Animalnfo> animals) {
		/*
		 * RECORREMOS LA INFORMACION DE CADA ANIMAL DENTRO DE LA LISTA DE ANIMALES
		 * EXTRAIDA DEL REGIONINFO
		 */
		if (!animals.isEmpty()) {
			for (Animalnfo a : animals) {
				/*
				 * USANDO ORDINAL PARA EXTRAER LA DIETA DE LOS ANIMALES, CREANDO LA LISTA
				 * DIET_INFO SIGUIENDO EL MISMO ORDEN QUE EL ORDEN DE LAS DIETAS SUMAMOS 1 A
				 * CADA DIETA POR CADA ANIMAL
				 */
				info._diet_info[a.get_diet().ordinal()]++;
			}
			
		}
		else {
			//SI LA LISTA ESTA VACIA ENTONCES RELLENAMOS DIET_INFO CON 0s POR CADA DIETA
			for (Diet d : _diet) {
				info._diet_info[d.ordinal()] = 0;
			}
		}
		this._regions.add(info);
	}

	// CREAMOS UN METODO YA QUE ONRESET Y ONADVANCE USAN EL MISMO CODIGO
	private void register_regions(MapInfo map) {
		// VACIAMOS LA LISTA PARA ACTUALIZARLA
		this._regions = new LinkedList<>();
		// RECORREMOS EL MAPA SEGUN EL REGIONDATA
		for (RegionData region : map) {
			/*
			 * DECLARAMOS EL INFO QUE LE VAMOS A METER A _REGIONS DENTRO DEL FOR PARA
			 * REINICIARLO PARA CADA REGION
			 */
			Info info = new Info();
			info._diet_info = new int[this._diet.length];

			// AÑADIMOS EL VALOR DE ROW
			info._row = String.valueOf(region.row());

			// AÑADIMOS EL VALOR DE COL
			info._col = String.valueOf(region.col());

			/*
			 * AÑADIMOS LA DESCRIPCION USANDO EL TOSTRING QUE LE HEMOS AÑADIDO A CADA REGION
			 */
			info._desc = region.r().toString();

			/*
			 * SACAMOS DE REGIONDATA LA INFORMACION DE ANIMALES PARA PODER CALCULAR EL
			 * NUMERO DE ANIMALES POR DIETA
			 */
			List<Animalnfo> animals = region.r().getAnimalsInfo();

			register_by_diet(info, animals);

		}
	}

	// METODOS DEL OBSERVER
	@Override
	public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {
		register_regions(map);
		fireTableStructureChanged();

	}

	@Override
	public void onAdvance(double time, MapInfo map, List<Animalnfo> animals, double dt) {
		register_regions(map);
		fireTableStructureChanged();

	}

	@Override
	public void onAnimalAdded(double time, MapInfo map, List<Animalnfo> animals, Animalnfo a) {
		register_regions(map);
		fireTableStructureChanged();
	}

	@Override
	public void onRegister(double time, MapInfo map, List<Animalnfo> animals) {
		register_regions(map);
		fireTableStructureChanged();
	}

	@Override
	public void onReset(double time, MapInfo map, List<Animalnfo> animals) {
		register_regions(map);
		fireTableStructureChanged();
	}

}
