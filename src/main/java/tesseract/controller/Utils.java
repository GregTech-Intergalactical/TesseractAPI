package tesseract.controller;

import net.minecraft.block.Blocks;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class Utils {

    public static void createExplosion(World world, BlockPos pos, float explosionRadius, Explosion.Mode modeIn) {
        if (world instanceof ServerWorld) {
            ServerWorld w = (ServerWorld) world;
            w.explode(null, pos.getX(), pos.getY() + 0.0625D, pos.getZ(), explosionRadius, true, modeIn);
            w.sendParticles(ParticleTypes.SMOKE, pos.getX(), pos.getY() + 0.5D, pos.getZ(), 1, 0, 0, 0, 0.0D);
        }
    }

    public static void createFireAround(World world, BlockPos pos) {
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
