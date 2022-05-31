package net.minecraftforge.mixin.crafting;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.IngredientExtension;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.stream.Stream;

@Mixin(Ingredient.class)
public class IngredientMixin implements IngredientExtension {

    @Unique
    private int invalidationCounter;
    @Mutable
    @Unique
    @Final
    private boolean isSimple;

    @Shadow
    @Nullable
    private ItemStack[] itemStacks;

    @Shadow
    @Nullable
    private IntList stackingIds;

    @Shadow
    @Final
    public static Ingredient EMPTY;

    @Shadow
    @Final
    private Ingredient.Value[] values;

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private void injectToInit(Stream stream, CallbackInfo ci){
        this.isSimple = !net.minecraftforge.data.loading.DatagenModLoader.isRunningDataGen() && !Arrays.stream(values).anyMatch(list -> list.getItems().stream().anyMatch(stack -> stack.getItem().isDamageable(stack)));
    }

    @Inject(method = "toNetwork", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/crafting/Ingredient;dissolve()V", shift = At.Shift.AFTER), cancellable = true)
    private void injectToNetwork(FriendlyByteBuf buffer, CallbackInfo ci){
        if (!this.isVanilla()){
            net.minecraftforge.common.crafting.CraftingHelper.write(buffer, (Ingredient) (Object)this);
            ci.cancel();
        }
    }

    @Override
    public boolean checkInvalidation() {
        int currentInvalidationCounter = INVALIDATION_COUNTER.get();
        if (this.invalidationCounter != currentInvalidationCounter) {
            invalidate();
            return true;
        }
        return false;
    }

    @Override
    public void markValid() {
        this.invalidationCounter = INVALIDATION_COUNTER.get();
    }

    @Override
    public void invalidate() {
        this.itemStacks = null;
        this.stackingIds = null;
    }

    @Override
    public boolean isSimple() {
        return isSimple || ((Ingredient)(Object)this) == EMPTY;
    }

    private final boolean isVanilla = this.getClass().equals(Ingredient.class);
    @Override
    public boolean isVanilla() {
        return isVanilla;
    }

    public net.minecraftforge.common.crafting.IIngredientSerializer<? extends Ingredient> getSerializer() {
        if (!isVanilla()) throw new IllegalStateException("Modders must implement Ingredient.getSerializer in their custom Ingredients: " + this);
        return net.minecraftforge.common.crafting.VanillaIngredientSerializer.INSTANCE;
    }
}
