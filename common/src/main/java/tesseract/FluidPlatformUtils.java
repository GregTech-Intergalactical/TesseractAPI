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

    public static long moveStandardToItemFluid(FluidContainer from, PlatformFluidItemHandler to, ItemStackHolder receiver, FluidHolder fluid) {
        FluidHolder extracted = from.extractFluid(fluid, true);
        long inserted = to.insertFluid(receiver.copy(), extracted, true);
        from.extractFluid(FluidHooks.newFluidHolder(fluid.getFluid(), inserted, fluid.getCompound()), false);
        return to.insertFluid(receiver, extracted, false);
    }

    public static long moveStandardToItemFluid(FluidContainer from, PlatformFluidItemHandler to, ItemStackHolder receiver, long maxInsert, boolean simulate) {
        for (int i = 0; i < from.getSize(); i++) {
            FluidHolder fluid = from.getFluids().get(i);
            FluidHolder extracted = from.extractFluid(fluid, true);
            if (!extracted.isEmpty()){
                long inserted = to.insertFluid(receiver.copy(), extracted, true);
                if (inserted > 0){
                    FluidHolder fluidHolder = FluidHooks.newFluidHolder(fluid.getFluid(), Math.min(maxInsert, inserted), fluid.getCompound());
                    from.extractFluid(fluidHolder, simulate);
                    return to.insertFluid(receiver, fluidHolder, simulate);
                }

            }
        }
        return 0;
    }

    public static long moveItemToStandardFluid(PlatformFluidItemHandler from, FluidContainer to, ItemStackHolder receiver, long maxExtract, boolean simulate) {
        for (int i = 0; i < from.getTankAmount(); i++) {
            FluidHolder fluid = from.getFluidInTank(i);
            FluidHolder extracted = from.extractFluid(receiver.copy(), fluid, true);
            if (!extracted.isEmpty()){
                long inserted = to.insertFluid(extracted, true);
                if (inserted > 0){
                    FluidHolder fluidHolder = FluidHooks.newFluidHolder(fluid.getFluid(), Math.min(maxExtract, inserted), fluid.getCompound());
                    from.extractFluid(receiver, fluidHolder, simulate);
                    return to.insertFluid(fluidHolder, simulate);
                }

            }
        }
        return 0;
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
