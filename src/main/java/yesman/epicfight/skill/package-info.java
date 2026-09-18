/**
 * Skill definitions and their runtime state.
 *
 * <p>A {@code Skill} describes behavior shared by every user of that skill. A
 * per-player {@code SkillContainer} equips it in a {@code SkillSlot} and owns
 * mutable values such as duration, stacks, resource, cooldown, and synchronized
 * {@code SkillDataKey} values. Concrete skill families live in subpackages.</p>
 */
package yesman.epicfight.skill;
