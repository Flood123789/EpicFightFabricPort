package yesman.epicfight.forgecompat.fml.loading;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import yesman.epicfight.forgecompat.api.distmarker.Dist;

public class FMLEnvironment {
	public static final Dist dist = FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT ? Dist.CLIENT : Dist.DEDICATED_SERVER;
	public static final boolean production = !FabricLoader.getInstance().isDevelopmentEnvironment();
}
