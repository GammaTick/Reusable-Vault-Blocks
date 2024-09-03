package net.jurassicbeast.reusablevaultblocks.mixin;

import com.mojang.datafixers.util.Pair;
import net.jurassicbeast.reusablevaultblocks.CooldownManagerProvider;
import net.jurassicbeast.reusablevaultblocks.ReusableVaultBlocks;
import net.jurassicbeast.reusablevaultblocks.RewardedPlayersProvider;
import net.jurassicbeast.reusablevaultblocks.VaultBlockCooldownManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.VaultBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.VaultBlockEntity;
import net.minecraft.block.vault.VaultConfig;
import net.minecraft.block.vault.VaultServerData;
import net.minecraft.block.vault.VaultSharedData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Mixin(VaultBlockEntity.Server.class)
public class VaultBlockServerMixin {

    @Inject(method = "tryUnlock", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/block/vault/VaultServerData;markPlayerAsRewarded(Lnet/minecraft/entity/player/PlayerEntity;)V", shift = At.Shift.AFTER))
    private static void tryUnlock(ServerWorld world, BlockPos pos, BlockState state, VaultConfig config, VaultServerData serverData, VaultSharedData sharedData, PlayerEntity player, ItemStack stack, CallbackInfo info) {
        BlockEntity blockEntity = world.getBlockEntity(pos);

        if (!(blockEntity instanceof CooldownManagerProvider)) {
            return;
        }

        VaultBlockCooldownManager cooldownManager = ((CooldownManagerProvider) blockEntity).getCooldownManager();

        int vaultBlockCooldown = state.get(VaultBlock.OMINOUS)
                ? world.getGameRules().getInt(ReusableVaultBlocks.OMINOUS_VAULT_BLOCK_COOLDOWN)
                : world.getGameRules().getInt(ReusableVaultBlocks.VAULT_BLOCK_COOLDOWN);

        cooldownManager.addPlayer(player.getUuid(), world.getTime() + vaultBlockCooldown);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private static void tick(ServerWorld world, BlockPos pos, BlockState state, VaultConfig config, VaultServerData serverData, VaultSharedData sharedData, CallbackInfo info) {
        if (world.isClient()) {
            return;
        }

        BlockEntity blockEntity = world.getBlockEntity(pos);

        if (blockEntity == null) {
            return;
        }

        VaultBlockCooldownManager cooldownManager = ((CooldownManagerProvider) blockEntity).getCooldownManager();

        int vaultBlockCooldown = state.get(VaultBlock.OMINOUS)
                ? world.getGameRules().getInt(ReusableVaultBlocks.OMINOUS_VAULT_BLOCK_COOLDOWN)
                : world.getGameRules().getInt(ReusableVaultBlocks.VAULT_BLOCK_COOLDOWN);

        boolean cooldownChanged = cooldownManager.getCooldownLength() != vaultBlockCooldown;

        if (cooldownChanged) {
            cooldownManager.setCooldownLength(vaultBlockCooldown);
        }

        List<Pair<UUID, Long>> playerCooldowns = cooldownManager.getPlayerCooldowns();
        RewardedPlayersProvider rewardedPlayersProvider = (RewardedPlayersProvider) serverData;

        if (playerCooldowns.isEmpty() && rewardedPlayersProvider.isEmpty()) {
            return;
        }

        long worldTime = world.getTime();

        if (cooldownManager.getSizeOfPlayerCooldowns() != rewardedPlayersProvider.rewardedPlayersSize()) {
            for (UUID rewardedPlayer : rewardedPlayersProvider.getRewardedPlayers()) {
                if (!cooldownManager.containsPlayer(rewardedPlayer)) {
                    cooldownManager.addPlayer(rewardedPlayer, worldTime + vaultBlockCooldown);
                }
            }
        }

        List<UUID> playersToRemove = new ArrayList<>();

        for (Pair<UUID, Long> playerCooldown : playerCooldowns) {
            UUID playerUUID = playerCooldown.getFirst();
            Long cooldownEnd = playerCooldown.getSecond();

            if (cooldownChanged) {
                cooldownManager.setPlayerCooldownEnd(playerUUID, worldTime + vaultBlockCooldown);
                continue;
            }

            if (cooldownEnd <= worldTime) {
                playersToRemove.add(playerUUID);
            }
        }

        for (UUID playerToRemove : playersToRemove) {
            rewardedPlayersProvider.removeRewardedPlayer(playerToRemove);
            cooldownManager.removePlayer(playerToRemove);
        }
    }
}
