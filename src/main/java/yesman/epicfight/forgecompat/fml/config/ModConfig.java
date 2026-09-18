package yesman.epicfight.forgecompat.fml.config;

import yesman.epicfight.forgecompat.common.ForgeConfigSpec;

public class ModConfig {
	public enum Type {
		COMMON,
		CLIENT,
		SERVER
	}

	private final Type type;
	private final ForgeConfigSpec spec;
	private final String modId;

	public ModConfig(Type type, ForgeConfigSpec spec, String modId) {
		this.type = type;
		this.spec = spec;
		this.modId = modId;
	}

	public Type getType() {
		return this.type;
	}

	public ForgeConfigSpec getSpec() {
		return this.spec;
	}

	public String getModId() {
		return this.modId;
	}

	public java.nio.file.Path getFullPath() {
		return java.nio.file.Paths.get("config", this.modId + "-" + this.type.name().toLowerCase() + ".toml");
	}
}
