package tesseract.controller;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import tesseract.api.gt.GTController;

// TODO: Make explosions depend on voltage, amp
public class Energy extends GTController {

    /**
     * Creates instance of the tesseract.controller.
     *
     * @param dim The dimension id.
     */
    public Energy(Level dim) {
        super(dim);
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
