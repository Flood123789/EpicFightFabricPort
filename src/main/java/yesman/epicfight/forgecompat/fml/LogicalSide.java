package yesman.epicfight.forgecompat.fml;

public enum LogicalSide {
	CLIENT,
	SERVER;

	public boolean isClient() {
		return this == CLIENT;
	}

	public boolean isServer() {
		return this == SERVER;
	}
}
