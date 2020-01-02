package tesseract.electric.api;

public interface IElectricStorage {

	long getEnergyStored();

	long getMaxEnergyStored();

	long receiveEnergy(long energy, boolean simulate);

	long extractEnergy(long energy, boolean simulate);

	boolean canReceive();

	boolean canExtract();
}
