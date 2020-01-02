package tesseract.electric.api;

public interface IElectricCable {

	long getLossPerBlock();

	IElectricLimits getPassageLimits();
}
