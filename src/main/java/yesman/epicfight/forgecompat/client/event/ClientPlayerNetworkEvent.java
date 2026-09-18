package yesman.epicfight.forgecompat.client.event;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import yesman.epicfight.forgecompat.eventbus.api.Event;

public class ClientPlayerNetworkEvent extends Event {
	private final LocalPlayer player;
	private final MultiPlayerGameMode gameMode;

	public ClientPlayerNetworkEvent(LocalPlayer player, MultiPlayerGameMode gameMode) {
		this.player = player;
		this.gameMode = gameMode;
	}

	public LocalPlayer getPlayer() {
		return this.player;
	}

	public MultiPlayerGameMode getMultiPlayerGameMode() {
		return this.gameMode;
	}

	public static class LoggingIn extends ClientPlayerNetworkEvent {
		public LoggingIn(LocalPlayer player, MultiPlayerGameMode gameMode) {
			super(player, gameMode);
		}
	}

	public static class LoggingOut extends ClientPlayerNetworkEvent {
		public LoggingOut(LocalPlayer player, MultiPlayerGameMode gameMode) {
			super(player, gameMode);
		}
	}

	public static class Clone extends ClientPlayerNetworkEvent {
		private final LocalPlayer oldPlayer;
		private final LocalPlayer newPlayer;

		public Clone(LocalPlayer oldPlayer, LocalPlayer newPlayer, MultiPlayerGameMode gameMode) {
			super(newPlayer, gameMode);
			this.oldPlayer = oldPlayer;
			this.newPlayer = newPlayer;
		}

		public LocalPlayer getOldPlayer() {
			return this.oldPlayer;
		}

		public LocalPlayer getNewPlayer() {
			return this.newPlayer;
		}
	}
}
