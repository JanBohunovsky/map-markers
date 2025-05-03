package dev.bohush.mapmarkers.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.bohush.mapmarkers.command.suggestion.MarkerIdSuggestionProvider;
import dev.bohush.mapmarkers.util.CommandUtils;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class RemoveMarkerCommand implements Command<ServerCommandSource> {

	public static final String NAME = "remove";

	private static final DynamicCommandExceptionType ID_NOT_FOUND = new DynamicCommandExceptionType(obj ->
		Text.translatable("map-markers.error.marker.not_found", obj));

	public static String buildString(String id) {
		return "/" + ModCommands.GROUP + " " + NAME + " " + id;
	}

	public static LiteralCommandNode<ServerCommandSource> build() {
		return literal(NAME)
			.requires(ServerCommandSource::isExecutedByPlayer)
			.then(argument("id", word())
				.suggests(new MarkerIdSuggestionProvider())
				.executes(new RemoveMarkerCommand())
			)
			.build();
	}

	@Override
	public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		var id = getString(context, "id");

		var source = context.getSource();
		var player = source.getPlayerOrThrow();
		var world = source.getWorld();

		var map = CommandUtils.getMapOrThrow(player, world);
		var removedMarker = map.removeMarker(id);
		if (removedMarker == null) {
			throw ID_NOT_FOUND.create(map.getHoverableText());
		}

		source.sendFeedback(() -> Text.translatable("map-markers.text.removed_marker",
			removedMarker.getHoverableText(), map.getHoverableText()), true);

		return SINGLE_SUCCESS;
	}
}
