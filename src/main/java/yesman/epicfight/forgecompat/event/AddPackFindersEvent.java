package yesman.epicfight.forgecompat.event;

import java.util.function.Consumer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import yesman.epicfight.forgecompat.eventbus.api.Event;
import yesman.epicfight.forgecompat.fml.event.lifecycle.IModBusEvent;

public class AddPackFindersEvent extends Event implements IModBusEvent {
	private final PackType packType;
	private final Consumer<Consumer<Consumer<Pack>>> sourceAdder;

	public AddPackFindersEvent(PackType packType, Consumer<Consumer<Consumer<Pack>>> sourceAdder) {
		this.packType = packType;
		this.sourceAdder = sourceAdder;
	}

	public PackType getPackType() {
		return this.packType;
	}

	public void addRepositorySource(Consumer<Consumer<Pack>> source) {
		if (this.sourceAdder != null) {
			this.sourceAdder.accept(source);
		}
	}
}
