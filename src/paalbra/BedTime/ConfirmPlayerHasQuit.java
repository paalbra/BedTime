package paalbra.BedTime;

import org.bukkit.entity.Player;

public class ConfirmPlayerHasQuit implements Runnable {

	BedTime plugin;
	Player player;
	int numRepeat;

	public ConfirmPlayerHasQuit(BedTime plugin, Player player) {
		this.plugin = plugin;
		this.player = player;
		this.numRepeat = 10;
	}

	public ConfirmPlayerHasQuit(BedTime plugin, Player player, int numRepeat) {
		this.plugin = plugin;
		this.player = player;
		this.numRepeat = numRepeat;
	}

	@Override
	public void run() {
		if (!player.isOnline() && !plugin.getServer().getWorld(player.getWorld().getUID()).getPlayers().contains(player)) {
			plugin.getServer().getPluginManager().callEvent(new PlayerHasQuitEvent(player));
		} else if (numRepeat > 0) {
			//plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, new ConfirmPlayerHasQuit(plugin, player, numRepeat - 1), 5L);
			plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, new ConfirmPlayerHasQuit(plugin, player, numRepeat - 1), 5L);
		} else {
			plugin.log.warning(plugin.prefix + "Player(" + player.getName() + ") has not become offline even though he should have");
		}
	}
}