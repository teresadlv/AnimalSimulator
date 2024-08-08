package simulator.launcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.json.JSONObject;
import org.json.JSONTokener;

import simulator.control.Controller;
import simulator.factories.*;
import simulator.misc.Utils;
import simulator.model.*;
import simulator.view.MainWindow;

public class Main {

	private enum ExecMode {
		BATCH("batch", "Batch mode"), GUI("gui", "Graphical User Interface mode");

		private String _tag;
		private String _desc;

		private ExecMode(String modeTag, String modeDesc) {
			_tag = modeTag;
			_desc = modeDesc;
		}

		public String get_tag() {
			return _tag;
		}

		public String get_desc() {
			return _desc;
		}
	}

	// default values for some parameters
	//
	private final static Double _default_time = 10.0; // in seconds
	private final static Double _default_delta_time = 0.03;
	private final static int _default_width = 800;
	private final static int _default_height = 600;
	private final static int _default_rows = 15;
	private final static int _default_columns = 20;

	// some attributes to stores values corresponding to command-line parameters
	//
	public static Double _delta_time = null;
	private static Double _time = null;
	private static String _in_file = null;
	private static String _out_file = null;
	private static boolean _simple_viewer = false;
	private static ExecMode _mode = null;

	public static Factory<Region> region_factory;
	public static Factory<Animal> animal_factory;
	public static Factory<SelectionStrategy> selection_strategy_factory;

	private static void parse_args(String[] args) {

		// define the valid command line options
		//
		Options cmdLineOptions = build_options();

		// parse the command line as provided in args
		//
		CommandLineParser parser = new DefaultParser();
		try {
			CommandLine line = parser.parse(cmdLineOptions, args);
			parse_delta_time_option(line);
			parse_help_option(line, cmdLineOptions);
			parse_mode_option(line);
			parse_in_file_option(line);
			parse_out_file_option(line);
			parse_simple_viewer_option(line);
			parse_time_option(line);

			// if there are some remaining arguments, then something wrong is
			// provided in the command line!
			//
			String[] remaining = line.getArgs();
			if (remaining.length > 0) {
				String error = "Illegal arguments:";
				for (String o : remaining)
					error += (" " + o);
				throw new ParseException(error);
			}

		} catch (ParseException e) {
			System.err.println(e.getLocalizedMessage());
			System.exit(1);
		}

	}

	private static Options build_options() {
		Options cmdLineOptions = new Options();

		// Delta Time
		cmdLineOptions.addOption(Option.builder("dt").longOpt("delta-time").hasArg()
				.desc("A double representating actual time, in seconds, per simulation step. Default value: "
						+ _default_delta_time + ".")
				.build());

		// help
		cmdLineOptions.addOption(Option.builder("h").longOpt("help").desc("Print this message.").build());

		// input file
		cmdLineOptions.addOption(Option.builder("i").longOpt("input").hasArg().desc("A configuration file.").build());

		// modo de uso
		cmdLineOptions.addOption(Option.builder("m").longOpt("mode").hasArg().desc(
				"Execution Mode. Possible Values: 'batch' (Batch mode), 'gui' (Graphical User Interface mode). Default value: 'gui'.")
				.build());

		// output file
		cmdLineOptions.addOption(
				Option.builder("o").longOpt("output").hasArg().desc("Output file, where output is written.").build());

		// Simple Viewer
		cmdLineOptions.addOption(
				Option.builder("sv").longOpt("simple-viewer").desc("Show the viewer window in console mode").build());

		// steps
		cmdLineOptions.addOption(Option.builder("t").longOpt("time").hasArg()
				.desc("An real number representing the total simulation time in seconds. Default value: "
						+ _default_time + ".")
				.build());

		return cmdLineOptions;
	}

	private static void parse_help_option(CommandLine line, Options cmdLineOptions) {
		if (line.hasOption("h")) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp(Main.class.getCanonicalName(), cmdLineOptions, true);
			System.exit(0);
		}
	}

	private static void parse_in_file_option(CommandLine line) throws ParseException {
		_in_file = line.getOptionValue("i");
		if (_mode == ExecMode.BATCH && _in_file == null) {
			throw new ParseException("In batch mode an input configuration file is required");
		}
	}

	private static void parse_mode_option(CommandLine line) throws ParseException {
		if (line.hasOption("m")) { // TIENE LA OPCION M POR TANTO EVALUAMOS PARA ASIGNARLE A _MODE EL VALOR
									// CORRESPONDINETE
			String s = line.getOptionValue("m");
			s = s.toLowerCase();
			if (s.equals(ExecMode.BATCH.get_tag()))
				_mode = ExecMode.BATCH;
			else if (s.equals(ExecMode.GUI.get_tag()))
				_mode = ExecMode.GUI;
		} else { // NO TIENE LA OPCION -M, ASIGNAMOS POR DEFECTO -GUI
			_mode = ExecMode.GUI;
		}
	}

	private static void parse_out_file_option(CommandLine line) throws ParseException {
		_out_file = line.getOptionValue("o");
	}

	private static void parse_time_option(CommandLine line) throws ParseException {
		String t = line.getOptionValue("t", _default_time.toString());
		try {
			_time = Double.parseDouble(t);
			assert (_time >= 0);
		} catch (Exception e) {
			throw new ParseException("Invalid value for time: " + t);
		}
	}

	private static void parse_delta_time_option(CommandLine line) throws ParseException {
		String dt = line.getOptionValue("dt", _default_delta_time.toString());
		try {
			_delta_time = Double.parseDouble(dt);
			assert (_delta_time > 0);
		} catch (Exception e) {
			throw new ParseException("Invalid value for delta time: " + dt);
		}
	}

	private static void parse_simple_viewer_option(CommandLine line) throws ParseException {
		_simple_viewer = line.hasOption("sv");
	}

	private static void init_factories() {

		// INITIALIZATION OF STRATEGY FACTORY
		List<Builder<SelectionStrategy>> selection_strategy_builders = new ArrayList<>();
		selection_strategy_builders.add(new SelectFirstBuilder());
		selection_strategy_builders.add(new SelectClosestBuilder());
		selection_strategy_builders.add(new SelectYoungestBuilder());
		selection_strategy_factory = new BuilderBasedFactory<SelectionStrategy>(selection_strategy_builders);

		// INITIALIZATION OF ANIMAL FACTORY
		List<Builder<Animal>> animal_builders = new ArrayList<>();
		animal_builders.add(new SheepBuilder(selection_strategy_factory));
		animal_builders.add(new WolfBuilder(selection_strategy_factory));
		animal_factory = new BuilderBasedFactory<Animal>(animal_builders);

		// INITIALIZATION OF REGION FACTORY
		List<Builder<Region>> region_builders = new ArrayList<>();
		region_builders.add(new DefaultRegionBuilder());
		region_builders.add(new DynamicSupplyRegionBuilder());
		region_factory = new BuilderBasedFactory<Region>(region_builders);

	}

	private static JSONObject load_JSON_file(InputStream in) {
		return new JSONObject(new JSONTokener(in));
	}

	private static void start_batch_mode() throws Exception {
		InputStream is = new FileInputStream(new File(_in_file));
		JSONObject obj = load_JSON_file(is);
		OutputStream out = new FileOutputStream(new File(_out_file));

		int width = obj.getInt("width");
		int height = obj.getInt("height");
		int rows = obj.getInt("rows");
		int cols = obj.getInt("cols");

		Simulator sim = new Simulator(cols, rows, width, height, animal_factory, region_factory);
		Controller controller = new Controller(sim);
		
		controller.load_data(obj);
		controller.run(_time, _delta_time, _simple_viewer, out);

		out.close();
	}

	private static void start_GUI_mode() throws Exception {

		Simulator sim;
		JSONObject obj;
		Controller ctrl;

		if (_in_file != null) {
			InputStream is = new FileInputStream(new File(_in_file));
			obj = load_JSON_file(is);

			int width = obj.getInt("width");
			int height = obj.getInt("height");
			int rows = obj.getInt("rows");
			int cols = obj.getInt("cols");

			sim = new Simulator(cols, rows, width, height, animal_factory, region_factory);
			ctrl = new Controller(sim);
			ctrl.load_data(obj);
		} else {
			sim = new Simulator(_default_columns, _default_rows, _default_width, _default_height, animal_factory,
					region_factory);
			ctrl = new Controller(sim);
		}

		SwingUtilities.invokeLater(() -> new MainWindow(ctrl));
	}

	private static void start(String[] args) throws Exception {
		init_factories();
		parse_args(args);
		switch (_mode) {
		case BATCH:
			start_batch_mode();
			break;
		case GUI:
			start_GUI_mode();
			break;
		}
	}

	public static void main(String[] args) {
		Utils._rand.setSeed(2147483647l);
		try {
			start(args);
		} catch (Exception e) {
			System.err.println("Something went wrong ...");
			System.err.println();
			e.printStackTrace();
		}
	}
}
