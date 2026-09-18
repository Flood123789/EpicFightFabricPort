package yesman.epicfight.compat;

import java.lang.reflect.Constructor;

import yesman.epicfight.forgecompat.api.distmarker.Dist;
import yesman.epicfight.forgecompat.api.distmarker.OnlyIn;
import yesman.epicfight.forgecompat.common.MinecraftForge;
import yesman.epicfight.forgecompat.eventbus.api.IEventBus;
import yesman.epicfight.forgecompat.fml.DistExecutor;
import yesman.epicfight.forgecompat.fml.ModLoadingException;
import yesman.epicfight.forgecompat.fml.javafmlmod.FMLJavaModLoadingContext;
import yesman.epicfight.main.EpicFightMod;

public interface ICompatModule {
	static void loadCompatModule(FMLJavaModLoadingContext context, Class<? extends ICompatModule> compatModule) {
		try {
			Constructor<? extends ICompatModule> constructor = compatModule.getConstructor();
			ICompatModule compatModuleInstance = constructor.newInstance();
			compatModuleInstance.onModEventBus(context.getModEventBus());
			compatModuleInstance.onForgeEventBus(MinecraftForge.EVENT_BUS);
			
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
				compatModuleInstance.onModEventBusClient(context.getModEventBus());
				compatModuleInstance.onForgeEventBusClient(MinecraftForge.EVENT_BUS);
			});
			
			EpicFightMod.LOGGER.info("Loaded mod compat: " + compatModule.getSimpleName());
		} catch (ModLoadingException e) {
			throw e;
		} catch (Exception e) {
			EpicFightMod.LOGGER.error("Failed to load mod compat: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	void onModEventBus(IEventBus eventBus);
	
	void onForgeEventBus(IEventBus eventBus);
	
	@OnlyIn(Dist.CLIENT)
	void onModEventBusClient(IEventBus eventBus);
	@OnlyIn(Dist.CLIENT)
	void onForgeEventBusClient(IEventBus eventBus);
}