package simulator.view;

import simulator.misc.Vector2D;
import simulator.model.Animalnfo;
import simulator.model.MapInfo;
import simulator.model.State;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.*;

@SuppressWarnings("serial")
public class MapViewer extends AbstractMapViewer {

	private static final int SHEEP_TAG_POSITION_X = 20;
	private static final int SHEEP_TAG_POSITION_Y = 462;
	private static final int SUB_FOR_NEXT_TAG_VALUE = 18;

	// Anchura/altura/ de la simulación -- se supone que siempre van a ser iguales
	// al tamaño del componente
	private int _width;
	private int _height;

	// Número de filas/columnas de la simulación
	private int _rows;
	private int _cols;

	// Anchura/altura de una región
	int _rwidth;
	int _rheight;

	// Mostramos sólo animales con este estado. Los posibles valores de _currState
	// son null, y los valores de Animal.State.values(). Si es null mostramos todo.
	State _currState;
	int state_counter;

	// En estos atributos guardamos la lista de animales y el tiempo que hemos
	// recibido la última vez para dibujarlos.
	volatile private Collection<Animalnfo> _objs;
	volatile private Double _time;

	// Una clase auxilar para almacenar información sobre una especie
	private static class SpeciesInfo {
		private Integer _count;
		private Color _color;

		SpeciesInfo(Color color) {
			_count = 0;
			_color = color;
		}
	}

	// Un mapa para la información sobre las especies
	Map<String, SpeciesInfo> _kindsInfo = new HashMap<>();

	// El font que usamos para dibujar texto
	private Font _font = new Font("Arial", Font.BOLD, 12);

	// Indica si mostramos el texto la ayuda o no
	private boolean _showHelp;

	public MapViewer() {
		this.state_counter = 0;
		initGUI();
	}

	private void initGUI() {

		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				switch (e.getKeyChar()) {
				case 'h':
					_showHelp = !_showHelp;
					repaint();
					break;
				case 's':
					State[] states = State.values();
					if (MapViewer.this.state_counter == states.length) {
						_currState = null;
						state_counter = 0;
					} else {
						// Cambiar _currState al siguiente (de manera circular). Después de null
						// viene el primero de Animal.State.values() y después del último viene null.
						_currState = states[state_counter];
						MapViewer.this.state_counter++;
					}
					repaint();
				default:
				}
			}

		});

		addMouseListener(new MouseAdapter() {

			@Override
			public void mouseEntered(MouseEvent e) {
				requestFocus(); // Esto es necesario para capturar las teclas cuando el ratón está sobre este
								// componente.
			}
		});

		// Por defecto mostramos todos los animales
		_currState = null;

		// Por defecto mostramos el texto de ayuda
		_showHelp = true;
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		Graphics2D gr = (Graphics2D) g;
		gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		gr.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		// Cambiar el font para dibujar texto
		g.setFont(_font);

		// Dibujar fondo blanco
		gr.setBackground(Color.WHITE);
		gr.clearRect(0, 0, _width, _height);

		// Dibujar los animales, el tiempo, etc.
		if (_objs != null)
			drawObjects(gr, _objs, _time);

		// TEXTO DE AYUDA
		if (this._showHelp)
			showHelp(gr);

	}

	private void showHelp(Graphics2D gr) {

		gr.setColor(Color.RED);
		gr.drawString("h: toggle help", 10, 15);
		gr.drawString("s: show animals of a specific state", 10, 35);

	}

	private boolean visible(Animalnfo a) {
		return _currState == null || a.get_state() == _currState;
	}

	private void drawObjects(Graphics2D g, Collection<Animalnfo> animals, Double time) {

		JPanel grid = new JPanel();
		grid.setLayout(new GridLayout(this._rows, this._cols));
		this._objs = animals;

		// Dibujar los animales
		for (Animalnfo a : animals) {

			// Si no es visible saltamos la iteración
			if (!visible(a))
				continue;

			// La información sobre la especie de 'a'
			SpeciesInfo esp_info = _kindsInfo.get(a.get_genetic_code());

			// Si esp_info es null, añade una entrada correspondiente al mapa

			if (esp_info == null) {
				// INICIALIZAMOS ESP_INFO
				esp_info = new SpeciesInfo(ViewUtils.get_color(a.get_genetic_code()));
				// AÑADIMOS A _KIND_INFO
				_kindsInfo.put(String.valueOf(a.get_genetic_code()),
						new SpeciesInfo(ViewUtils.get_color(a.get_genetic_code())));

			} else {
				// Incrementar el contador de la especie (es decir el contador dentro de
				// tag_info)
				esp_info._count++;
				esp_info._color = ViewUtils.get_color(a.get_genetic_code());
			}

			// Dibijar el animal en la posicion correspondiente, usando el color
			// tag_info._color. Su tamaño tiene que ser relativo a su edad, por ejemplo
			// edad/2+2. Se puede dibujar usando fillRoundRect, fillRect o fillOval.

			Vector2D pos = a.get_position();
			int x = (int) pos.getX();
			int y = (int) pos.getY();
			int size = (int) a.get_age() / 2 + 2;

			g.setColor(esp_info._color);
			g.fillOval(x, y, size, size);

		}

		// Dibujar la etiqueta del estado visible, si no es null. QUE ES LA ETIQUETA DEL
		// ESTADO VISIBLE?
		if (this._currState != null) {
			String state = _currState.toString();
			g.setColor(Color.MAGENTA);
			drawStringWithRect(g, 10, 55, "Current state: " + state + " ");
		}

		// Dibujar la etiqueta del tiempo. Para escribir solo 3 decimales puede
		// usar String.format("%.3f", time)
		String t = String.format("%.3f", time);
		g.setColor(Color.MAGENTA);
		drawStringWithRect(g, 20, 480, "Time: " + t + " ");

		// Dibujar la información de todas la especies. Al final de cada iteración
		// poner el contador de la especie correspondiente a 0 (para resetear el cuento)
		int sub_for_next_tag = 0;
		for (Entry<String, SpeciesInfo> e : _kindsInfo.entrySet()) {
			g.setColor(e.getValue()._color);
			drawStringWithRect(g, SHEEP_TAG_POSITION_X, SHEEP_TAG_POSITION_Y - sub_for_next_tag,
					e.getKey() + " " + e.getValue()._count + " ");
			sub_for_next_tag = SUB_FOR_NEXT_TAG_VALUE;
			e.getValue()._count = 0;
		}
	}

	// Un método que dibujar un texto con un rectángulo
	void drawStringWithRect(Graphics2D g, int x, int y, String s) {
		Rectangle2D rect = g.getFontMetrics().getStringBounds(s, g);
		g.drawString(s, x, y);
		g.drawRect(x - 1, y - (int) rect.getHeight(), (int) rect.getWidth() + 1, (int) rect.getHeight() + 5);
	}

	@Override
	public void update(List<Animalnfo> objs, Double time) {
		this._objs = objs;
		this._time = time;
		repaint();
	}

	@Override
	public void reset(double time, MapInfo map, List<Animalnfo> animals) {
		// ACTUALIZAMOS ATRIBUTOS
		this._width = map.get_width();
		this._height = map.get_height();
		this._rows = map.get_rows();
		this._cols = map.get_cols();

		// Esto cambia el tamaño del componente, y así cambia el tamaño de la ventana
		// porque en MapWindow llamamos a pack() después de llamar a reset
		setPreferredSize(new Dimension(map.get_width(), map.get_height()));

		// Dibuja el estado
		update(animals, time);
	}

}
