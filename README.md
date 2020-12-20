# Description

A simple plugin, for the [Minecraft](https://www.minecraft.net/) server mod [Spigot](https://www.spigotmc.org), that notifies all players when someone goes to bed and kick idle players. Will also list the sleeping players to the players whom are in bed.

The plugin can also be found here: https://dev.bukkit.org/projects/bed-time

# Build

```bash
cd container
IMAGE_ID=$(podman build . | tail -n 1)
CONTAINER_ID=$(podman create $IMAGE_ID https://github.com/paalbra/BedTime.git)
BEDTIME_PATH=$(podman start -a $CONTAINER_ID | tail -n 1 | cut -f 2 --delimiter " ")
podman cp $CONTAINER_ID:$BEDTIME_PATH .
podman rm $CONTAINER_ID
podman image rm $IMAGE_ID
```
