package tesseract.controller;

import net.minecraft.block.Blocks;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.util.Optional;

public class Utils {
    public static Optional<World> getServerWorld(int dimension) {
        DimensionType type = DimensionType.getById(dimension);
        if (type == null) return Optional.empty();
        return Optional.of(ServerLifecycleHooks.getCurrentServer().getWorld(type));
    }

    public static void createExplosion(@Nullable World world, BlockPos pos, float explosionRadius, Explosion.Mode modeIn) {
        if (world != null) {
            if (!world.isRemote) {
                world.createExplosion(null, pos.getX(), pos.getY() + 0.0625D, pos.getZ(), explosionRadius, modeIn);
            } else {
                world.addParticle(ParticleTypes.SMOKE, pos.getX(), pos.getY() + 0.5D, pos.getZ(), 0.0D, 0.0D, 0.0D);
            }
            world.setBlockState(pos, Blocks.AIR.getDefaultState());
        }
    }

    public static void createFireAround(@Nullable World world, BlockPos pos) {
        if (world != null) {
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
}
