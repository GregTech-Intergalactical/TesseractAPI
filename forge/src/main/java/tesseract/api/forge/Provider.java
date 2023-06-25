package tesseract.api.forge;

import earth.terrarium.botarium.util.Serializable;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;

public class Provider<T> implements ICapabilityProvider, INBTSerializable {
    private final LazyOptional<T> optional;
    private final Capability<T> capability;

    public Provider(Capability<T> capability, NonNullSupplier<T> supplier) {
        this.optional = LazyOptional.of(supplier);
        this.capability = capability;
    }

    @Override
    public <C> LazyOptional<C> getCapability(Capability<C> cap, Direction side) {
        return cap == capability ? optional.cast() : LazyOptional.empty();
    }

    @Override
    public Tag serializeNBT() {
        return optional.map(t -> {
            if (t instanceof INBTSerializable it){
                return it.serializeNBT();
            } else if (t instanceof Serializable it){
                return it.serialize(new CompoundTag());
            }
            return new CompoundTag();
        }).orElse(new CompoundTag());
    }

    @Override
    public void deserializeNBT(Tag nbt) {
        optional.ifPresent(t -> {
            if (t instanceof INBTSerializable it){
                it.deserializeNBT(nbt);
            } else if (t instanceof Serializable it && nbt instanceof CompoundTag tag){
                it.deserialize(tag);
            }
        });
    }

}
