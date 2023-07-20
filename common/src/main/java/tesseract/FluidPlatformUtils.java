package tesseract;

import dev.architectury.injectables.annotations.ExpectPlatform;
import earth.terrarium.botarium.common.fluid.base.FluidContainer;
import earth.terrarium.botarium.common.fluid.base.FluidHolder;
import earth.terrarium.botarium.common.fluid.base.PlatformFluidHandler;
import earth.terrarium.botarium.common.fluid.base.PlatformFluidItemHandler;
import earth.terrarium.botarium.common.fluid.utils.FluidHooks;
import earth.terrarium.botarium.common.item.ItemStackHolder;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class FluidPlatformUtils {
    public static FluidHolder createFluidStack(Fluid fluid, long amount){
        return FluidHooks.newFluidHolder(fluid,amount, null);
    }

    @ExpectPlatform
    public static ResourceLocation getStillTexture(Fluid fluid){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static ResourceLocation getFlowingTexture(Fluid fluid){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static ResourceLocation getFluidId(Fluid fluid){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static int getFluidTemperature(Fluid fluid){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isFluidGaseous(Fluid fluid){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static int getFluidColor(Fluid fluid){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static SoundEvent getFluidSound(Fluid fluid, boolean fill){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Component getFluidDisplayName(FluidHolder fluid){
        throw new AssertionError();
    }

    /*@ExpectPlatform
    public static FluidStack tryFluidTransfer(IFluidHandler fluidDestination, IFluidHandler fluidSource, long maxAmount, boolean doTransfer){
        throw new AssertionError();
    }*/

    public static boolean fillItemFromContainer(ItemStack stack, PlatformFluidHandler handler, Consumer<ItemStack> consumer){
        return fillItemFromContainer(stack, handler, s -> true, consumer);
    }

    public static boolean emptyItemintoContainer(ItemStack stack, PlatformFluidHandler handler, Consumer<ItemStack> consumer){
        return emptyItemintoContainer(stack, handler, s -> true, consumer);
    }

    public static boolean fillItemFromContainer(ItemStack stack, PlatformFluidHandler handler, Predicate<ItemStack> tester, Consumer<ItemStack> consumer){
        PlatformFluidItemHandler itemHandler = FluidHooks.safeGetItemFluidManager(stack).orElse(null);
        if (itemHandler == null) return false;
        for (int i = 0; i < handler.getTankAmount(); i++) {
            FluidHolder fluid = handler.getFluidInTank(i);
            FluidHolder extracted = handler.extractFluid(fluid, true);
            if (!extracted.isEmpty()){
                ItemStackHolder holder = new ItemStackHolder(stack.copy());
                long inserted = itemHandler.insertFluid(holder, extracted, false);
                if (inserted > 0 && tester.test(holder.getStack())){
                    FluidHolder fluidHolder = FluidHooks.newFluidHolder(fluid.getFluid(), inserted, fluid.getCompound());
                    handler.extractFluid(fluidHolder, false);
                    long insert = itemHandler.insertFluid(holder, fluidHolder, false);
                    consumer.accept(holder.getStack());
                    return insert > 0;
                }
            }
        }
        return false;
    }

    public static boolean emptyItemintoContainer(ItemStack stack, PlatformFluidHandler handler, Predicate<ItemStack> tester, Consumer<ItemStack> consumer){
        PlatformFluidItemHandler itemHandler = FluidHooks.safeGetItemFluidManager(stack).orElse(null);
        if (itemHandler == null) return false;
        for (int i = 0; i < itemHandler.getTankAmount(); i++) {
            FluidHolder fluid = itemHandler.getFluidInTank(i);
            ItemStackHolder holder = new ItemStackHolder(stack.copy());
            FluidHolder extracted = itemHandler.extractFluid(holder, fluid, false);
            if (!extracted.isEmpty() && tester.test(holder.getStack())){
                long inserted = handler.insertFluid(extracted, true);
                if (inserted > 0){
                    FluidHolder fluidHolder = FluidHooks.newFluidHolder(fluid.getFluid(), inserted, fluid.getCompound());
                    itemHandler.extractFluid(holder, fluidHolder, false);
                    long insert = handler.insertFluid(fluidHolder, false);
                    consumer.accept(holder.getStack());
                    return insert > 0;
                }
            }
        }
        return false;
    }

    public static void writeToPacket(FriendlyByteBuf buffer, FluidHolder holder) {
        if (holder.isEmpty()) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            buffer.writeVarInt(Registry.FLUID.getId(holder.getFluid()));
            buffer.writeVarLong(holder.getFluidAmount());
            buffer.writeNbt(holder.getCompound());
        }
    }

    public static FluidHolder readFromPacket(FriendlyByteBuf buffer) {
        if (!buffer.readBoolean()) return FluidHooks.emptyFluid();
        Fluid fluid = Registry.FLUID.byId(buffer.readVarInt());
        long amount = buffer.readVarLong();
        return FluidHolder.of(fluid, amount, buffer.readNbt());
    }
}
