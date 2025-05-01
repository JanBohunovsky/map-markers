package dev.bohush.mapmarkers.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.bohush.mapmarkers.command.suggestion.MapDecorationTypeSuggestionProvider;
import dev.bohush.mapmarkers.util.CommandUtils;
import dev.bohush.mapmarkers.util.MapDecorationTypeUtils;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.command.argument.IdentifierArgumentType.getIdentifier;
import static net.minecraft.command.argument.IdentifierArgumentType.identifier;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class AddMarkerCommand implements Command<ServerCommandSource> {

	public static final String NAME = "add";

	private static final SimpleCommandExceptionType UNKNOWN_MARKER_TYPE
		= new SimpleCommandExceptionType(Text.translatable("map-markers.error.marker.type.unknown"));
	private static final SimpleCommandExceptionType MARKER_TYPE_NOT_SUPPORTED
		= new SimpleCommandExceptionType(Text.translatable("map-markers.error.marker.type.not_supported"));
	private static final SimpleCommandExceptionType OUT_OF_BOUNDS
		= new SimpleCommandExceptionType(Text.translatable("map-markers.error.player.out_of_bounds"));

	public static LiteralCommandNode<ServerCommandSource> build() {
		return literal(NAME)
			.requires(ServerCommandSource::isExecutedByPlayer)
			.then(argument("type", identifier())
				.suggests(new MapDecorationTypeSuggestionProvider())
				.executes(new AddMarkerCommand())
			)
			.build();
	}

	@Override
	public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		var decorationTypeId = getIdentifier(context, "type");

		var source = context.getSource();
		var world = source.getWorld();
		var player = source.getPlayerOrThrow();
		var position = player.getBlockPos();

		var decorationType = Registries.MAP_DECORATION_TYPE.get(decorationTypeId);
		if (decorationType == null){
			throw UNKNOWN_MARKER_TYPE.create();
		}
		if (!MapDecorationTypeUtils.isValidDecorationType(decorationType)) {
			throw MARKER_TYPE_NOT_SUPPORTED.create();
		}

		var map = CommandUtils.getMapOrThrow(player, world);
		var marker = map.addMarker(decorationType, position.getX(), position.getZ());
		if (marker == null) {
			throw OUT_OF_BOUNDS.create();
		}

		source.sendFeedback(() -> Text.translatable("map-markers.text.added_marker",
			marker.getHoverableText(), map.getHoverableText()), true);

		return SINGLE_SUCCESS;
	}
}
