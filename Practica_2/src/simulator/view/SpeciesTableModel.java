package simulator.view;

import java.util.List;
import java.util.Map;
import java.util.LinkedList;

import simulator.model.State;

import javax.swing.table.AbstractTableModel;

import simulator.control.Controller;
import simulator.model.Animalnfo;
import simulator.model.EcoSysObserver;
import simulator.model.MapInfo;
import simulator.model.RegionInfo;
import java.util.LinkedHashMap;

@SuppressWarnings("serial")
public class SpeciesTableModel extends AbstractTableModel implements EcoSysObserver {

	// MAPAS PARA GUARDAR TODA LA INFORMACION
	Map<String, Map<State, Integer>> info_of_each_state; // GUARDA TODA LA INFORMACION
	Map<State, Integer> info; // GUARDA LOS VALORES POR CADA ESTADO

	// OTROS ATRIBUTOS
	State[] _states = State.values(); // ESTADOS
	String _first_header = "Species"; // EL PRIMER HEADER
	String[] _headers; // PARA GUARDAR LOS HEADERS
	List<String> _species; // PARA GUARDAR LAS SPECIES

	public SpeciesTableModel(Controller ctrl) {
		// INICIALIZAMOS ESTRUCTURAS DE DATOS
		_species = new LinkedList<>();
		// INICIALIZAMOS LOS HEADERS
		initiateHeaders();
		// REGISTRAR THIS COMO OBSERVER
		ctrl.addObserver(this);
	}

	@Override
	public int getRowCount() {
		return this._species.size();
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
		// SACAMOS LA ESPECIE QUE SE PIDE SEGUN EL ROW INDEX
		String row_info = _species.get(rowIndex);

		/*
		 * CREAMOS UNA VARIABLE PARA EL VALOR PEDIDO Y LE AÑADIMOS INFO SEGUN EL ROW YA
		 * QUE LA PRIMERA CLAVE DEL MAPA DISTINGUE ENTRE ESPECIES
		 */
		Map<State, Integer> requested_value = info_of_each_state.get(row_info);

		// SI LA COLUMNA ES 0 SABEMOS QUE NOS ESTA PIDINEDO EL STRING DE LA ESPECIE EN
		// LA QUE ESTAMOS
		if (columnIndex == 0) {
			return row_info;
		} else {
			State column_info = _states[columnIndex - 1]; // SACAMOS LA INFORMACION DE LA COLUMNA PERO LE RESTAMOS 1
															// PARA QUITAR LA COLUMNA DEL IF
			return requested_value.get(column_info); // DEVOLVEMOS AHORA LA INFORMACION DEL SEGUNDO MAPA
		}

	}

	// METODOS AUXILIARES

	private void initiateHeaders() {
		// INICIALIZAMOS HEADERS AL TAMAÑO SEGUN LA CANTIDAD DE ESTADOS +1 PARA EL FIRST
		// HEADER
		this._headers = new String[_states.length + 1];

		// AÑADIMOS EL PRIMER HEADER
		this._headers[0] = _first_header;

		// YA QUE _HEADERS NO EMPEZARA EN 0, USAMOS J = 1 PARA EL FOR
		int j = 1;
		for (int i = 0; i < _states.length; i++) {
			this._headers[j] = _states[i].toString(); // AÑADIMOS EL ESTADO
			j++; // SUMAMOS UNA POSICION A J
		}
	}

	/*
	 * CREAMOS ESTE METODO YA QUE ONANIMALADDED Y ONADVANCE USAN LA MISMA TECNICA DE
	 * CLASIFICACION
	 */
	private void classify_animals(List<Animalnfo> animals) {
		/*
		 * YA QUE EN EL CONTROLLER LLAMAREMOS AL METODO PARA CADA VEZ QUE SE ACTUALIZA
		 * LA LISTA, LA REINICIAMOS ANTES DE ENRAR AL BUCLE
		 */
		info_of_each_state = new LinkedHashMap<>();

		// RECORREMOS LA LISTA DE ANIMALES
		for (Animalnfo a : animals) {
			// REINICIAMOS INFO
			info = new LinkedHashMap<>();

			/*
			 * SACAMOS LA ESPECIE DEL ANIMAL EN QUE ESTAMOS PARA PODER COMPROBAR SI EXISTE
			 * YA EN EL MAPA, Y PARA PODER ACTUALIZAR SU INFORMACION
			 */
			String species = a.get_genetic_code();

			// MIRAMOS SI EL MAPA TIENE O NO YA LA ESPECIE, Y SI NO LA TIENE, LA AÑADIMOS
			if (!info_of_each_state.containsKey(species)) {

				// MIRAMOS SI LA LISTA DE ESPECIE TIENE O NO LA ESPECIE PARA SI NO AÑADIRLA
				if (!_species.contains(species))
					_species.add(species);

				/*
				 * POR CADA ESTADO LO PONEMOS A 0 PARA QUE LUEGO CUANDO UN ANIMAL NO TENGA UN
				 * ESTADO, ESTE SE MANTENGA EN 0
				 */
				for (State s : _states) {
					info.put(s, 0);
				}

				/* AÑADIMOS AL MAPA LA ESPECIE CON INFO VACIA */
				info_of_each_state.put(species, info);
			}

			// GUARDAMOS EN INFO LA INFORMACION QUE TENEMOS DE LA ESPECIE HASTA EL MOMENTO
			info = info_of_each_state.get(species);

			// OBTENEMOS EL CONTADOR ACTUAL PARA PODER MODIFICARLO
			int cont = info.get(a.get_state());

			// ACTUALIZAMOS EL CONTADOR
			info.put(a.get_state(), cont + 1);

			// ACTUALIZAMOS LA INFORMACION DE LA ESPECIE
			info_of_each_state.put(species, info);
		}
	}

	// METODOS DEL OBSERVER
	@Override
	public void onAnimalAdded(double time, MapInfo map, List<Animalnfo> animals, Animalnfo a) {
		classify_animals(animals);
		fireTableStructureChanged();

	}

	@Override
	public void onAdvance(double time, MapInfo map, List<Animalnfo> animals, double dt) {
		classify_animals(animals);
		fireTableStructureChanged();
	}

	@Override
	public void onRegister(double time, MapInfo map, List<Animalnfo> animals) {
		classify_animals(animals);
		fireTableStructureChanged();
	}

	@Override
	public void onReset(double time, MapInfo map, List<Animalnfo> animals) {
		classify_animals(animals);
		fireTableStructureChanged();
	}

	// IMPLEMENTACIONES VACIAS
	@Override
	public void onRegionSet(int row, int col, MapInfo map, RegionInfo r) {

	}
}
