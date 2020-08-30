package tesseract.controller;

import net.minecraft.block.Blocks;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import tesseract.api.electric.ElectricController;

import javax.annotation.Nullable;
import java.util.Optional;

// TODO: Make explosions depend on voltage, amp
public class Electric extends ElectricController {

    /**
     * Creates instance of the tesseract.controller.
     *
     * @param dim The dimension id.
     */
    public Electric(int dim) {
        super(dim);
    }

    @Override
    public void onNodeOverVoltage(int dim, long pos, int voltage) {
        Utils.getServerWorld(dim).ifPresent(w -> Utils.createExplosion(w, BlockPos.fromLong(pos), 4.0F, Explosion.Mode.BREAK));
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
