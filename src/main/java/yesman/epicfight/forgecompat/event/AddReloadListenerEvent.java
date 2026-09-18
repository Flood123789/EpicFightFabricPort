package yesman.epicfight.forgecompat.event;

import java.util.function.Consumer;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import yesman.epicfight.forgecompat.eventbus.api.Event;

public class AddReloadListenerEvent extends Event {
	private final Consumer<PreparableReloadListener> listenerAdder;

	public AddReloadListenerEvent(Consumer<PreparableReloadListener> listenerAdder) {
		this.listenerAdder = listenerAdder;
	}

	public void addListener(PreparableReloadListener listener) {
		if (this.listenerAdder != null) {
			this.listenerAdder.accept(listener);
		}
	}
}
