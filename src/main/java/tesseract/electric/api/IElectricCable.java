package tesseract.electric.api;

/**
 *
 */
public interface IElectricCable {

	/**
	 *
	 * @return
	 */
	long getLossPerBlock();

	/**
	 *
	 * @return
	 */
	IElectricLimits getPassageLimits();
}
