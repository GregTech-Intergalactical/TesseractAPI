package zap.electric.api;

import net.minecraft.util.math.Direction;
import zap.graph.IConnectable;

public interface IElectricNode extends IConnectable {
	IElectricStorage getStorage(Direction direction);
	IElectricLimits getReceiverLimits(Direction direction);
	int getOfferedPackets();

	@Override
	default boolean connects(Direction direction) {
		return getStorage(direction) != null;
	}
}
