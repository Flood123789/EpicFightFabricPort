package yesman.epicfight.forgecompat.event.entity.player;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ArrowLooseEvent extends PlayerEvent {
	private final ItemStack bow;
	private final Level level;
	private final int charge;
	private final boolean hasAmmo;

	public ArrowLooseEvent(Player player, ItemStack bow, Level level, int charge, boolean hasAmmo) {
		super(player);
		this.bow = bow;
		this.level = level;
		this.charge = charge;
		this.hasAmmo = hasAmmo;
	}

	public ItemStack getBow() {
		return this.bow;
	}

	public Level getLevel() {
		return this.level;
	}

	public int getCharge() {
		return this.charge;
	}

	public boolean hasAmmo() {
		return this.hasAmmo;
	}
}
