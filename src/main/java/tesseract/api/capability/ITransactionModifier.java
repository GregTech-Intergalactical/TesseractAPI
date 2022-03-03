package tesseract.api.capability;


import net.minecraft.util.Direction;

@FunctionalInterface
public interface ITransactionModifier {
    void modify(Object stack, Direction in, Direction out, boolean simulate);

    ITransactionModifier EMPTY = (a,b,c,d) -> {};
}
