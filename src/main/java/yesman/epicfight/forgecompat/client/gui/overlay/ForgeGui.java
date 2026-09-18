package yesman.epicfight.forgecompat.client.gui.overlay;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;

public class ForgeGui extends Gui {
	public ForgeGui(Minecraft minecraft, net.minecraft.client.renderer.entity.ItemRenderer itemRenderer) {
		super(minecraft, itemRenderer);
	}
}
