package tesseract.electric.api;

/**
 *
 */
public interface IElectricStorage {

	/**
	 *
	 */
	long getEnergyStored();

	/**
	 *
	 */
	long getMaxEnergyStored();

	/**
	 *
	 * @param energy
	 * @param simulate
	 * @return
	 */
	long receiveEnergy(long energy, boolean simulate);

	/**
	 *
	 * @param energy
	 * @param simulate
	 * @return
	 */
	long extractEnergy(long energy, boolean simulate);

	/**
	 * @return
	 */
	boolean canReceive();

	/**
	 * @return
	 */
	boolean canExtract();
}
