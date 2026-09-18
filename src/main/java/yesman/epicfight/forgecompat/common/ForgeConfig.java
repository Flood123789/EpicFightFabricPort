package yesman.epicfight.forgecompat.common;

import java.util.function.Supplier;

public class ForgeConfig {
	public static final Server SERVER = new Server();

	public static class Server {
		public final Supplier<Boolean> fullBoundingBoxLadders = () -> false;
	}
}
