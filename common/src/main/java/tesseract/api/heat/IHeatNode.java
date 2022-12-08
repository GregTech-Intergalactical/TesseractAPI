package tesseract.api.heat;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import tesseract.TesseractCapUtils;
import tesseract.api.GraphWrapper;
import tesseract.api.TesseractCaps;

public interface IHeatNode {
    HeatTransaction extract();

    void insert(HeatTransaction transaction);

    int getHeat();
    int getHeatCap();
    void update(boolean active);

    default int getTemperature() {
        return getHeat() / 100;
    }

    GraphWrapper.ICapabilityGetter<IHeatNode> GETTER = ((level, pos, capSide, invalidate) -> {
        BlockEntity tile = level.getBlockEntity(BlockPos.of(pos));
        if (tile == null) return null;
        LazyOptional<IHeatHandler> capability = TesseractCapUtils.getHeatHandler(tile, capSide).map(h -> LazyOptional.of(() -> h)).orElse(LazyOptional.empty());
        if (capability.isPresent()) {
            if (invalidate != null) capability.addListener(t -> invalidate.run());
            return capability.resolve().get();
        }
        return null;
    });
}
