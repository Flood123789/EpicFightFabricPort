package yesman.epicfight.forgecompat.fml;

import java.util.function.Supplier;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import yesman.epicfight.forgecompat.api.distmarker.Dist;

public class DistExecutor {
	public static void unsafeRunWhenOn(Dist dist, Supplier<Runnable> runnable) {
		if (dist == Dist.CLIENT && FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
			runnable.get().run();
		} else if (dist == Dist.DEDICATED_SERVER && FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
			runnable.get().run();
		}
	}
}
