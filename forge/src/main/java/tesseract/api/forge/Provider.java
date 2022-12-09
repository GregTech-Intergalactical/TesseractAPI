package tesseract.api.forge;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;
import tesseract.api.gt.IEnergyHandlerItem;

public class Provider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    private final LazyOptional<IEnergyHandlerItem> energy;

    public Provider(NonNullSupplier<IEnergyHandlerItem> cap) {
        this.energy = LazyOptional.of(cap);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        return cap == TesseractCaps.ENERGY_HANDLER_CAPABILITY_ITEM ? energy.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return energy.map(INBTSerializable::serializeNBT).orElse(new CompoundTag());
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        energy.ifPresent(t -> t.deserializeNBT(nbt));
    }

}
