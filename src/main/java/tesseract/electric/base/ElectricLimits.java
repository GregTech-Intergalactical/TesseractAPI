package tesseract.electric.base;

import tesseract.electric.api.IElectricLimits;

public class ElectricLimits implements IElectricLimits {

	public static final ElectricLimits UNLIMITED = new ElectricLimits();

	private long maxEnergy;
	private int maxPackets;

	public ElectricLimits() {
		this(Long.MAX_VALUE);
	}

	public ElectricLimits(long maxEnergy) {
		this(maxEnergy, Integer.MAX_VALUE);
	}

	public ElectricLimits(long maxEnergy, int maxPackets) {
		this.maxEnergy = maxEnergy;
		this.maxPackets = maxPackets;
	}

	@Override
	public long getMaxEnergy() {
		return maxEnergy;
	}

	@Override
	public int getMaxPackets() {
		return maxPackets;
	}
}
