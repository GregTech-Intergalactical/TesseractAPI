package tesseract.api.gt;

import io.github.fabricators_of_create.porting_lib.util.LazyOptional;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

public interface EnergyTransferable {
    @Nullable
    LazyOptional<IEnergyHandler> getGTEnergyHandler(@Nullable Direction direction);
}