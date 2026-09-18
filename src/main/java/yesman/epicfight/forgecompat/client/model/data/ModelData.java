package yesman.epicfight.forgecompat.client.model.data;

import java.util.HashMap;
import java.util.Map;

public class ModelData {
	public static final ModelData EMPTY = new ModelData(new HashMap<>());

	private final Map<ModelProperty<?>, Object> properties;

	private ModelData(Map<ModelProperty<?>, Object> properties) {
		this.properties = properties;
	}

	public static Builder builder() {
		return new Builder();
	}

	@SuppressWarnings("unchecked")
	public <T> T get(ModelProperty<T> property) {
		return (T) this.properties.get(property);
	}

	public static class Builder {
		private final Map<ModelProperty<?>, Object> properties = new HashMap<>();

		public <T> Builder with(ModelProperty<T> property, T value) {
			this.properties.put(property, value);
			return this;
		}

		public ModelData build() {
			return new ModelData(new HashMap<>(properties));
		}
	}
}
