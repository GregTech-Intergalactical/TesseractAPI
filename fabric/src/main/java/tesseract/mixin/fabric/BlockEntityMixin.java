package tesseract.mixin.fabric;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tesseract.api.fabric.TileListeners;

import java.util.List;

@Mixin(BlockEntity.class)
public class BlockEntityMixin implements TileListeners {
    @Unique
    List<Runnable> listeners = new ObjectArrayList<>();

    @Override
    public void addListener(Runnable listener) {
        listeners.add(listener);
    }

    @Inject(method = "setRemoved", at = @At("TAIL"))
    public void runListeners(CallbackInfo ci) {
        listeners.forEach(Runnable::run);
    }
}
