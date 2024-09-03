package net.jurassicbeast.reusablevaultblocks.mixin;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import net.jurassicbeast.reusablevaultblocks.CooldownManagerProvider;
import net.jurassicbeast.reusablevaultblocks.ReusableVaultBlocks;
import net.jurassicbeast.reusablevaultblocks.VaultBlockCooldownManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.VaultBlockEntity;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(VaultBlockEntity.class)
public abstract class VaultBlockEntityMixin implements CooldownManagerProvider {

    @Shadow
    private static <T> NbtElement encodeValue(Codec<T> codec, T value, RegistryWrapper.WrapperLookup registries) {
        return null;
    }

    @Shadow
    private static final Logger LOGGER = LogUtils.getLogger();

    @Unique
    private VaultBlockCooldownManager cooldownManager;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void injectCooldownManager(BlockPos pos, BlockState state, CallbackInfo ci) {
        this.cooldownManager = new VaultBlockCooldownManager();
    }

    @Inject(method = "writeNbt", at = @At("TAIL"))
    private void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup, CallbackInfo info) {
        nbt.put("cooldown_manager", encodeValue(VaultBlockCooldownManager.codec, this.cooldownManager, registryLookup));
    }

    @Inject(method = "readNbt", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup, CallbackInfo info, DynamicOps<NbtElement> dynamicOps) {
        if (nbt.contains("cooldown_manager")) {
            VaultBlockCooldownManager.codec.parse(dynamicOps, nbt.get("cooldown_manager")).resultOrPartial(LOGGER::error).ifPresent(this.cooldownManager::copyFrom);
        }
    }

    @Override
    public VaultBlockCooldownManager getCooldownManager() {
        return this.cooldownManager;
    }
}
