package dev.bohush.mapmarkers;

import net.minecraft.component.type.MapDecorationsComponent;
import net.minecraft.item.map.MapDecorationType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public record MapMarker(String id, RegistryEntry<MapDecorationType> type, double x, double z, float rotation) {

	public static MapMarker fromDecoration(String id, MapDecorationsComponent.Decoration decoration) {
		return new MapMarker(id, decoration.type(), decoration.x(), decoration.z(), decoration.rotation());
	}

	public Text getHoverableText() {
		return Text.literal(getTypeId().getPath())
			.styled(style -> style
				.withColor(Formatting.AQUA)
				.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
					Text.translatable("map-markers.text.marker_detail", id)))
			);
	}

	public Text getFormattedText() {
		return Text.literal(getTypeId().getPath())
			.formatted(Formatting.AQUA);
	}

	private Identifier getTypeId() {
		return type().getKey()
			.map(RegistryKey::getValue)
			.orElseGet(() -> Identifier.ofVanilla("unknown"));
	}

}
