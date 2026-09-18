package yesman.epicfight.forgecompat.event.level;

import net.minecraft.world.level.LevelAccessor;
import yesman.epicfight.forgecompat.eventbus.api.Event;

public class LevelEvent extends Event {
	private final LevelAccessor level;

	public LevelEvent(LevelAccessor level) {
		this.level = level;
	}

	public LevelAccessor getLevel() {
		return this.level;
	}

	public static class Load extends LevelEvent {
		public Load(LevelAccessor level) {
			super(level);
		}
	}

	public static class Unload extends LevelEvent {
		public Unload(LevelAccessor level) {
			super(level);
		}
	}

	public static class Save extends LevelEvent {
		public Save(LevelAccessor level) {
			super(level);
		}
	}
}
