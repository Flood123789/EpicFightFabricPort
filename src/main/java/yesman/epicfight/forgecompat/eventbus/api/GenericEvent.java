package yesman.epicfight.forgecompat.eventbus.api;

public class GenericEvent<T> extends Event {
	private Class<T> type;

	public GenericEvent() {
		super();
	}

	public GenericEvent(Class<T> type) {
		super();
		this.type = type;
	}

	public Class<T> getGenericType() {
		return this.type;
	}
}
