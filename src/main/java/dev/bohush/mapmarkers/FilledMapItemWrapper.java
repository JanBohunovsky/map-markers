package dev.bohush.mapmarkers;

import dev.bohush.mapmarkers.mixin.MapStateInvoker;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.MapDecorationsComponent;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapDecorationType;
import net.minecraft.item.map.MapState;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class FilledMapItemWrapper {

	private final ItemStack map;
	private final ServerWorld world;

	public FilledMapItemWrapper(ItemStack map, ServerWorld world) {
		if (!map.isOf(net.minecraft.item.Items.FILLED_MAP)) {
			throw new IllegalArgumentException("ItemStack is not a filled map");
		}

		this.map = map;
		this.world = world;
	}

	/**
	 * Checks if the map has any markers.
	 *
	 * @return true if the map has any markers, false otherwise.
	 */
	public boolean hasMarkers() {
		return !getDecorations().decorations().isEmpty();
	}

	/**
	 * Gets all markers on the map.
	 *
	 * @return An Iterable collection of MapMarker objects representing all markers on the map.
	 */
	public Iterable<MapMarker> getMarkers() {
		return getDecorations().decorations().entrySet()
			.stream()
			.map(entry -> MapMarker.fromDecoration(entry.getKey(), entry.getValue()))
			.sorted(Comparator.comparingDouble(MapMarker::x))
			.sorted(Comparator.comparingDouble(MapMarker::z))
			.collect(Collectors.toList());
	}

	/**
	 * Adds a new marker to the map with default rotation (180 degrees).
	 *
	 * @param type The type of map decoration to add
	 * @param x    The X coordinate for the marker
	 * @param z    The Z coordinate for the marker
	 * @return The newly added marker, or null if the provided coordinates are outside the map bounds.
	 */
	@Nullable
	public MapMarker addMarker(MapDecorationType type, double x, double z) {
		return addMarker(type, x, z, 180);
	}

	/**
	 * Adds a new marker to the map.
	 *
	 * @param type     The type of map decoration to add
	 * @param x        The X coordinate for the marker
	 * @param z        The Z coordinate for the marker
	 * @param rotation The rotation angle in degrees
	 * @return The newly added marker, or null if the provided coordinates are outside the map bounds.
	 */
	@Nullable
	public MapMarker addMarker(MapDecorationType type, double x, double z, float rotation) {
		var mapState = getMapState();
		if (mapState == null)
			return null;

		var scale = 1 << mapState.scale;
		float dx = (float)(x - mapState.centerX) / scale;
		float dz = (float)(z - mapState.centerZ) / scale;
		if (!MapStateInvoker.invokeIsInBounds(dx, dz))
			return null;

		var decorations = getDecorations();

		var id = UUID.randomUUID().toString();

		var decorationTypeEntry = Registries.MAP_DECORATION_TYPE.getEntry(type);
		var decoration = new MapDecorationsComponent.Decoration(decorationTypeEntry, x, z, rotation);

		setDecorations(decorations.with(id, decoration));

		return MapMarker.fromDecoration(id, decoration);
	}

	/**
	 * Removes a marker from the map.
	 *
	 * @param id The unique identifier of the marker to remove
	 * @return The removed MapMarker object, or null if no marker was found with the given id.
	 */
	@Nullable
	public MapMarker removeMarker(String id) {
		var decorations = getDecorations();

		var decorationToRemove = decorations.decorations().get(id);
		if (decorationToRemove == null)
			return null;

		var updatedDecorations = decorations.decorations().entrySet()
			.stream()
			.filter(entry -> !entry.getKey().equals(id))
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		setDecorations(new MapDecorationsComponent(updatedDecorations));

		var mapState = getMapState();
		if (mapState != null) {
			((MapStateInvoker)mapState).invokeRemoveDecoration(id);
		}

		return MapMarker.fromDecoration(id, decorationToRemove);
	}

	public Text getHoverableText() {
		return map.toHoverableText();
	}

	public Text getFormattedName() {
		return map.getFormattedName();
	}

	private MapDecorationsComponent getDecorations() {
		return map.getOrDefault(DataComponentTypes.MAP_DECORATIONS, MapDecorationsComponent.DEFAULT);
	}

	private void setDecorations(MapDecorationsComponent decorations) {
		map.set(DataComponentTypes.MAP_DECORATIONS, decorations);
	}

	@Nullable
	private MapState getMapState() {
		return FilledMapItem.getMapState(map, world);
	}

}
