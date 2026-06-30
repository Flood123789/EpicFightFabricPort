package yesman.epicfight.client.gui.datapack;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import io.netty.util.internal.StringUtil;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import yesman.epicfight.api.data.reloader.SkillManager;
import yesman.epicfight.api.utils.ParseUtil;
import yesman.epicfight.client.gui.datapack.widgets.ComboBox;
import yesman.epicfight.client.gui.datapack.widgets.PopupBox;
import yesman.epicfight.client.gui.datapack.widgets.ResizableEditBox;
import yesman.epicfight.data.conditions.Condition;
import yesman.epicfight.data.conditions.entity.HasCustomTag;
import yesman.epicfight.data.conditions.entity.HealthPoint;
import yesman.epicfight.data.conditions.entity.OffhandItemCategory;
import yesman.epicfight.data.conditions.entity.PlayerName;
import yesman.epicfight.data.conditions.entity.PlayerSkillActivated;
import yesman.epicfight.data.conditions.entity.RandomChance;
import yesman.epicfight.data.conditions.entity.TargetInDistance;
import yesman.epicfight.data.conditions.entity.TargetInPov;
import yesman.epicfight.data.conditions.itemstack.TagValueCondition;
import yesman.epicfight.world.capabilities.item.WeaponCategory;

public final class ConditionParameterEditors {
	private ConditionParameterEditors() {
	}
	
	public static List<ParameterEditor> getAcceptingParameters(Condition<?> condition, Screen screen) {
		if (condition instanceof OffhandItemCategory) {
			AbstractWidget comboBox = new ComboBox<>(screen, net.minecraft.client.Minecraft.getInstance().font, 0, 0, 0, 0, null, null, 4, Component.literal("category"), List.copyOf(WeaponCategory.ENUM_MANAGER.universalValues()), ParseUtil::snakeToSpacedCamel, null);
			return List.of(ParameterEditor.of((value) -> StringTag.valueOf(value.toString().toLowerCase(Locale.ROOT)), (tag) -> WeaponCategory.ENUM_MANAGER.get(ParseUtil.nullOrToString(tag, Tag::getAsString)), comboBox));
		}
		
		if (condition instanceof PlayerSkillActivated) {
			AbstractWidget popupBox = new PopupBox.RegistryPopupBox<>(screen, net.minecraft.client.Minecraft.getInstance().font, 0, 0, 0, 0, null, null, Component.literal("skill"), SkillManager.getSkillRegistry(), null);
			return List.of(ParameterEditor.of((skill) -> StringTag.valueOf(skill.toString()), (tag) -> SkillManager.getSkill(ParseUtil.nullOrToString(tag, Tag::getAsString)), popupBox));
		}
		
		if (condition instanceof PlayerName) {
			ResizableEditBox editBox = new ResizableEditBox(net.minecraft.client.Minecraft.getInstance().font, 0, 0, 0, 0, Component.literal("name"), null, null);
			return List.of(ParameterEditor.of((name) -> StringTag.valueOf(name.toString()), (tag) -> ParseUtil.nullOrToString(tag, Tag::getAsString), editBox));
		}
		
		if (condition instanceof HealthPoint) {
			ResizableEditBox editBox = new ResizableEditBox(net.minecraft.client.Minecraft.getInstance().font, 0, 0, 0, 0, Component.literal("health"), null, null);
			AbstractWidget comboBox = new ComboBox<>(screen, net.minecraft.client.Minecraft.getInstance().font, 0, 0, 0, 0, null, null, 4, Component.literal("comparator"), List.of(HealthPoint.Comparator.values()), ParseUtil::snakeToSpacedCamel, null);
			
			editBox.setFilter((context) -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsable(context, Float::parseFloat));
			
			return List.of(
				ParameterEditor.of((value) -> ParseUtil.parseOrGet(value.toString(), (v) -> FloatTag.valueOf(Float.parseFloat(value.toString())), StringTag.valueOf("")), (tag) -> ParseUtil.valueOfOmittingType(ParseUtil.nullOrToString(tag, Tag::getAsString)), editBox),
				ParameterEditor.of((value) -> StringTag.valueOf(value.toString().toLowerCase(Locale.ROOT)), (tag) -> ParseUtil.enumValueOfOrNull(HealthPoint.Comparator.class, ParseUtil.nullOrToString(tag, Tag::getAsString)), comboBox)
			);
		}
		
		if (condition instanceof RandomChance) {
			ResizableEditBox editBox = new ResizableEditBox(net.minecraft.client.Minecraft.getInstance().font, 0, 0, 0, 0, Component.literal("chance"), null, null);
			editBox.setFilter((context) -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsable(context, Double::parseDouble));
			return List.of(ParameterEditor.of((value) -> ParseUtil.parseOrGet(value.toString(), (v) -> FloatTag.valueOf(Float.parseFloat(value.toString())), StringTag.valueOf("")), (tag) -> ParseUtil.valueOfOmittingType(ParseUtil.nullOrToString(tag, Tag::getAsString)), editBox));
		}
		
		if (condition instanceof TargetInDistance || condition instanceof TargetInPov) {
			ResizableEditBox minEditBox = new ResizableEditBox(net.minecraft.client.Minecraft.getInstance().font, 0, 0, 0, 0, Component.literal("min"), null, null);
			ResizableEditBox maxEditBox = new ResizableEditBox(net.minecraft.client.Minecraft.getInstance().font, 0, 0, 0, 0, Component.literal("max"), null, null);
			minEditBox.setFilter((context) -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsable(context, Double::parseDouble));
			maxEditBox.setFilter((context) -> StringUtil.isNullOrEmpty(context) || ParseUtil.isParsable(context, Double::parseDouble));
			Function<Object, Tag> doubleParser = (value) -> DoubleTag.valueOf(Double.valueOf(value.toString()));
			Function<Tag, Object> doubleGetter = (tag) -> ParseUtil.valueOfOmittingType(ParseUtil.nullOrToString(tag, Tag::getAsString));
			return List.of(ParameterEditor.of(doubleParser, doubleGetter, minEditBox), ParameterEditor.of(doubleParser, doubleGetter, maxEditBox));
		}
		
		if (condition instanceof HasCustomTag) {
			ResizableEditBox editBox = new ResizableEditBox(net.minecraft.client.Minecraft.getInstance().font, 0, 0, 0, 0, Component.literal("tag"), null, null);
			return List.of(ParameterEditor.of((value) -> StringTag.valueOf(value.toString()), (tag) -> ParseUtil.nullOrToString(tag, Tag::getAsString), editBox));
		}
		
		if (condition instanceof TagValueCondition) {
			ResizableEditBox keyEditBox = new ResizableEditBox(net.minecraft.client.Minecraft.getInstance().font, 0, 0, 0, 0, Component.literal("key"), null, null);
			ResizableEditBox valueEditBox = new ResizableEditBox(net.minecraft.client.Minecraft.getInstance().font, 0, 0, 0, 0, Component.literal("value"), null, null);
			Function<Object, Tag> stringParser = (value) -> StringTag.valueOf(value.toString());
			Function<Tag, Object> stringGetter = (tag) -> ParseUtil.nullOrToString(tag, Tag::getAsString);
			return List.of(ParameterEditor.of(stringParser, stringGetter, keyEditBox), ParameterEditor.of(stringParser, stringGetter, valueEditBox));
		}
		
		return List.of();
	}
}
