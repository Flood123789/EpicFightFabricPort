package yesman.epicfight.main;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.core.Registry;
import yesman.epicfight.forgecompat.registries.DeferredRegister;
import yesman.epicfight.forgecompat.registries.ForgeRegistry;
import yesman.epicfight.forgecompat.registries.IForgeRegistry;
import yesman.epicfight.forgecompat.registries.RegistryBuilder;
import yesman.epicfight.forgecompat.registries.RegistryManager;
import yesman.epicfight.forgecompat.registries.RegistryObject;

/**
 * Materializes Forge-style deferred registrations during Fabric startup.
 *
 * <p>The compatibility registry classes intentionally keep their internal
 * collections private, so this one boundary uses reflection to read pending
 * entries and reconnect each {@link RegistryObject} after registration. Keeping
 * that reflection here prevents it from leaking into gameplay registries.</p>
 */
final class EpicFightFabricRegistryBridge {
	private static final Field ENTRIES_FIELD;
	private static final Field REGISTRY_FACTORY_FIELD;
	private static final Method UPDATE_REFERENCE_METHOD;
	private static final Method UPDATE_REFERENCE_VANILLA_METHOD;
	private static final Method CREATE_REGISTRY_METHOD;
	
	static {
		try {
			ENTRIES_FIELD = DeferredRegister.class.getDeclaredField("entries");
			ENTRIES_FIELD.setAccessible(true);
			REGISTRY_FACTORY_FIELD = DeferredRegister.class.getDeclaredField("registryFactory");
			REGISTRY_FACTORY_FIELD.setAccessible(true);
			UPDATE_REFERENCE_METHOD = RegistryObject.class.getDeclaredMethod("updateReference", IForgeRegistry.class);
			UPDATE_REFERENCE_METHOD.setAccessible(true);
			UPDATE_REFERENCE_VANILLA_METHOD = RegistryObject.class.getDeclaredMethod("updateReference", Registry.class);
			UPDATE_REFERENCE_VANILLA_METHOD.setAccessible(true);
			CREATE_REGISTRY_METHOD = RegistryManager.class.getDeclaredMethod("createRegistry", net.minecraft.resources.ResourceLocation.class, RegistryBuilder.class);
			CREATE_REGISTRY_METHOD.setAccessible(true);
		} catch (ReflectiveOperationException exception) {
			throw new ExceptionInInitializerError(exception);
		}
	}
	
	private EpicFightFabricRegistryBridge() {
	}
	
	static <T> void register(DeferredRegister<T> deferredRegister, IForgeRegistry<? super T> registry) {
		// Custom Epic Fight/Forge-shaped registries are created on demand. Vanilla
		// registries use the separate registerVanilla overload below.
		if (registry == null) {
			registry = createRegistry(deferredRegister);
			
			if (registry == null) {
				EpicFightMod.LOGGER.warn("Skipping unavailable Fabric-side Forge registry {}", deferredRegister.getRegistryName());
				return;
			}
		}
		
		IForgeRegistry<? super T> targetRegistry = registry;
		entries(deferredRegister).forEach((registryObject, supplier) -> {
			if (!targetRegistry.containsKey(registryObject.getId())) {
				targetRegistry.register(registryObject.getId(), supplier.get());
			}

			updateReference(registryObject, targetRegistry);
		});

		// Forge bakes a registry once its contents are final, which is what builds the
		// slave maps the id-based network codecs read. Nothing on the Fabric side fired
		// that, so those maps stayed empty and every getId() answered -1.
		if (targetRegistry instanceof ForgeRegistry<?> forgeRegistry) {
			forgeRegistry.fireBake();
		}
	}
	
	static <T> void register(DeferredRegister<T> deferredRegister, Supplier<? extends IForgeRegistry<? super T>> registrySupplier) {
		register(deferredRegister, registrySupplier.get());
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	static <T> void registerVanilla(DeferredRegister<T> deferredRegister, Registry<? super T> registry) {
		// RegistryObject.get() cannot work until its reference is updated, even
		// when the value has already been inserted by another compatibility layer.
		entries(deferredRegister).forEach((registryObject, supplier) -> {
			if (!registry.containsKey(registryObject.getId())) {
				Registry.register((Registry)registry, registryObject.getId(), supplier.get());
			}
			
			updateReference(registryObject, registry);
		});
	}
	
	static <T> IForgeRegistry<T> createRegistry(net.minecraft.resources.ResourceLocation registryName, RegistryBuilder<T> builder) {
		try {
			ForgeRegistry<T> existingRegistry = RegistryManager.ACTIVE.getRegistry(registryName);
			
			if (existingRegistry != null) {
				return existingRegistry;
			}
			
			ForgeRegistry<T> createdRegistry = (ForgeRegistry<T>) CREATE_REGISTRY_METHOD.invoke(RegistryManager.ACTIVE, registryName, builder);
			EpicFightMod.LOGGER.info("Created Fabric-side Forge registry {}", registryName);
			return createdRegistry;
		} catch (ReflectiveOperationException exception) {
			throw new IllegalStateException("Unable to create Epic Fight registry " + registryName, exception);
		}
	}
	
	@SuppressWarnings("unchecked")
	private static <T> Map<RegistryObject<T>, Supplier<? extends T>> entries(DeferredRegister<T> deferredRegister) {
		try {
			return (Map<RegistryObject<T>, Supplier<? extends T>>) ENTRIES_FIELD.get(deferredRegister);
		} catch (IllegalAccessException exception) {
			throw new IllegalStateException("Unable to read Epic Fight deferred registry entries", exception);
		}
	}
	
	@SuppressWarnings("unchecked")
	private static <T> IForgeRegistry<? super T> createRegistry(DeferredRegister<T> deferredRegister) {
		try {
			ForgeRegistry<? super T> existingRegistry = RegistryManager.ACTIVE.getRegistry(deferredRegister.getRegistryName());
			
			if (existingRegistry != null) {
				return existingRegistry;
			}
			
			Supplier<RegistryBuilder<T>> registryFactory = (Supplier<RegistryBuilder<T>>) REGISTRY_FACTORY_FIELD.get(deferredRegister);
			
			if (registryFactory == null) {
				return null;
			}
			
			return createRegistry(deferredRegister.getRegistryName(), registryFactory.get());
		} catch (ReflectiveOperationException exception) {
			throw new IllegalStateException("Unable to create Epic Fight registry " + deferredRegister.getRegistryName(), exception);
		}
	}
	
	private static <T> void updateReference(RegistryObject<T> registryObject, IForgeRegistry<?> registry) {
		try {
			UPDATE_REFERENCE_METHOD.invoke(registryObject, registry);
		} catch (ReflectiveOperationException exception) {
			throw new IllegalStateException("Unable to update Epic Fight registry object " + registryObject.getId(), exception);
		}
	}
	
	private static <T> void updateReference(RegistryObject<T> registryObject, Registry<?> registry) {
		try {
			UPDATE_REFERENCE_VANILLA_METHOD.invoke(registryObject, registry);
		} catch (ReflectiveOperationException exception) {
			throw new IllegalStateException("Unable to update Epic Fight registry object " + registryObject.getId(), exception);
		}
	}
}
