package zap.electric.api;

public interface IElectricCable {
	long getLossPerBlock();
	IElectricLimits getPassageLimits();
}
