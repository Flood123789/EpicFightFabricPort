package yesman.epicfight.forgecompat.registries;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class RegistryObject<T> implements Supplier<T> {
	private final ResourceLocation id;
	private Supplier<? extends T> supplier;
	private T value;
	private ResourceKey<T> resourceKey;

	public RegistryObject(ResourceLocation id, Supplier<? extends T> supplier) {
		this.id = id;
		this.supplier = supplier;
	}

	public static <T, U extends T> RegistryObject<U> create(ResourceLocation id, Supplier<? extends U> supplier) {
		return new RegistryObject<>(id, supplier);
	}

	public static <T, U extends T> RegistryObject<U> of(Supplier<? extends U> supplier) {
		return new RegistryObject<>(new ResourceLocation("epicfight", "synthetic"), supplier);
	}

	public ResourceLocation getId() {
		return this.id;
	}

	@Override
	public T get() {
		if (this.value == null && this.supplier != null) {
			this.value = this.supplier.get();
		}
		return this.value;
	}

	public void updateReference(IForgeRegistry<?> registry) {
		if (registry != null && registry.containsKey(this.id)) {
			@SuppressWarnings("unchecked")
			T registered = (T) registry.getValue(this.id);
			if (registered != null) {
				this.value = registered;
			}
		} else if (this.value == null && this.supplier != null) {
			this.value = this.supplier.get();
		}
	}

	public void updateReference(Registry<?> registry) {
		if (registry != null && registry.containsKey(this.id)) {
			@SuppressWarnings("unchecked")
			T registered = (T) registry.get(this.id);
			if (registered != null) {
				this.value = registered;
			}
		} else if (this.value == null && this.supplier != null) {
			this.value = this.supplier.get();
		}
	}

	public boolean isPresent() {
		return this.value != null || (this.supplier != null && this.get() != null);
	}

	public void ifPresent(Consumer<? super T> consumer) {
		if (this.isPresent()) {
			consumer.accept(this.get());
		}
	}

	public Optional<T> Optional() {
		return Optional.ofNullable(this.get());
	}

	public Predicate<T> predicate() {
		return val -> Objects.equals(val, this.get());
	}

	public Stream<T> stream() {
		return this.isPresent() ? Stream.of(this.get()) : Stream.empty();
	}

	public Supplier<T> lazyMap(Function<T, T> mapper) {
		return () -> mapper.apply(this.get());
	}

	@SuppressWarnings("unchecked")
	public ResourceKey<T> getKey() {
		if (this.resourceKey == null) {
			this.resourceKey = (ResourceKey<T>) ResourceKey.create(ResourceKey.createRegistryKey(new ResourceLocation(this.id.getNamespace(), "root")), this.id);
		}
		return this.resourceKey;
	}

	public void setKey(ResourceKey<T> key) {
		this.resourceKey = key;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj instanceof RegistryObject<?> other) {
			return Objects.equals(this.id, other.id);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.id);
	}
}
