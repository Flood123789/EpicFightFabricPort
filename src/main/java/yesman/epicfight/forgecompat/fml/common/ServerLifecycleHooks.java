package yesman.epicfight.forgecompat.fml.common;

import org.jetbrains.annotations.Nullable;

import net.minecraft.server.MinecraftServer;

public class ServerLifecycleHooks {
	private static MinecraftServer currentServer;

	public static void setCurrentServer(@Nullable MinecraftServer server) {
		currentServer = server;
	}

	@Nullable
	public static MinecraftServer getCurrentServer() {
		return currentServer;
	}
}
