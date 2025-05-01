package dev.bohush.mapmarkers.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.bohush.mapmarkers.util.CommandUtils;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.literal;

public class ListMarkersCommand implements Command<ServerCommandSource> {

	public static final String NAME = "list";

	public static LiteralCommandNode<ServerCommandSource> build() {
		return literal(NAME)
			.requires(ServerCommandSource::isExecutedByPlayer)
			.executes(new ListMarkersCommand())
			.build();
	}

	@Override
	public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
		var source = context.getSource();
		var player = source.getPlayerOrThrow();
		var world = source.getWorld();

		var map = CommandUtils.getMapOrThrow(player, world);

		if (!map.hasMarkers()) {
			source.sendFeedback(() -> Text.translatable("map-markers.text.no_markers", map.getHoverableText()), false);
			return Command.SINGLE_SUCCESS;
		}

		source.sendFeedback(() -> {
			var message = Text.translatable("map-markers.text.marker_list", map.getHoverableText());

			for (var marker : map.getMarkers()) {
				message.append("\n")
					.append(Text.literal("[❌] ")
						.styled(style -> style
							.withColor(Formatting.RED)
							.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, RemoveMarkerCommand.buildString(marker.id())))
							.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
								Text.translatable("map-markers.text.marker_list.remove",
										Text.translatable("map-markers.text.marker_list.remove.action")
											.formatted(Formatting.RED),
										marker.getFormattedText(),
										map.getFormattedName())
									.append("\n")
									.append(Text.translatable("map-markers.text.marker_detail", marker.id())))
							)
						)
					)
					.append(Text.translatable("map-markers.text.marker_list.entry",
						marker.getHoverableText(),
						(int)marker.x(),
						(int)marker.z()));
			}

			return message;
		}, false);

		return SINGLE_SUCCESS;
	}
}
