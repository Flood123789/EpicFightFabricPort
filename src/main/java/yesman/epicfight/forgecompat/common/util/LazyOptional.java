package yesman.epicfight.forgecompat.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

public class LazyOptional<T> {
	private static final LazyOptional<Object> EMPTY = new LazyOptional<>(null);

	private final NonNullSupplier<T> supplier;
	private T cached;
	private boolean isValid = true;
	private List<NonNullConsumer<LazyOptional<T>>> listeners;

	private LazyOptional(@Nullable NonNullSupplier<T> supplier) {
		this.supplier = supplier;
		if (supplier == null) {
			this.isValid = false;
		}
	}

	public static <T> LazyOptional<T> of(NonNullSupplier<T> supplier) {
		return supplier == null ? empty() : new LazyOptional<>(supplier);
	}

	@SuppressWarnings("unchecked")
	public static <T> LazyOptional<T> empty() {
		return (LazyOptional<T>) EMPTY;
	}

	public boolean isPresent() {
		return this.supplier != null && this.isValid;
	}

	public void ifPresent(NonNullConsumer<? super T> consumer) {
		if (this.isPresent()) {
			consumer.accept(this.getValue());
		}
	}

	public Optional<T> resolve() {
		return this.isPresent() ? Optional.ofNullable(this.getValue()) : Optional.empty();
	}

	public T orElse(T other) {
		return this.isPresent() ? this.getValue() : other;
	}

	public T orElseGet(NonNullSupplier<? extends T> other) {
		return this.isPresent() ? this.getValue() : other.get();
	}

	public <X extends Throwable> T orElseThrow(NonNullSupplier<? extends X> exceptionSupplier) throws X {
		if (this.isPresent()) {
			return this.getValue();
		}
		throw exceptionSupplier.get();
	}

	private T getValue() {
		if (!this.isValid || this.supplier == null) {
			throw new IllegalStateException("LazyOptional is empty or invalidated");
		}
		if (this.cached == null) {
			this.cached = this.supplier.get();
		}
		return this.cached;
	}

	@SuppressWarnings("unchecked")
	public <X> LazyOptional<X> cast() {
		return (LazyOptional<X>) this;
	}

	public void addListener(NonNullConsumer<LazyOptional<T>> listener) {
		if (this.isPresent()) {
			if (this.listeners == null) {
				this.listeners = new ArrayList<>();
			}
			this.listeners.add(listener);
		} else {
			listener.accept(this);
		}
	}

	public void invalidate() {
		if (this.isValid) {
			this.isValid = false;
			if (this.listeners != null) {
				for (NonNullConsumer<LazyOptional<T>> listener : this.listeners) {
					listener.accept(this);
				}
				this.listeners.clear();
			}
		}
	}

	public <U> LazyOptional<U> lazyMap(Function<? super T, ? extends U> mapper) {
		return this.isPresent() ? LazyOptional.of(() -> mapper.apply(this.getValue())) : empty();
	}

	public LazyOptional<T> filter(Predicate<? super T> predicate) {
		return this.isPresent() && predicate.test(this.getValue()) ? this : empty();
	}
}
