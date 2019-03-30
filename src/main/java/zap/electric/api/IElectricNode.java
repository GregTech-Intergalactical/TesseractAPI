package zap.electric.api;

import net.minecraft.util.EnumFacing;

public interface IElectricNode {
	IElectricStorage getStorage(EnumFacing facing);
	IElectricLimits getReceiverLimits(EnumFacing facing);
	int getOfferedPackets();
}
