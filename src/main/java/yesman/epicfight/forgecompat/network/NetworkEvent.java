package yesman.epicfight.forgecompat.network;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import yesman.epicfight.forgecompat.eventbus.api.Event;

public class NetworkEvent extends Event {
	public static class Context {
		private final Connection connection;
		private final ServerPlayer sender;
		private boolean packetHandled = false;

		public Context(Connection connection, @Nullable ServerPlayer sender) {
			this.connection = connection;
			this.sender = sender;
		}

		public void enqueueWork(Runnable runnable) {
			if (this.sender != null && this.sender.getServer() != null) {
				this.sender.getServer().execute(runnable);
			} else {
				net.minecraft.client.Minecraft.getInstance().execute(runnable);
			}
		}

		@Nullable
		public ServerPlayer getSender() {
			return this.sender;
		}

		public Connection getNetworkManager() {
			return this.connection;
		}

		public void setPacketHandled(boolean handled) {
			this.packetHandled = handled;
		}

		public boolean getPacketHandled() {
			return this.packetHandled;
		}
	}
}
