package net.jurassicbeast.reusablevaultblocks;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Uuids;
import net.minecraft.util.dynamic.Codecs;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class VaultBlockCooldownManager {
    public int cooldownLength;
    private List<Pair<UUID, Long>> playerCooldowns;

    public VaultBlockCooldownManager() {
        this.playerCooldowns = new ArrayList<>();
    }

    public VaultBlockCooldownManager(int cooldownLength, List<Pair<UUID, Long>> playerCooldowns) {
        this.cooldownLength = cooldownLength;
        this.playerCooldowns = new ArrayList<>(playerCooldowns);
    }

    private static final Codec<Pair<UUID, Long>> PAIR_CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                    Uuids.INT_STREAM_CODEC.fieldOf("uuid").forGetter(Pair::getFirst),
                    Codec.LONG.fieldOf("cooldown_ends_at").forGetter(Pair::getSecond)
            ).apply(instance, Pair::of)
    );

    public static final Codec<VaultBlockCooldownManager> codec = RecordCodecBuilder.create(
            instance -> instance.group(
                    Codecs.NONNEGATIVE_INT.fieldOf("cooldown_length").forGetter(VaultBlockCooldownManager::getCooldownLength),
                    PAIR_CODEC.listOf().fieldOf("player_cooldowns").forGetter(VaultBlockCooldownManager::getPlayerCooldowns)
            ).apply(instance, VaultBlockCooldownManager::new)
    );

    public void addPlayer(UUID player, long cooldownEnd) {
        this.playerCooldowns.add(new Pair<>(player, cooldownEnd));
    }

    public void removePlayer(UUID player) {
        this.playerCooldowns.removeIf(pair -> pair.getFirst().equals(player));
    }

    public void setPlayerCooldownEnd(UUID player, long newCooldownEnd) {
        for (int i = 0; i < playerCooldowns.size(); i++) {
            Pair<UUID, Long> pair = playerCooldowns.get(i);
            if (pair.getFirst().equals(player)) {
                playerCooldowns.set(i, new Pair<>(player, newCooldownEnd));
                return;
            }
        }
    }

    public int getSizeOfPlayerCooldowns() {
        return this.playerCooldowns.size();
    }

    public boolean containsPlayer(UUID player) {
        return this.playerCooldowns.stream().anyMatch(pair -> pair.getFirst().equals(player));
    }

    public List<Pair<UUID, Long>> getPlayerCooldowns() {
        return this.playerCooldowns;
    }

    public int getCooldownLength() {
        return this.cooldownLength;
    }

    public void setCooldownLength(int newCooldownLength) {
        this.cooldownLength = newCooldownLength;
    }

    public void copyFrom(VaultBlockCooldownManager data) {
        this.playerCooldowns = new ArrayList<>(data.playerCooldowns);
        this.cooldownLength = data.getCooldownLength();
    }
}