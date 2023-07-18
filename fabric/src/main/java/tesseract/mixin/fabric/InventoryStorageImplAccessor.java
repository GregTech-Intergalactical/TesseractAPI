package tesseract.mixin.fabric;

import net.fabricmc.fabric.impl.transfer.item.InventoryStorageImpl;
import net.minecraft.world.Container;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(InventoryStorageImpl.class)
public interface InventoryStorageImplAccessor {
    @Accessor
    Container getInventory();
}
