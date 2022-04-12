package tesseract.api.gt;


import net.minecraftforge.common.util.LazyOptional;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

public interface EnergyTransferable {
    @Nullable
    LazyOptional<IEnergyHandler> getGTEnergyHandler(@Nullable Direction direction);
}