package net.jurassicbeast.reusablevaultblocks.mixin;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.jurassicbeast.reusablevaultblocks.RewardedPlayersProvider;
import net.minecraft.block.vault.VaultServerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Set;
import java.util.UUID;

@Mixin(VaultServerData.class)
public class VaultServerDataMixin implements RewardedPlayersProvider {

    @Shadow
    private final Set<UUID> rewardedPlayers = new ObjectLinkedOpenHashSet<>();

    @Override
    public Set<UUID> getRewardedPlayers() {
        return rewardedPlayers;
    }

    @Override
    public void removeRewardedPlayer(UUID player) {
        rewardedPlayers.remove(player);
    }

    @Override
    public int rewardedPlayersSize() {
        return this.rewardedPlayers.size();
    }

    @Override
    public boolean isEmpty() {
        return this.rewardedPlayers.isEmpty();
    }
}
