package zap.electric.api;

import net.minecraft.util.EnumFacing;
import zap.graph.IConnectable;

public interface IElectricNode extends IConnectable {
	IElectricStorage getStorage(EnumFacing facing);
	IElectricLimits getReceiverLimits(EnumFacing facing);
	int getOfferedPackets();

	@Override
	default boolean connects(EnumFacing facing) {
		return getStorage(facing) != null;
	}
}
