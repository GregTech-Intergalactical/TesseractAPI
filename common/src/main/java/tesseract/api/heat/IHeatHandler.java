package tesseract.api.heat;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public interface IHeatHandler extends IHeatNode, INBTSerializable<CompoundTag> {
}
