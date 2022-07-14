package tesseract.api.gt;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface IEnergyHandlerItem extends IEnergyHandler{
    @Override
    void setCapacity(long capacity);

    @NotNull
    ItemStack getContainer();
}
