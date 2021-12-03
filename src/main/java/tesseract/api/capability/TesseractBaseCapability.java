package tesseract.api.capability;

import java.util.ArrayDeque;
import java.util.Deque;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;

public abstract class TesseractBaseCapability {
    public final TileEntity tile;
    public final Direction side;
    public final boolean isNode;
    public final ITransactionModifier callback;

    protected final Deque<Direction> modifyDirs = new ArrayDeque<>(6);

    protected boolean isSending;

    public TesseractBaseCapability(TileEntity tile, Direction side, boolean isNode, ITransactionModifier callback) {
        this.tile = tile;
        this.side = side;
        this.isNode = isNode;
        this.callback = callback;
        this.isSending = false;
    }

}
