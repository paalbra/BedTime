# Description

A simple plugin, for the [Minecraft](https://www.minecraft.net/) server mod [Spigot](https://www.spigotmc.org), that notifies all players when someone goes to bed and kick idle players. Will also list the sleeping players to the players whom are in bed.

The plugin can also be found here: https://dev.bukkit.org/projects/bed-time

# Build

You will need to get spigot.jar and place it in `jars\spigot.jar`. Instructions: https://www.spigotmc.org/wiki/buildtools/

```
mkdir build
javac -d build -classpath jars/spigot.jar src/paalbra/BedTime/*.java
cp *.yml build
jar -cvf build/BedTime.jar -C build .
```
