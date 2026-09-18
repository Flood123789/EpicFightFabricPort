package yesman.epicfight.forgecompat.common.loot;

import com.mojang.datafixers.Products;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public abstract class LootModifier implements IGlobalLootModifier {
	protected final LootItemCondition[] conditions;

	protected LootModifier(LootItemCondition[] conditions) {
		this.conditions = conditions;
	}

	@Override
	public ObjectList<ItemStack> apply(ObjectList<ItemStack> generatedLoot, LootContext context) {
		for (LootItemCondition condition : this.conditions) {
			if (!condition.test(context)) {
				return generatedLoot;
			}
		}
		return this.doApply(generatedLoot, context);
	}

	protected abstract ObjectArrayList<ItemStack> doApply(ObjectList<ItemStack> generatedLoot, LootContext context);

	protected static <T extends LootModifier> Products.P1<Mu<T>, LootItemCondition[]> codecStart(Instance<T> instance) {
		return instance.group(
			Codec.unit(new LootItemCondition[0]).fieldOf("conditions").forGetter(lm -> lm.conditions)
		);
	}
}
