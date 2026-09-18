package yesman.epicfight.config;

import yesman.epicfight.forgecompat.api.distmarker.Dist;
import yesman.epicfight.forgecompat.common.ForgeConfigSpec;
import yesman.epicfight.forgecompat.eventbus.api.SubscribeEvent;
import yesman.epicfight.forgecompat.fml.common.Mod;
import yesman.epicfight.forgecompat.fml.config.ModConfig;
import yesman.epicfight.forgecompat.fml.event.config.ModConfigEvent;
import yesman.epicfight.main.EpicFightMod;

@Mod.EventBusSubscriber(modid = EpicFightMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.DEDICATED_SERVER)
public class ServerConfig {
	private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
	public static final ForgeConfigSpec.BooleanValue ALLOW_CUSTOM_ANIMATIONS = BUILDER.define("allow_custom_animations", false);
	public static final ForgeConfigSpec SPEC = BUILDER.build();
	
	public static boolean allowCustomAnimations;
	
	@SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
		if (event.getConfig().getType() != ModConfig.Type.SERVER) {
			return;
		}
		
		allowCustomAnimations = ALLOW_CUSTOM_ANIMATIONS.get();
	}
}
