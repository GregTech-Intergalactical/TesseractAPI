package tesseract.api.gt;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

import tesseract.api.rf.IEnergyHandlerRF;

public interface IEnergyHandler extends IGTNode, IEnergyHandlerRF {
    @Override
    default IEnergyHandler getEnergyHandler(){
        return this;
    }
}
