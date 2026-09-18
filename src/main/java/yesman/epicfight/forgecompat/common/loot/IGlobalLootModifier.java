package yesman.epicfight.forgecompat.common.loot;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;

public interface IGlobalLootModifier {
	ObjectList<ItemStack> apply(ObjectList<ItemStack> generatedLoot, LootContext context);

	Codec<? extends IGlobalLootModifier> codec();
}
