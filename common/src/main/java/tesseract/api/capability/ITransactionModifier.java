package tesseract.api.capability;

import net.minecraft.core.Direction;

@FunctionalInterface
public interface ITransactionModifier {
    boolean modify(Object stack, Direction in, Direction out, boolean simulate);

    ITransactionModifier EMPTY = (a,b,c,d) -> false;
}
