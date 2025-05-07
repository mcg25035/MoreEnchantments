package org.moreenchantments.enchantments;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.moreenchantments.ItemNBTUtils;
import org.moreenchantments.Main;

public class ShrinkingEnchantment extends AbstractCustomEnchantment {

    private BukkitTask task;
    private double scaleFactor = 0.4; // Adjust the scale factor as needed

    public ShrinkingEnchantment() {
        super(
                "shrinking",
                new String[]{"moreenchantments:enlarging"},
                new org.bukkit.enchantments.Enchantment[]{},
                new String[]{},
                new String[]{"shrinking_scale"}
        );
        scaleFactor = (Double)(main.config.get("shrinking_scale"));
        Plugin plugin = Main.getThis();
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                handlePlayerShrinking(player);
            }
        }, 0L, 1L);
    }

    private void handlePlayerShrinking(Player player) {
        PlayerInventory inventory = player.getInventory();
        ItemStack[] armorContents = inventory.getArmorContents();

        boolean hasShrinking = false;
        for (ItemStack armor : armorContents) {
            if (armor != null && ItemNBTUtils.containsCustomEnchantment(armor, "moreenchantments:shrinking")) {
                hasShrinking = true;
                break;
            }
        }

        AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_SCALE);
        if (attribute == null) return;

        if (hasShrinking) {
            setPlayerScale(player, scaleFactor);
        } else {
            resetPlayerScale(player);
        }
    }

    private void setPlayerScale(Player player, double scale) {
        AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_SCALE);
        if (attribute == null) return;

        if (attribute.getBaseValue() == scale) return;

        attribute.setBaseValue(scale);
    }

    private void resetPlayerScale(Player player) {
        AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_SCALE);
        if (attribute == null) return;

        if (attribute.getBaseValue() != scaleFactor) return;

        attribute.setBaseValue(1.0);
    }
}
