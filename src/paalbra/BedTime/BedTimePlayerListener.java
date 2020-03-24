package paalbra.BedTime;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import paalbra.BedTime.BedTime;
import paalbra.BedTime.ConfirmPlayerHasQuit;

public class BedTimePlayerListener implements Listener {

	public static BedTime plugin;

	public BedTimePlayerListener(BedTime instance) {
		plugin = instance;
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerBedEnter(PlayerBedEnterEvent event) {
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new ConfirmPlayerHasEnteredBed(plugin, event.getPlayer()));
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerHasEnteredBedEvent(PlayerHasEnteredBedEvent event) {
		final Player player = event.getPlayer();
		for (Player p : player.getWorld().getPlayers()) {
			p.sendMessage(plugin.prefix + ChatColor.GREEN + player.getName() + ChatColor.WHITE + " went to bed");
		}

		plugin.takeAction(player.getWorld().getName());
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerBedLeave(final PlayerBedLeaveEvent event) {
		// If there is a count down and no sleeping players, cancel it
		Integer taskId = plugin.countdownIds.get(event.getPlayer().getWorld().getName());
		if (taskId != null) {
			boolean cancel = true;
			for (Player p : event.getPlayer().getWorld().getPlayers()) {
				if (p.isSleeping() && p.isOnline()) {
					cancel = false;
				}
			}
			if (cancel) {
				plugin.getServer().getScheduler().cancelTask(taskId);
				plugin.countdownIds.remove(event.getPlayer().getWorld().getName());
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent event) {
		plugin.registerPlayerActivity(event.getPlayer());
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player p = event.getPlayer();
		plugin.registerPlayerActivity(p);
		plugin.setSleepingIgnoredStatus(p);
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerQuit(PlayerQuitEvent event) {
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new ConfirmPlayerHasQuit(plugin, event.getPlayer()));
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerHasQuit(PlayerHasQuitEvent event) {
		Player player = event.getPlayer();

		// If there is a count down and no sleeping players, cancel it
		Integer taskId = plugin.countdownIds.get(event.getPlayer().getWorld().getName());
		if (taskId != null) {
			boolean cancel = true;
			for (Player p : event.getPlayer().getWorld().getPlayers()) {
				if (p.isSleeping() && p.isOnline()) {
					cancel = false;
				}
			}
			if (cancel) {
				plugin.getServer().getScheduler().cancelTask(taskId);
				plugin.countdownIds.remove(event.getPlayer().getWorld().getName());
			}
		}

		// Do nothing if this is a player kicked by this plugin
		if (plugin.kickedPlayers.remove(player.getName())) {
			return;
		}

		if (plugin.action == BedTime.Action.KICK)
			plugin.takeAction(player.getWorld().getName());

		plugin.playerActivity.remove(player.getName());
	}
}
