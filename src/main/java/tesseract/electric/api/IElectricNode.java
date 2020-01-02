package tesseract.electric.api;

import tesseract.util.Dir;
import tesseract.graph.IConnectable;

public interface IElectricNode extends IConnectable {

	IElectricStorage getStorage(Dir direction);

	IElectricLimits getReceiverLimits(Dir direction);

	int getOfferedPackets();

	@Override
	default boolean connects(Dir direction) {
		return getStorage(direction) != null;
	}
}
