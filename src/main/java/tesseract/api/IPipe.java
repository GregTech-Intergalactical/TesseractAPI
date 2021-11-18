package tesseract.api;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import tesseract.graph.Graph;
import tesseract.util.Pos;

import javax.annotation.Nullable;

public interface IPipe extends IConnectable {

    default void onSideCapInvalidate(Direction side) {
        if (validate(side)) {
            refresh(side);
        } else {
            removeNode(side);
        }
    }

    default void addSides() {
        for (Direction side : Graph.DIRECTIONS) {
            addNode(side);
        }
    }

    @Nullable
    IPipe getValidPipe(Direction side);

    void clearConnection(Direction side);

    void setConnection(Direction side);

    void removeNode(Direction side);

    void addNode(Direction side);

    void refresh(Direction side);

    default void toggleConnection(Direction side) {
        if (connects(side)) {
            clearConnection(side);
        } else {
            setConnection(side);
        }
    }

    interface IPipeBlock {
        default IPipe getPipe(IWorldReader world, BlockPos pos) {
            TileEntity tile = world.getBlockEntity(pos);
            return tile instanceof IPipe ? (IPipe) tile : null;
        }

        default void sideChange(IWorldReader world, BlockPos pos, BlockPos neighbor) {
            IPipe pipe = getPipe(world, pos);
            if (pipe != null) {
                Direction side = Pos.blockPosToDir(neighbor, pos);
                if (!pipe.connects(side)) return;
                IPipe other = pipe.getValidPipe(side);
                BlockState state = world.getBlockState(neighbor);
                if (state.getBlock() instanceof IPipeBlock) {
                    if (other == null) {
                        pipe.clearConnection(side);
                    }
                } else {
                    boolean ok = pipe.validate(side);
                    if (ok) {
                        pipe.addNode(side);
                    } else {
                        pipe.removeNode(side);
                    }
                }
            }
        }
    }
}
