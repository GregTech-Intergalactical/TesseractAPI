package tesseract.util;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import tesseract.TesseractPlatformUtils;
import tesseract.api.item.PlatformItemHandler;


public class ItemHandlerUtils {
    @NotNull
    public static ItemStack insertItem(PlatformItemHandler dest, @NotNull ItemStack stack, boolean simulate) {
        if (dest == null || stack.isEmpty())
            return stack;
        for (int i = 0; i < dest.getSlots(); i++) {
            stack = dest.insertItem(i, stack, simulate);
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
        }
        return stack;
    }


    public static boolean canItemStacksStack(@NotNull ItemStack a, @NotNull ItemStack b) {
        if (a.isEmpty() || !a.sameItem(b) || a.hasTag() != b.hasTag())
            return false;
        return (!a.hasTag() || a.getTag().equals(b.getTag())) && areCapsCompatible(a, b);
    }

    private static boolean areCapsCompatible(ItemStack a, ItemStack b){
        return TesseractPlatformUtils.INSTANCE.areCapsCompatible(a, b);
    }

    public static ItemStack copyStackWithSize(@NotNull ItemStack itemStack, int size) {
        if (size == 0) return ItemStack.EMPTY;
        ItemStack copy = itemStack.copy();
        copy.setCount(size);
        return copy;
    }
}
