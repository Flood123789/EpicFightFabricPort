package yesman.epicfight.forgecompat.fml.javafmlmod;

import yesman.epicfight.forgecompat.common.MinecraftForge;
import yesman.epicfight.forgecompat.eventbus.api.IEventBus;

public class FMLJavaModLoadingContext {
	private static final FMLJavaModLoadingContext INSTANCE = new FMLJavaModLoadingContext();

	private FMLJavaModLoadingContext() {
	}

	public static FMLJavaModLoadingContext get() {
		return INSTANCE;
	}

	public IEventBus getModEventBus() {
		return MinecraftForge.EVENT_BUS;
	}

	public void registerConfig(Object type, Object spec) {
	}

	public <T> void registerExtensionPoint(Class<T> extensionPoint, java.util.function.Supplier<? extends T> supplier) {
	}
}
