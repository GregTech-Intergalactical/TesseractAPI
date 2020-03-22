package tesseract.electric.api;

import tesseract.util.Dir;
import tesseract.graph.IConnectable;

/**
 *
 */
public interface IElectricNode extends IConnectable {

	/**
	 *
	 * @param direction The direction vector.
	 * @return
	 */
	IElectricStorage getStorage(Dir direction);

	/**
	 *
	 * @param direction The direction vector.
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
	 * @param direction The direction vector.
	 * @return
	 */
	@Override
	default boolean connects(Dir direction) {
		return getStorage(direction) != null;
	}
}
