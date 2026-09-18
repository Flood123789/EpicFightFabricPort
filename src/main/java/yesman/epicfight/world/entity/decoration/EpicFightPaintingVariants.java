package yesman.epicfight.world.entity.decoration;

import net.minecraft.world.entity.decoration.PaintingVariant;
import yesman.epicfight.forgecompat.registries.DeferredRegister;
import yesman.epicfight.forgecompat.registries.ForgeRegistries;
import yesman.epicfight.forgecompat.registries.RegistryObject;
import yesman.epicfight.main.EpicFightMod;

public class EpicFightPaintingVariants {
	public static final DeferredRegister<PaintingVariant> PAINTING_VARIANTS = DeferredRegister.create(ForgeRegistries.PAINTING_VARIANTS, EpicFightMod.MODID);
	
	public static final RegistryObject<PaintingVariant> EPICFIGHT_LOGO = PAINTING_VARIANTS.register("epicfight_logo", () -> new PaintingVariant(64, 64));
}
