package tesseract.api.capability;

import net.minecraft.core.Direction;

@FunctionalInterface
public interface ITransactionModifier {
    void modify(Object stack, Direction in, Direction out, boolean simulate);
}
