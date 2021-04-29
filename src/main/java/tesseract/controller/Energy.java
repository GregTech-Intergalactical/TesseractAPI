package tesseract.controller;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import tesseract.api.gt.GTController;

import java.util.function.Function;

// TODO: Make explosions depend on voltage, amp
public class Energy extends GTController {

    /**
     * Creates instance of the tesseract.controller.
     *
     * @param dim The dimension id.
     */
    public Energy(World dim) {
        super(dim);
    }

    @Override
    public void onNodeOverVoltage(World w, long pos, int voltage) {
        Utils.createExplosion(w, BlockPos.fromLong(pos), 4.0F, Explosion.Mode.BREAK);
    }

    @Override
    public void onCableOverAmperage(World w, long pos, int amperage) {
        Utils.createFireAround(w, BlockPos.fromLong(pos));
    }

    @Override
    public void onCableOverVoltage(World w, long pos, int voltage) {
        Utils.createFireAround(w, BlockPos.fromLong(pos));
    }
}
