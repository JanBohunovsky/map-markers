package dev.bohush.mapmarkers.util;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.bohush.mapmarkers.FilledMapItemWrapper;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;

public class CommandUtils {

	private static final SimpleCommandExceptionType NOT_HOLDING_FILLED_MAP
		= new SimpleCommandExceptionType(Text.translatable("map-markers.error.item.not_filled_map"));
	private static final SimpleCommandExceptionType EXPLORER_MAP_NOT_SUPPORTED
		= new SimpleCommandExceptionType(Text.translatable("map-markers.error.item.explorer_map.not_supported"));
	private static final SimpleCommandExceptionType MAP_IS_LOCKED
		= new SimpleCommandExceptionType(Text.translatable("map-markers.error.item.locked_map.not_supported"));

	public static FilledMapItemWrapper getMapOrThrow(PlayerEntity player, ServerWorld world) throws CommandSyntaxException {
		var itemInHand = player.getMainHandStack();
		if (itemInHand.isEmpty() || !itemInHand.isOf(net.minecraft.item.Items.FILLED_MAP)) {
			throw NOT_HOLDING_FILLED_MAP.create();
		}

		if (isExplorerMap(itemInHand)) {
			throw EXPLORER_MAP_NOT_SUPPORTED.create();
		}

		var mapState = FilledMapItem.getMapState(itemInHand, world);
		if (mapState != null && mapState.locked) {
			throw MAP_IS_LOCKED.create();
		}

		return new FilledMapItemWrapper(itemInHand, world);
	}

	private static boolean isExplorerMap(ItemStack map) {
		// Use the fact that explorer maps use a different translation key to detect them. It feels like a hack, though.
		var translationKey = map.getItem().getTranslationKey();
		var itemNameComponent = map.get(DataComponentTypes.ITEM_NAME);

		if (itemNameComponent == null)
			return false;

		if (!(itemNameComponent.getContent() instanceof TranslatableTextContent translatableContent))
			return false;

		return !translationKey.equals(translatableContent.getKey());
	}

}
