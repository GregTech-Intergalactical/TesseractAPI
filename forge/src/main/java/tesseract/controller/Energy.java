package tesseract.controller;

import net.minecraft.util.math.BlockPos;
import tesseract.api.gt.GTController;

import static net.minecraft.world.Explosion.Mode.*;

// TODO: Make explosions depend on voltage, amp
public class Energy extends GTController {

    /**
     * Creates instance of the tesseract.controller.
     *
     * @param dim The dimension id.
     */
    public Energy(int dim) {
        super(dim);
    }

    @Override
    public void onNodeOverVoltage(int dim, long pos, int voltage) {
        Utils.getServerWorld(dim).ifPresent(w -> Utils.createExplosion(w, BlockPos.fromLong(pos), 4.0F, DESTROY));
    }

    @Override
    public void onCableOverAmperage(int dim, long pos, int amperage) {
        Utils.getServerWorld(dim).ifPresent(w -> Utils.createFireAround(w, BlockPos.fromLong(pos)));
    }

    @Override
    public void onCableOverVoltage(int dim, long pos, int voltage) {
        Utils.getServerWorld(dim).ifPresent(w -> Utils.createFireAround(w, BlockPos.fromLong(pos)));
    }
}
