package yesman.epicfight.world.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import yesman.epicfight.api.data.reloader.SkillManager;
import yesman.epicfight.main.EpicFightMod;

public class EpicFightCreativeTabs {
	public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, EpicFightMod.MODID);

	public static final RegistryObject<CreativeModeTab> ITEMS = TABS.register("items", () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
					.title(Component.translatable("itemGroup.epicfight.items"))
					.icon(() -> new ItemStack(EpicFightItems.SKILLBOOK.get()))
					.hideTitle()
					.displayItems((params, output) -> {
						EpicFightItems.ITEMS.getEntries().forEach(item -> {
							// FIXME: bad implement, maybe based protocol better yet.
							// ignore UCHIGATANA_SHEATH
							if (item == EpicFightItems.UCHIGATANA_SHEATH || item == EpicFightItems.SKILLBOOK) {
								return;
							}
							
							output.accept(item.get());
						});
						
						acceptSkillBooks(output);
					})
					.build());
	
	private static void acceptSkillBooks(CreativeModeTab.Output output) {
		SkillManager.getSkillNames(skill -> skill.getCategory().learnable() && (skill.getCreativeTab() == null || skill.getCreativeTab() == ITEMS.get())).forEach((ResourceLocation skillId) -> {
			ItemStack stack = new ItemStack(EpicFightItems.SKILLBOOK.get());
			SkillBookItem.setContainingSkill(skillId.toString(), stack);
			output.accept(stack);
		});
	}
}
