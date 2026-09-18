package yesman.epicfight.forgecompat.event.entity.player;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import yesman.epicfight.forgecompat.eventbus.api.Cancelable;
import yesman.epicfight.forgecompat.eventbus.api.Event;

public class PlayerInteractEvent extends Event {
	private final Player player;
	private final InteractionHand hand;
	private final BlockPos pos;
	private final Direction face;

	public PlayerInteractEvent(Player player, InteractionHand hand, BlockPos pos, Direction face) {
		this.player = player;
		this.hand = hand;
		this.pos = pos;
		this.face = face;
	}

	public Player getEntity() {
		return this.player;
	}

	public Level getLevel() {
		return this.player.level();
	}

	public InteractionHand getHand() {
		return this.hand;
	}

	public BlockPos getPos() {
		return this.pos;
	}

	public Direction getFace() {
		return this.face;
	}

	public ItemStack getItemStack() {
		return this.hand != null ? this.player.getItemInHand(this.hand) : ItemStack.EMPTY;
	}

	public yesman.epicfight.forgecompat.fml.LogicalSide getSide() {
		return this.player.level().isClientSide() ? yesman.epicfight.forgecompat.fml.LogicalSide.CLIENT : yesman.epicfight.forgecompat.fml.LogicalSide.SERVER;
	}

	@Cancelable
	public static class RightClickItem extends PlayerInteractEvent {
		public RightClickItem(Player player, InteractionHand hand) {
			super(player, hand, null, null);
		}
	}

	@Cancelable
	public static class RightClickBlock extends PlayerInteractEvent {
		public RightClickBlock(Player player, InteractionHand hand, BlockPos pos, Direction face) {
			super(player, hand, pos, face);
		}
	}

	@Cancelable
	public static class LeftClickBlock extends PlayerInteractEvent {
		public LeftClickBlock(Player player, BlockPos pos, Direction face) {
			super(player, InteractionHand.MAIN_HAND, pos, face);
		}
	}

	@Cancelable
	public static class EntityInteract extends PlayerInteractEvent {
		private final Entity target;

		public EntityInteract(Player player, InteractionHand hand, Entity target) {
			super(player, hand, target.blockPosition(), null);
			this.target = target;
		}

		public Entity getTarget() {
			return this.target;
		}
	}

	@Cancelable
	public static class EntityInteractSpecific extends EntityInteract {
		public EntityInteractSpecific(Player player, InteractionHand hand, Entity target) {
			super(player, hand, target);
		}
	}
}
