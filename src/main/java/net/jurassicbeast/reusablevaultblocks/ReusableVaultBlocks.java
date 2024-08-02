package net.jurassicbeast.reusablevaultblocks;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.world.GameRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReusableVaultBlocks implements ModInitializer {
	public static final String MOD_ID = "reusable-vault-blocks";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static GameRules.Key<GameRules.IntRule> VAULT_BLOCK_COOLDOWN;
	public static GameRules.Key<GameRules.IntRule> OMINOUS_VAULT_BLOCK_COOLDOWN;

	@Override
	public void onInitialize() {
		LOGGER.info("Registering gamerules");
		VAULT_BLOCK_COOLDOWN = GameRuleRegistry.register("vaultBlockCooldown", GameRules.Category.MISC, GameRuleFactory.createIntRule(720_000, 1));
		OMINOUS_VAULT_BLOCK_COOLDOWN = GameRuleRegistry.register("ominousVaultBlockCooldown", GameRules.Category.MISC, GameRuleFactory.createIntRule(864_000, 1));

		LOGGER.info("Successfully loaded all parts of the mod");
	}
}