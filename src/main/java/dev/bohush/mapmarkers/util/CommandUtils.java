package dev.bohush.mapmarkers.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.bohush.mapmarkers.FilledMapItemWrapper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

public class CommandUtils {

	private static final DynamicCommandExceptionType NOT_FILLED_MAP = new DynamicCommandExceptionType(
			obj -> Text.translatable("map-markers.error.item.not_filled_map",
				Text.translatable("map-markers.text.slot.%s".formatted(obj))));
	private static final DynamicCommandExceptionType NO_MAP_STATE
		= new DynamicCommandExceptionType(obj -> Text.translatable("map-markers.error.item.no_map_state", obj));
	private static final SimpleCommandExceptionType EXPLORER_MAP_NOT_SUPPORTED
		= new SimpleCommandExceptionType(Text.translatable("map-markers.error.item.explorer_map.not_supported"));
	private static final SimpleCommandExceptionType MAP_IS_LOCKED
		= new SimpleCommandExceptionType(Text.translatable("map-markers.error.item.locked_map.not_supported"));

	/**
	 * Gets and validates a map from the player's main hand.
	 */
	public static FilledMapItemWrapper getMapOrThrow(PlayerEntity player, ServerWorld world) throws CommandSyntaxException {
		return getMapOrThrow(player, world, EquipmentSlot.MAINHAND);
	}

	/**
	 * Gets and validates a map from the player's offhand.
	 */
	public static FilledMapItemWrapper getOffHandMapOrThrow(PlayerEntity player, ServerWorld world) throws CommandSyntaxException {
		return getMapOrThrow(player, world, EquipmentSlot.OFFHAND);
	}

	private static FilledMapItemWrapper getMapOrThrow(PlayerEntity player, ServerWorld world, EquipmentSlot handSlot) throws CommandSyntaxException {
		if (handSlot.getType() != EquipmentSlot.Type.HAND) {
			throw new IllegalArgumentException("Argument 'handSlot' must be of type Hand, got %s".formatted(handSlot.getType()));
		}

		var itemInHand = player.getEquippedStack(handSlot);
		if (itemInHand.isEmpty() || !itemInHand.isOf(net.minecraft.item.Items.FILLED_MAP)) {
			throw NOT_FILLED_MAP.create(handSlot.getName());
		}

		var map = FilledMapItemWrapper.create(itemInHand, world);
		if (map == null) {
			throw NO_MAP_STATE.create(itemInHand.toHoverableText());
		}

		if (map.isExplorersMap()) {
			throw EXPLORER_MAP_NOT_SUPPORTED.create();
		}

		if (map.isLocked()) {
			throw MAP_IS_LOCKED.create();
		}

		return map;
	}

}
