package yesman.epicfight.forgecompat.event;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ForgeEventFactory {
	public static boolean getMobGriefingEvent(Level level, Entity entity) {
		return level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
	}

	public static boolean onEntityDestroyBlock(Entity entity, BlockPos pos, BlockState state) {
		return true;
	}

	public static int onUseItemStop(LivingEntity entity, ItemStack item, int remaining) {
		return remaining;
	}
}
