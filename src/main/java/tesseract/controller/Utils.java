package tesseract.controller;

import net.minecraft.block.Blocks;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.server.ServerWorld;

public class Utils {

    public static void createExplosion(ServerWorld world, BlockPos pos, float explosionRadius, Explosion.Mode modeIn) {
        world.createExplosion(null, pos.getX(), pos.getY() + 0.0625D, pos.getZ(), explosionRadius, true, modeIn);
        world.spawnParticle(ParticleTypes.SMOKE, pos.getX(), pos.getY() + 0.5D, pos.getZ(), 1, 0, 0, 0, 0.0D);
    }

    public static void createFireAround(ServerWorld world, BlockPos pos) {
        boolean fired = false;
        for (Direction side : Direction.values()) {
            BlockPos offset = pos.offset(side);
            if (world.getBlockState(offset) == Blocks.AIR.getDefaultState()) {
                world.setBlockState(offset, Blocks.FIRE.getDefaultState());
                fired = true;
            }
        }
        if (!fired) world.setBlockState(pos, Blocks.FIRE.getDefaultState());
    }
}
