package tesseract.electric.api;

import net.minecraft.util.Direction;
import tesseract.graph.IConnectable;

public interface IElectricNode extends IConnectable {
	IElectricStorage getStorage(Direction direction);
	IElectricLimits getReceiverLimits(Direction direction);
	int getOfferedPackets();

	@Override
	default boolean connects(Direction direction) {
		return getStorage(direction) != null;
	}
}
