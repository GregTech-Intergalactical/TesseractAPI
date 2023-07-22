package tesseract.controller;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class Utils {

    public static void createExplosion(Level world, BlockPos pos, float explosionRadius, Level.ExplosionInteraction modeIn) {
        if (world instanceof ServerLevel) {
            ServerLevel w = (ServerLevel) world;
            w.explode(null, (double)pos.getX(), pos.getY() + 0.0625D, (double)pos.getZ(), explosionRadius, true, modeIn);
            w.sendParticles(ParticleTypes.SMOKE, pos.getX(), pos.getY() + 0.5D, pos.getZ(), 1, 0, 0, 0, 0.0D);
        }
    }

    public static void createFireAround(Level world, BlockPos pos) {
        boolean fired = false;
        for (Direction side : Direction.values()) {
            BlockPos offset = pos.relative(side);
            if (world.getBlockState(offset) == Blocks.AIR.defaultBlockState()) {
                world.setBlockAndUpdate(offset, Blocks.FIRE.defaultBlockState());
                fired = true;
            }
        }
        if (!fired) world.setBlockAndUpdate(pos, Blocks.FIRE.defaultBlockState());
    }
}
