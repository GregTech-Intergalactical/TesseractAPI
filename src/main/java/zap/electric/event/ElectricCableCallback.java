package zap.electric.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import zap.electric.api.IElectricCable;
import zap.graph.Connectivity;

/**
 * Parent holder for electric cable callbacks.
 */
public class ElectricCableCallback {
	private ElectricCableCallback() {}

	public static final Event<Update> UPDATE = EventFactory.createArrayBacked(Update.class,
			(listeners) -> (world, pos, cache) -> {
				for(Update callback: listeners) {
					ActionResult result = callback.update(world, pos, cache);

					if(result != ActionResult.PASS) {
						return result;
					}
				}

				return ActionResult.PASS;
			}
	);

	public static final Event<Remove> REMOVE = EventFactory.createArrayBacked(Remove.class,
			(listeners) -> (world, pos) -> {
				for(Remove callback: listeners) {
					ActionResult result = callback.remove(world, pos);

					if(result != ActionResult.PASS) {
						return result;
					}
				}

				return ActionResult.PASS;
			}
	);

	/**
	 * Callback for when a cable is added or updated. This event is used by the energy network to update its internal data structures.
	 * Consumers of this event (ie, energy networks) almost never want to return SUCCESS or FAIL, but rather PASS to enable other
	 * networks to also process the update.
	 */
	public interface Update {
		ActionResult update(World world, BlockPos pos, Connectivity.Cache<IElectricCable> cache);
	}

	/**
	 * Callback for when a cable is removed in the world. Cables should dispatch this event when they are broken or unloaded,
	 * so that the energy network to update its internal data structures. Consumers of this event (ie, energy networks)
	 * almost never want to return SUCCESS or FAIL, but rather PASS to enable other networks to also process the removal.
	 */
	public interface Remove {
		ActionResult remove(World world, BlockPos pos);
	}
}
