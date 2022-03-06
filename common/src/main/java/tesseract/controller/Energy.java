package tesseract.controller;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
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
    public Energy(Level dim, Graph.INodeGetter<IGTNode> node) {
        super(dim, node);
    }

    @Override
    public void onNodeOverVoltage(Level w, long pos, long voltage) {
        Utils.createExplosion(w, BlockPos.of(pos), 4.0F, Explosion.BlockInteraction.BREAK);
    }

    @Override
    public void onCableOverAmperage(Level w, long pos, long amperage) {
        Utils.createFireAround(w, BlockPos.of(pos));
    }

    @Override
    public void onCableOverVoltage(Level w, long pos, long voltage) {
        Utils.createFireAround(w, BlockPos.of(pos));
    }
}