package tesseract;

import dev.architectury.injectables.annotations.ExpectPlatform;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

import javax.annotation.Nonnull;
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

    /**
     * Fill a destination fluid handler from a source fluid handler with a max amount.
     * To specify a fluid to transfer instead of max amount, use {@link #tryFluidTransfer(PlatformFluidHandler, PlatformFluidHandler, FluidHolder, boolean)}
     * To transfer as much as possible, use {@link Integer#MAX_VALUE} for maxAmount.
     *
     * @param fluidDestination The fluid handler to be filled.
     * @param fluidSource      The fluid handler to be drained.
     * @param maxAmount        The largest amount of fluid that should be transferred.
     * @param doTransfer       True if the transfer should actually be done, false if it should be simulated.
     * @return the fluidStack that was transferred from the source to the destination. null on failure.
     */
    @Nonnull
    public static FluidHolder tryFluidTransfer(PlatformFluidHandler fluidDestination, PlatformFluidHandler fluidSource, long maxAmount, boolean doTransfer) {
        for (int i = 0; i < fluidSource.getTankAmount(); i++) {
            FluidHolder fluid = fluidSource.getFluidInTank(i);
            FluidHolder transfer = tryFluidTransfer(fluidDestination, fluidSource, fluid.copyWithAmount(Math.min(fluid.getFluidAmount(), maxAmount)), doTransfer);
            if (!transfer.isEmpty()) return transfer;
        }
        return FluidHooks.emptyFluid();
    }

    /**
     * Fill a destination fluid handler from a source fluid handler using a specific fluid.
     * To specify a max amount to transfer instead of specific fluid, use {@link #tryFluidTransfer(PlatformFluidHandler, PlatformFluidHandler, long, boolean)}
     * To transfer as much as possible, use {@link Integer#MAX_VALUE} for resource.amount.
     *
     * @param fluidDestination The fluid handler to be filled.
     * @param fluidSource      The fluid handler to be drained.
     * @param resource         The fluid that should be transferred. Amount represents the maximum amount to transfer.
     * @param doTransfer       True if the transfer should actually be done, false if it should be simulated.
     * @return the FluidHolder that was transferred from the source to the destination. null on failure.
     */
    @Nonnull
    public static FluidHolder tryFluidTransfer(PlatformFluidHandler fluidDestination, PlatformFluidHandler fluidSource, FluidHolder resource, boolean doTransfer)
    {
        FluidHolder drainable = fluidSource.extractFluid(resource, true);
        if (!drainable.isEmpty() && resource.matches(drainable))
        {
            return tryFluidTransfer_Internal(fluidDestination, fluidSource, drainable, doTransfer);
        }
        return FluidHooks.emptyFluid();
    }

    /**
     * Internal method for filling a destination fluid handler from a source fluid handler using a specific fluid.
     * Assumes that "drainable" can be drained from "fluidSource".
     *
     * Modders: Instead of this method, use {@link #tryFluidTransfer(PlatformFluidHandler, PlatformFluidHandler, FluidHolder, boolean)}
     * or {@link #tryFluidTransfer(PlatformFluidHandler, PlatformFluidHandler, long, boolean)}.
     */
    @Nonnull
    private static FluidHolder tryFluidTransfer_Internal(PlatformFluidHandler fluidDestination, PlatformFluidHandler fluidSource, FluidHolder drainable, boolean doTransfer)
    {
        long fillableAmount = fluidDestination.insertFluid(drainable, true);
        if (fillableAmount > 0)
        {
            drainable.setAmount(fillableAmount);
            if (doTransfer)
            {
                FluidHolder drained = fluidSource.extractFluid(drainable, false);
                if (!drained.isEmpty())
                {
                    drained.setAmount(fluidDestination.insertFluid(drained, false));
                    return drained;
                }
            }
            else
            {
                return drainable;
            }
        }
        return FluidHooks.emptyFluid();
    }

    public static boolean fillItemFromContainer(ItemStack stack, PlatformFluidHandler handler, Consumer<ItemStack> consumer){
        return fillItemFromContainer(stack, handler, s -> true, consumer);
    }

    public static boolean emptyItemIntoContainer(ItemStack stack, PlatformFluidHandler handler, Consumer<ItemStack> consumer){
        return emptyItemIntoContainer(stack, handler, s -> true, consumer);
    }

    public static boolean fillItemFromContainer(ItemStack stack, PlatformFluidHandler handler, Predicate<ItemStack> tester, Consumer<ItemStack> consumer){
        PlatformFluidItemHandler itemHandler = FluidHooks.safeGetItemFluidManager(stack.copy()).orElse(null);
        if (itemHandler == null) return false;
        for (int i = 0; i < handler.getTankAmount(); i++) {
            FluidHolder fluid = handler.getFluidInTank(i);
            FluidHolder extracted = handler.extractFluid(fluid, true);
            if (!extracted.isEmpty()){
                ItemStackHolder holder = new ItemStackHolder(stack);
                long inserted = itemHandler.insertFluid(holder, extracted, false);
                if (inserted > 0 && tester.test(holder.getStack())){
                    itemHandler = FluidHooks.getItemFluidManager(stack);
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

    public static boolean emptyItemIntoContainer(ItemStack stack, PlatformFluidHandler handler, Predicate<ItemStack> tester, Consumer<ItemStack> consumer){
        PlatformFluidItemHandler itemHandler = FluidHooks.safeGetItemFluidManager(stack.copy()).orElse(null);
        if (itemHandler == null) return false;
        for (int i = 0; i < itemHandler.getTankAmount(); i++) {
            FluidHolder fluid = itemHandler.getFluidInTank(i);
            ItemStackHolder holder = new ItemStackHolder(stack);
            FluidHolder extracted = itemHandler.extractFluid(holder, fluid, false);
            if (!extracted.isEmpty() && tester.test(holder.getStack())){
                long inserted = handler.insertFluid(extracted, true);
                if (inserted > 0){
                    itemHandler = FluidHooks.getItemFluidManager(stack);
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
        return FluidHooks.newFluidHolder(fluid, amount, buffer.readNbt());
    }
}
