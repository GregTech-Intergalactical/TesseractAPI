package tesseract.api.gt;

import net.minecraft.core.Direction;

public interface IEnergyHandlerTile {
    IEnergyHandler getEnergyHandler(Direction side);
}
