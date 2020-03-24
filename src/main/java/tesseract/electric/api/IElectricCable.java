package tesseract.electric.api;

import net.minecraft.block.Block;
import tesseract.graph.IConnectable;

/**
 * Provides data for a given electric cable.
 */
public interface IElectricCable extends IConnectable {

	/**
	 * Returns the energy that this electrical component will permit to lost through or be received in a single tick.
	 * @return A positive integer representing the loss energy per block, zero or negative indicates that this component doesn't have a loss.
	 */
	int getLoss();

	/**
	 * Returns the maximum amount of packets that this electrical component will permit to pass through or be received in a single tick.
	 * @return A positive integer representing the maximum packets, zero or negative indicates that this component accepts no energy.
	 */
	int getAmps();

	/**
	 * Returns the maximum energy that this electrical component will permit to pass through or be received in a single packet.
	 * @return A positive integer representing the maximum accepted energy, zero or negative indicates that this component accepts no energy.
	 */
	long getVoltage();
}
