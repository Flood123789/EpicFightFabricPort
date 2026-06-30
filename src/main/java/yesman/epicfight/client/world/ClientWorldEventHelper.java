package yesman.epicfight.client.world;

import net.minecraft.client.multiplayer.ClientLevel;
import yesman.epicfight.client.world.util.FakeLevel;

public final class ClientWorldEventHelper {
	private ClientWorldEventHelper() {
	}

	public static void loadLevel(Object level) {
		if (level instanceof FakeLevel) {
			return;
		}

		if (level instanceof ClientLevel clientLevel) {
			FakeLevel.getFakeLevel(clientLevel);
		}
	}

	public static void unloadLevel() {
		FakeLevel.unloadFakeLevel();
	}
}
