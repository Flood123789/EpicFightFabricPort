package yesman.epicfight.mixin;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import net.fabricmc.loader.api.FabricLoader;

public class EpicFightMixinPlugin implements IMixinConfigPlugin {
	@Override
	public void onLoad(String mixinPackage) {
	}

	@Override
	public String getRefMapperConfig() {
		return null;
	}

	@Override
	public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
		if (mixinClassName.startsWith("yesman.epicfight.mixin.skinlayers.")) {
			return FabricLoader.getInstance().isModLoaded("skinlayers3d");
		}

		if (mixinClassName.startsWith("yesman.epicfight.mixin.azurelib.")) {
			FabricLoader loader = FabricLoader.getInstance();
			return loader.isModLoaded("azurelib") && (loader.isModLoaded("vivecraft") || loader.isModLoaded("physicsmod"));
		}

		if (mixinClassName.startsWith("yesman.epicfight.mixin.bettercombat.")) {
			return FabricLoader.getInstance().isModLoaded("bettercombat");
		}

		return true;
	}

	@Override
	public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
	}

	@Override
	public List<String> getMixins() {
		return null;
	}

	@Override
	public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}

	@Override
	public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
	}
}
