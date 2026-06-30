package yesman.epicfight.client.gui.datapack;

import java.util.function.Function;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.nbt.Tag;

public class ParameterEditor {
	public static ParameterEditor of(Function<Object, Tag> toTag, Function<Tag, Object> fromTag, AbstractWidget editWidget) {
		return new ParameterEditor(toTag, fromTag, editWidget);
	}
	
	public final Function<Object, Tag> toTag;
	public final Function<Tag, Object> fromTag;
	public final AbstractWidget editWidget;
	
	private ParameterEditor(Function<Object, Tag> toTag, Function<Tag, Object> fromTag, AbstractWidget editWidget) {
		this.toTag = toTag;
		this.fromTag = fromTag;
		this.editWidget = editWidget;
	}
}
