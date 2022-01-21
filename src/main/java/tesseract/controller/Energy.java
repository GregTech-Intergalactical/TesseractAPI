package tesseract.controller;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import tesseract.api.gt.GTController;
import tesseract.api.gt.IGTNode;
import tesseract.graph.Graph;

// TODO: Make explosions depend on voltage, amp
public class Energy extends GTController {

    /**
     * Creates instance of the tesseract.controller.
     *
     * @param dim The dimension id.
     */
    public Energy(World dim, Graph.INodeGetter<IGTNode> node) {
        super(dim, node);
    }

    @Override
    public void onNodeOverVoltage(World w, long pos, long voltage) {
        Utils.createExplosion(w, BlockPos.of(pos), 4.0F, Explosion.Mode.BREAK);
    }

    @Override
    public void onCableOverAmperage(World w, long pos, long amperage) {
        Utils.createFireAround(w, BlockPos.of(pos));
    }

    @Override
    public void onCableOverVoltage(World w, long pos, long voltage) {
        Utils.createFireAround(w, BlockPos.of(pos));
    }
}
