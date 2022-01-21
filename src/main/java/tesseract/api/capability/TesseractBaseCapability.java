package tesseract.api.capability;

import java.util.ArrayDeque;
import java.util.Deque;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import tesseract.api.IConnectable;

public abstract class TesseractBaseCapability<T extends TileEntity & IConnectable> {
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
