package tesseract.api;

import net.minecraft.util.Direction;

public interface ITransactionModifier {
    default void modify(Direction incoming, Direction towards, Object transaction, boolean simulate) {

    }

    default boolean canModify(Direction incoming, Direction towards) {
        return false;
    }
}
