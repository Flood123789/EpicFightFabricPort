package yesman.epicfight.forgecompat.eventbus.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class Event {
	private boolean canceled = false;
	private Result result = Result.DEFAULT;

	public Event() {
	}

	public boolean isCanceled() {
		return this.canceled;
	}

	public void setCanceled(boolean cancel) {
		if (!this.isCancelable()) {
			throw new UnsupportedOperationException("Attempted to cancel a non-cancelable event: " + this.getClass().getName());
		}
		this.canceled = cancel;
	}

	public boolean isCancelable() {
		return this.getClass().isAnnotationPresent(Cancelable.class);
	}

	public Result getResult() {
		return this.result;
	}

	public void setResult(Result result) {
		if (!this.hasResult()) {
			throw new UnsupportedOperationException("Attempted to set result for event without @HasResult: " + this.getClass().getName());
		}
		this.result = result;
	}

	public boolean hasResult() {
		return this.getClass().isAnnotationPresent(HasResult.class);
	}

	public enum Result {
		DENY,
		DEFAULT,
		ALLOW
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	public @interface HasResult {
	}
}
