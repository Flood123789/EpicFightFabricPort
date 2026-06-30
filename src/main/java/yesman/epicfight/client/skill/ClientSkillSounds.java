package yesman.epicfight.client.skill;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import yesman.epicfight.gameasset.EpicFightSounds;

public final class ClientSkillSounds {
	private ClientSkillSounds() {
	}

	public static void playVengeanceSound() {
		// Playing sound twice fixes the original volume issue.
		Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(EpicFightSounds.VENGEANCE.get(), 1.0F, 1.0F));
		Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(EpicFightSounds.VENGEANCE.get(), 1.0F, 1.0F));
	}
}
