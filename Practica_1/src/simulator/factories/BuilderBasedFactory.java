package simulator.factories;

import java.util.Collections;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import org.json.JSONObject;

public class BuilderBasedFactory<T> implements Factory<T> {
	private Map<String, Builder<T>> _builders;
	private List<JSONObject> _builders_info;

	public BuilderBasedFactory() {
		_builders = new HashMap<String, Builder<T>>();
		_builders_info = new LinkedList<JSONObject>();

	}

	public BuilderBasedFactory(List<Builder<T>> builders) {
		this();
		if (builders == null)
			throw new IllegalArgumentException("ERROR. Valor nulo en BuilderBasedFactory");

		else {
			for (Builder<T> b : builders) {
				add_builder(b);
			}
		}
	}

	public void add_builder(Builder<T> b) {
		_builders.put(b.get_type_tag(), b);
		_builders_info.add(b.get_info());
	}

	@Override
	public T create_instance(JSONObject info) {
		if (info == null) {
			throw new IllegalArgumentException("’info’ cannot be null");
		} else {

			Builder<T> builder_instancia = _builders.get(info.getString("type"));

			if (builder_instancia == null)
				throw new IllegalArgumentException("Unrecognized ‘info’:" + info.toString());

			JSONObject datos = null;
			if (info.has("data"))
				datos = info.getJSONObject("data");
			return builder_instancia.create_instance(datos);

		}
	}

	@Override
	public List<JSONObject> get_info() {
		return Collections.unmodifiableList(_builders_info);
	}

}
