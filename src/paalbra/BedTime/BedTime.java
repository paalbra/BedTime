package paalbra.BedTime;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class BedTime extends JavaPlugin {

	String name;
	String prefix;
	Logger log = Logger.getLogger("Minecraft");

	// Needed because kicking a player trigger the playerQuit event
	List<String> kickedPlayers = new LinkedList<String>();
	List<String> ignoreSleep = new LinkedList<String>();

	private final BedTimePlayerListener playerListener = new BedTimePlayerListener(this);
	// playerActivity is used to store the time when a player last moved
	protected HashMap<String, Long> playerActivity = new HashMap<String, Long>();
	long idletime;
	Action action;
	protected HashMap<String, Integer> countdownIds = new HashMap<String, Integer>();
	int countdown; // Countdown in seconds
	int percentage;

	@Override
	public void onDisable() {
		this.getConfig().set("sleepignore", ignoreSleep);
		this.saveConfig();
	}

	@Override
	public void onEnable() {
		name = getDescription().getName();
		prefix = "[" + name + "] ";
		// Loads the saved config and default config into memory
		this.getConfig().options().copyDefaults(true);
		idletime = this.getConfig().getInt("idletime");
		countdown = this.getConfig().getInt("countdown");
		percentage = this.getConfig().getInt("percentage");

		// Register events
		PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvents(playerListener, this);

		ignoreSleep = this.getConfig().getStringList("sleepignore");
		try {
			action = Action.valueOf(this.getConfig().getString("idleaction", "KICK").toUpperCase());
		} catch (IllegalArgumentException e) {
			action = Action.KICK;
			this.getLogger().warning(prefix + "Invalid idleaction. Setting default: " + action.toString());
		}

		// This is needed in case the plugin is reloaded while there are players
		// on the server.
		for (Player p : this.getServer().getOnlinePlayers()) {
			registerPlayerActivity(p);
			setSleepingIgnoredStatus(p);
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		if (cmd.getName().equalsIgnoreCase("bed")) {
			if (!(sender instanceof Player))
				sender.sendMessage("Players only");
			else {
				displaySleepers((Player) sender);
			}
			return true;
		} else if (cmd.getName().equalsIgnoreCase("bedcountdown")) {
			if (args.length != 1)
				return false;
			try {
				countdown = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				return false;
			}
			this.getConfig().set("countdown", countdown);
			this.saveConfig();
			sender.sendMessage(prefix + "countdown set to " + ChatColor.GREEN + countdown + "sec");
			return true;
		} else if (cmd.getName().equalsIgnoreCase("bedpercentage")) {
			if (args.length != 1)
				return false;
			try {
				percentage = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				return false;
			}
			this.getConfig().set("percentage", percentage);
			this.saveConfig();
			sender.sendMessage(prefix + "percentage set to " + ChatColor.GREEN + percentage + "%");
			return true;
		} else if (cmd.getName().equalsIgnoreCase("bedidleaction")) {
			if (args.length != 1)
				return false;
			try {
				action = Action.valueOf(args[0].toUpperCase());
			} catch (IllegalArgumentException e) {
				return false;
			}
			this.getConfig().set("idleaction", action.toString());
			this.saveConfig();
			sender.sendMessage(prefix + "idle action set to " + ChatColor.GREEN + action);
			return true;
		} else if (cmd.getName().equalsIgnoreCase("bedidletime")) {
			if (args.length != 1)
				return false;
			try {
				idletime = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				return false;
			}
			this.getConfig().set("idletime", idletime);
			this.saveConfig();
			sender.sendMessage(prefix + "idletime set to " + ChatColor.GREEN + idletime + "sec");
			return true;
		} else if (cmd.getName().equalsIgnoreCase("bedsleepignore") && args.length == 1) {
			String pname = args[0].toLowerCase();
			Player p = this.getServer().getPlayer(pname);
			if (p != null) {
				pname = p.getName();
				ignoreSleep.add(pname);
				p.setSleepingIgnored(true);
				sender.sendMessage(prefix + ChatColor.GREEN + pname + ChatColor.WHITE + " added");
			} else
				sender.sendMessage(prefix + ChatColor.RED + pname + ChatColor.WHITE + " not found");
			return true;
		} else if (cmd.getName().equalsIgnoreCase("bedsleepunignore") && args.length == 1) {
			String pname = args[0].toLowerCase();
			Player p = this.getServer().getPlayer(pname);
			if (p != null) {
				pname = p.getName();
				ignoreSleep.remove(pname);
				p.setSleepingIgnored(false);
				sender.sendMessage(prefix + ChatColor.GREEN + pname + ChatColor.WHITE + " removed");
			} else
				sender.sendMessage(prefix + ChatColor.RED + pname + ChatColor.WHITE + " not found");
			return true;
		} else
			return false;
	}

	/**
	 * @param player
	 *            The player whom should get the sleepers list displayed
	 * 
	 *            Sends a sleepers list to the given player. The nick of the
	 *            players is either green or yellow depending on whether or not
	 *            the player is sleeping in a bed.
	 */
	void displaySleepers(Player player) {

		List<Player> players = player.getWorld().getPlayers();

		String msg = prefix + "Sleeping players: ";

		for (int i = 0; i < players.size(); i++) {
			Player p = players.get(i);
			ChatColor c = ChatColor.YELLOW;
			if (p.isSleepingIgnored())
				c = ChatColor.AQUA;
			else if (p.isSleeping())
				c = ChatColor.GREEN;
			else if (isIdle(p))
				c = ChatColor.RED;
			msg += c + p.getName();
			if (i < (players.size() - 1))
				msg += ChatColor.WHITE + ", ";
		}
		player.sendMessage(msg);
	}

	/**
	 * Updates playerActivety with the current time.
	 * 
	 * @param player
	 *            The player which shall be updated.
	 */
	void registerPlayerActivity(Player player) {
		playerActivity.put(player.getName(), System.currentTimeMillis());
	}

	/**
	 * Check whether or not the given player hasn't moved for more than idletime
	 * milliseconds.
	 */
	boolean isIdle(Player player) {
		long activity = playerActivity.get(player.getName());
		long now = System.currentTimeMillis();
		if ((now - activity) > idletime * 1000)
			return true;
		else
			return false;
	}

	void kickIdlePlayers(String worldName) {
		LinkedList<Player> idlePlayers = getIdlePlayers(worldName);
		if (idlePlayers != null)
			kickPlayers(idlePlayers, prefix + "All other players were sleeping. Autokick due to idling.");
	}

	void kickNonsleepingPlayers(String worldName) {
		World w = this.getServer().getWorld(worldName);
		LinkedList<Player> nonsleepingPlayers = new LinkedList<Player>();

		for (Player p : w.getPlayers()) {
			if (!(p.isSleeping() || p.isSleepingIgnored())) {
				nonsleepingPlayers.add(p);
			}
		}
		if (!nonsleepingPlayers.isEmpty())
			kickPlayers(nonsleepingPlayers, prefix + "Autokick due to not entering bed");
	}

	/**
	 * @return LinkedList of idle players if there are only idle and sleeping
	 *         players on the server. Null if not.
	 */
	LinkedList<Player> getIdlePlayers(String worldName) {
		World w = this.getServer().getWorld(worldName);
		LinkedList<Player> idlePlayers = new LinkedList<Player>();

		boolean sleeper = false;

		for (Player p : w.getPlayers()) {
			if (!(p.isSleeping() || p.isSleepingIgnored()))
				if (isIdle(p)) {
					// If player is not sleeping and idle
					idlePlayers.add(p);
				} else {
					// If a nonsleeping player whom isn't idle is found
					return null;
				}
			else {
				// There is a sleeping player
				sleeper = true;
			}
		}
		if (idlePlayers.size() == 0 || !sleeper)
			return null;
		else
			return idlePlayers;
	}

	void kickPlayers(Collection<Player> players, String reason) {
		String playerNames = "";
		Iterator<Player> itr = players.iterator();
		while (itr.hasNext()) {
			Player p = itr.next();
			kickedPlayers.add(p.getName());
			p.kickPlayer(reason);
			playerNames += ChatColor.RED + p.getName() + ChatColor.WHITE;
			if (itr.hasNext())
				playerNames += ", ";
		}
		this.getServer().broadcastMessage(prefix + "Kicking " + playerNames);
	}

	void setSleepingIgnoredStatus(Player p) {
		if (ignoreSleep.contains(p.getName()))
			p.setSleepingIgnored(true);
		else
			p.setSleepingIgnored(false);
	}

	void takeAction(final String w) {
		if (action == Action.KICK) {
			kickIdlePlayers(w);
		} else if (action == Action.COUNTDOWN) {
			Integer countdownId = countdownIds.get(w);
			if (countdownId != null
					&& (this.getServer().getScheduler().isCurrentlyRunning(countdownId) || this.getServer().getScheduler()
							.isQueued(countdownId))) {
				// Do nothing if there is already a scheduled event
			} else {
				for (Player p : getServer().getWorld(w).getPlayers()) {
					if (!p.isSleepingIgnored())
						p.sendMessage(prefix + "Enter bed or be kicked in " + ChatColor.RED + countdown + ChatColor.WHITE + " sec");
				}
				countdownId = this.getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
					public void run() {
						kickNonsleepingPlayers(w);
					}
				}, 20 * countdown);
				if (countdownId != -1) {
					countdownIds.put(w, countdownId);
				}
			}
		} else if (action == Action.PERCENTAGE) {
			List<Player> players = getServer().getWorld(w).getPlayers();
			int canSleep = 0;
			int isSleeping = 0;
			for (Player p : players) {
				if (!p.isSleepingIgnored()) {
					canSleep++;
					if (p.isSleeping()) {
						isSleeping++;
					}
				}
			}
			int needed = (int) Math.ceil((float)percentage/100 * canSleep);
			if (isSleeping >= needed) {
				getServer().getWorld(w).setTime(0);
				this.getServer().broadcastMessage(prefix + "Rise and shine!");
			} else {
				this.getServer().broadcastMessage(prefix + "Sleepers needed " + isSleeping + "/" + needed);
			}

		} else if (action == Action.DISABLE) {
			// Do nothing
		}

		// New action has been taken: Display new list to sleeping players
		for (Player p : getServer().getWorld(w).getPlayers()) {
			if (p.isSleeping())
				displaySleepers(p);
		}
	}

	enum Action {
		COUNTDOWN, DISABLE, KICK, PERCENTAGE
	}
}
