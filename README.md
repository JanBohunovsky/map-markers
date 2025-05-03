# Map Markers
An easy way to add built-in markers to maps using commands.

## Commands
To use any of these commands, you must be holding a map in your main hand.  
Note: Explorer maps and locked maps are not supported. This is a design choice.

### List markers
Lists all markers that are stored in the map, with an option to remove them.

Syntax: `/mapmarkers list`

### Add marker
Adds a marker of the specified type at your position to the map.  
Note: You must be in the map's area to add the marker.

Syntax: `/mapmarkers add <type>`  
Argument `type`:
- A map decoration type ID (e.g. `minecraft:village_plains`)
- Possible values are listed in-game and can be found on [wiki](https://minecraft.wiki/w/Map#Item_data) as well.
- Banners and tracked markers (e.g. `minecraft:player`) are not supported.

### Remove marker
Removes a marker from the map.

Syntax: `/mapmarkers remove <id>`  
Argument `id`:
- The ID of the stored marker.
- You can find the marker ID by hovering over the marker type in the listing.

### Synchronise markers
Synchronises the markers between the maps in your main hand and your offhand. The maps must have the same map ID.  
This command is useful if you have duplicated a map and then added a marker to only one copy, which made the maps unstackable.

Syntax: `/mapmarkers sync`