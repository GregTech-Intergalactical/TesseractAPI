package zap.electric.api;

/**
 * Provides limits for a given electric cable or node.
 */
public interface IElectricLimits {
	/**
	 * Returns the maximum amount of packets that this electrical component will permit to pass through or be received in a single tick
	 * @return A positive integer representing the maximum packets, zero or negative indicates that this component accepts no energy
	 */
	int getMaxPackets();

	/**
	 * Returns the maximum energy that this electrical component will permit to pass through or be received in a single packet
	 * @return A positive integer representing the maximum accepted energy, zero or negative indicates that this component accepts no energy
	 */
	long getMaxEnergy();
}
