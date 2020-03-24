package tesseract.electric.api;

import net.minecraft.block.Block;
import tesseract.util.Dir;
import tesseract.graph.IConnectable;

/**
 *
 */
public interface IElectricNode extends IConnectable {

	long getEnergyStored();
	long getEnergyCapacity();
	long getOutputAmperage();
	long getOutputVoltage();
	long getInputAmperage();
	long getInputVoltage();
	//long receiveEnergy();
	//long extractEnergy();
	boolean canReceive();
	boolean canExtract();
}
