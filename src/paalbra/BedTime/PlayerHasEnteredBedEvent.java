package paalbra.BedTime;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
/**
 * This event is supposed to be triggered when a player_really_has entered a bed.
 * This would mean: player is sleeping
 */
public class PlayerHasEnteredBedEvent extends PlayerEvent {

	private static final HandlerList handlers = new HandlerList();

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	public PlayerHasEnteredBedEvent(Player who) {
		super(who);
	}
}
