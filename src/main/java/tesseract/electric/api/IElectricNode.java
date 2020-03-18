package tesseract.electric.api;

import tesseract.util.Dir;
import tesseract.graph.IConnectable;

/**
 *
 */
public interface IElectricNode extends IConnectable {

	/**
	 *
	 * @param direction
	 * @return
	 */
	IElectricStorage getStorage(Dir direction);

	/**
	 *
	 * @param direction
	 * @return
	 */
	IElectricLimits getReceiverLimits(Dir direction);

	/**
	 *
	 * @return
	 */
	int getOfferedPackets();

	/**
	 *
	 * @param direction
	 * @return
	 */
	@Override
	default boolean connects(Dir direction) {
		return getStorage(direction) != null;
	}
}
