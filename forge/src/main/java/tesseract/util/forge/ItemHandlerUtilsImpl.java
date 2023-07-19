package tesseract.util.forge;

import net.minecraft.world.item.ItemStack;

public class ItemHandlerUtilsImpl {
    private static boolean areCapsCompatible(ItemStack a, ItemStack b){
        return a.areCapsCompatible(b);
    }

}
