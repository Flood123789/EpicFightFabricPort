package yesman.epicfight.compat.kubejs;

import dev.latvian.mods.kubejs.typings.Info;
import yesman.epicfight.forgecompat.api.distmarker.Dist;
import yesman.epicfight.forgecompat.api.distmarker.OnlyIn;
import yesman.epicfight.client.ClientEngine;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.world.entity.eventlistener.SkillCastEvent;

public class EFUtilsJS {
    @OnlyIn(Dist.CLIENT)
    @Info("""
            Requests the server to execute a skill. Called from the client.
            """)
    public static SkillCastEvent requestExecuteSkill(Skill skill) {
        return ClientEngine.getInstance().getPlayerPatch().getSkill(skill).sendCastRequest(ClientEngine.getInstance().getPlayerPatch(), ClientEngine.getInstance().controlEngine);
    }
}
