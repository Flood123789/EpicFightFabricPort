package yesman.epicfight.forgecompat.registries;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.forgecompat.eventbus.api.IEventBus;

public class DeferredRegister<T> {
	private final ResourceLocation registryName;
	private final String modid;
	private final Map<RegistryObject<T>, Supplier<? extends T>> entries = new LinkedHashMap<>();
	private Supplier<RegistryBuilder<T>> registryFactory;

	private DeferredRegister(ResourceLocation registryName, String modid) {
		this.registryName = registryName;
		this.modid = modid;
	}

	public static <T> DeferredRegister<T> create(IForgeRegistry<T> reg, String modid) {
		return new DeferredRegister<>(reg.getRegistryName(), modid);
	}

	public static <T> DeferredRegister<T> create(Registry<T> reg, String modid) {
		ResourceLocation name = reg.key().location();
		return new DeferredRegister<>(name, modid);
	}

	public static <T> DeferredRegister<T> create(net.minecraft.resources.ResourceKey<? extends Registry<T>> regKey, String modid) {
		return new DeferredRegister<>(regKey.location(), modid);
	}

	public static <T> DeferredRegister<T> create(ResourceLocation registryName, String modid) {
		return new DeferredRegister<>(registryName, modid);
	}

	public <I extends T> RegistryObject<I> register(String name, Supplier<? extends I> sup) {
		ResourceLocation id = new ResourceLocation(this.modid, name);
		RegistryObject<I> ret = new RegistryObject<>(id, sup);
		@SuppressWarnings("unchecked")
		RegistryObject<T> castedRet = (RegistryObject<T>) ret;
		@SuppressWarnings("unchecked")
		Supplier<? extends T> castedSup = (Supplier<? extends T>) sup;
		this.entries.put(castedRet, castedSup);
		return ret;
	}

	public Supplier<IForgeRegistry<T>> makeRegistry(Supplier<RegistryBuilder<T>> builder) {
		this.registryFactory = builder;
		return () -> RegistryManager.ACTIVE.createRegistry(this.registryName, builder.get());
	}

	public ResourceLocation getRegistryName() {
		return this.registryName;
	}

	public java.util.Collection<RegistryObject<T>> getEntries() {
		return Collections.unmodifiableSet(this.entries.keySet());
	}

	@Nullable
	public Supplier<RegistryBuilder<T>> getRegistryFactory() {
		return this.registryFactory;
	}

	public void register(IEventBus bus) {
		ForgeRegistry<T> reg = RegistryManager.ACTIVE.getRegistry(this.registryName);
		if (reg != null) {
			this.entries.forEach((obj, sup) -> {
				reg.register(obj.getId(), sup.get());
				obj.updateReference(reg);
			});

			reg.fireBake();
		}
	}
}
