package yesman.epicfight.forgecompat.event;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import yesman.epicfight.forgecompat.eventbus.api.Event;

public class TickEvent extends Event {
	public enum Phase {
		START,
		END
	}

	public final Phase phase;

	public TickEvent(Phase phase) {
		this.phase = phase;
	}

	public static class ClientTickEvent extends TickEvent {
		public ClientTickEvent(Phase phase) {
			super(phase);
		}
	}

	public static class RenderTickEvent extends TickEvent {
		public final float renderTickTime;

		public RenderTickEvent(Phase phase, float renderTickTime) {
			super(phase);
			this.renderTickTime = renderTickTime;
		}
	}

	public static class LevelTickEvent extends TickEvent {
		public final Level level;

		public LevelTickEvent(Phase phase, Level level) {
			super(phase);
			this.level = level;
		}
	}

	public static class PlayerTickEvent extends TickEvent {
		public final Player player;

		public PlayerTickEvent(Phase phase, Player player) {
			super(phase);
			this.player = player;
		}
	}

	public static class ServerTickEvent extends TickEvent {
		public ServerTickEvent(Phase phase) {
			super(phase);
		}
	}
}
