package paalbra.BedTime;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
/**
 * This event is supposed to be triggered when a player_really_has quit the server.
 * This would mean: player is offline, player is not in any world, player is not in any
 * online playerlists on the server.
 */
public class PlayerHasQuitEvent extends PlayerEvent {

	private static final HandlerList handlers = new HandlerList();

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	public PlayerHasQuitEvent(Player who) {
		super(who);
	}
}