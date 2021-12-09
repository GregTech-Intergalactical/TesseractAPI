package tesseract.api.capability;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import tesseract.api.IConnectable;

public abstract class TesseractBaseCapability<T extends BlockEntity & IConnectable> {
    public final T tile;
    public final Direction side;
    public final boolean isNode;
    public final ITransactionModifier callback;
    protected boolean isSending;

    public TesseractBaseCapability(T tile, Direction side, boolean isNode, ITransactionModifier callback) {
        this.tile = tile;
        this.side = side;
        this.isNode = isNode;
        this.callback = callback;
        this.isSending = false;
    }
}
