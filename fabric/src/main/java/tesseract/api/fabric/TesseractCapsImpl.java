package tesseract.api.fabric;


import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.Nullable;
import tesseract.api.gt.EnergyTransferable;
import tesseract.api.gt.IEnergyHandler;
import tesseract.api.heat.IHeatHandler;

public class TesseractCapsImpl {
    public static final Capability<IEnergyHandler> ENERGY_HANDLER_CAPABILITY = new Capability<>("gt_energy");

    public static final Capability<IHeatHandler> HEAT_CAPABILITY = new Capability<>("heat");
    public static Capability<IEnergyHandler> getENERGY_HANDLER_CAPABILITY(){
        return ENERGY_HANDLER_CAPABILITY;
    }
    public static Capability<IHeatHandler> getHEAT_CAPABILITY(){
        return HEAT_CAPABILITY;
    }

    public static LazyOptional<IEnergyHandler> getGTEnergyHandler(Level level, BlockPos pos) {
        return getGTEnergyHandler(level, pos, null);
    }

    public static LazyOptional<IEnergyHandler> getGTEnergyHandler(Level level, BlockPos pos, @Nullable Direction direction) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null) return LazyOptional.empty();
        return getGTEnergyHandler(be, direction);
    }

    public static LazyOptional<IEnergyHandler> getGTEnergyHandler(BlockEntity blockEntity, Direction side){
        if (blockEntity instanceof ICapabilityProvider t){
            return t.getCapability(ENERGY_HANDLER_CAPABILITY, side);
        }
        if (blockEntity instanceof EnergyTransferable t){
            return t.getGTEnergyHandler(side);
        }
        return LazyOptional.empty();
    }
}
