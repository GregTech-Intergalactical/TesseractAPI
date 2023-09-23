package tesseract.util;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
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


    public static boolean canItemStacksStack(@Nonnull ItemStack a, @Nonnull ItemStack b) {
        if (a.isEmpty() || !a.is(b.getItem()) || a.hasTag() != b.hasTag())
            return false;
        return (!a.hasTag() || a.getTag().equals(b.getTag())) && areCapsCompatible(a, b);
    }

    @ExpectPlatform
    private static boolean areCapsCompatible(ItemStack a, ItemStack b){
        throw new AssertionError();
    }

    public static ItemStack copyStackWithSize(@NotNull ItemStack itemStack, int size) {
        if (size == 0) return ItemStack.EMPTY;
        ItemStack copy = itemStack.copy();
        copy.setCount(size);
        return copy;
    }
}
