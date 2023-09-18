package tesseract.api.item;

import net.minecraft.world.item.ItemStack;
import tesseract.api.DataHolder;

public class ItemDataHolder extends DataHolder<ItemStack, ItemStack> {
    public ItemDataHolder(ItemStack data) {
        super(data, data);
    }
}
