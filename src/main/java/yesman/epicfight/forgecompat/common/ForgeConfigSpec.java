package yesman.epicfight.forgecompat.common;

import java.util.HashMap;
import java.util.Map;

public class ForgeConfigSpec {
	private final Map<String, ConfigValue<?>> values;

	public ForgeConfigSpec(Map<String, ConfigValue<?>> values) {
		this.values = values;
	}

	public boolean isLoaded() {
		return true;
	}

	public static class Builder {
		private final Map<String, ConfigValue<?>> values = new HashMap<>();

		public Builder comment(String... comment) {
			return this;
		}

		public Builder push(String path) {
			return this;
		}

		public Builder pop() {
			return this;
		}

		public BooleanValue define(String path, boolean defaultValue) {
			BooleanValue val = new BooleanValue(defaultValue);
			values.put(path, val);
			return val;
		}

		public BooleanValue define(String path, java.util.function.BooleanSupplier defaultSupplier) {
			BooleanValue val = new BooleanValue(defaultSupplier.getAsBoolean());
			values.put(path, val);
			return val;
		}

		@SuppressWarnings("unchecked")
		public <T> ConfigValue<T> define(String path, java.util.function.Supplier<T> defaultSupplier) {
			T defaultVal = defaultSupplier.get();
			if (defaultVal instanceof Boolean b) {
				BooleanValue val = new BooleanValue(b);
				values.put(path, val);
				return (ConfigValue<T>) val;
			}
			ConfigValue<T> val = new ConfigValue<>(defaultVal);
			values.put(path, val);
			return val;
		}

		public <T> ConfigValue<T> define(String path, T defaultValue) {
			ConfigValue<T> val = new ConfigValue<>(defaultValue);
			values.put(path, val);
			return val;
		}

		public IntValue defineInRange(String path, int defaultValue, int min, int max) {
			IntValue val = new IntValue(defaultValue);
			values.put(path, val);
			return val;
		}

		public DoubleValue defineInRange(String path, double defaultValue, double min, double max) {
			DoubleValue val = new DoubleValue(defaultValue);
			values.put(path, val);
			return val;
		}

		public <V extends Enum<V>> EnumValue<V> defineEnum(String path, V defaultValue) {
			EnumValue<V> val = new EnumValue<>(defaultValue);
			values.put(path, val);
			return val;
		}

		public <T> ConfigValue<java.util.List<? extends T>> defineList(String path, java.util.List<? extends T> defaultValue, java.util.function.Predicate<Object> elementValidator) {
			ConfigValue<java.util.List<? extends T>> val = new ConfigValue<>(defaultValue);
			values.put(path, val);
			return val;
		}

		public ForgeConfigSpec build() {
			return new ForgeConfigSpec(new HashMap<>(values));
		}
	}

	public static class ConfigValue<T> {
		private final T defaultValue;
		private T value;

		public ConfigValue(T defaultValue) {
			this.defaultValue = defaultValue;
			this.value = defaultValue;
		}

		public T get() {
			return this.value;
		}

		public void set(T value) {
			this.value = value;
		}

		public T getDefault() {
			return this.defaultValue;
		}

		public void save() {
		}
	}

	public static class BooleanValue extends ConfigValue<Boolean> {
		public BooleanValue(Boolean defaultValue) {
			super(defaultValue);
		}
	}

	public static class IntValue extends ConfigValue<Integer> {
		public IntValue(Integer defaultValue) {
			super(defaultValue);
		}
	}

	public static class DoubleValue extends ConfigValue<Double> {
		public DoubleValue(Double defaultValue) {
			super(defaultValue);
		}
	}

	public static class EnumValue<T extends Enum<T>> extends ConfigValue<T> {
		public EnumValue(T defaultValue) {
			super(defaultValue);
		}
	}
}
