package tesseract.api.item;

import net.minecraft.core.Direction;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;

public interface IItemTransferable {
    LazyOptional<IItemHandler> getItemHandler(Direction side);
}
