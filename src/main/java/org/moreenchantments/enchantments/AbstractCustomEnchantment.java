package org.moreenchantments.enchantments;

import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.Listener;
import org.moreenchantments.Main;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractCustomEnchantment implements Listener {

    public static Set<Class<? extends AbstractCustomEnchantment>> customEnchantments = new HashSet<>();
    public static void registerCustomEnchantment(Class<? extends AbstractCustomEnchantment> enchantmentClass) {
        customEnchantments.add(enchantmentClass);
    }

    public String name;
    public Enchantment[] notCompatibleVanilla;
    public String[] notCompatibleCustom;
    public String[] requiredPlugins;
    public String[] requiredConfigs;

    public Main main = Main.getThis();

    public AbstractCustomEnchantment(String name, String[] notCompatibleCustom, Enchantment[] notCompatibleVanilla, String[] requiredPlugins, String[] requiredConfigs) {

        this.name = name;
        this.notCompatibleCustom = notCompatibleCustom;
        this.notCompatibleVanilla = notCompatibleVanilla;
        this.requiredPlugins = requiredPlugins;
        this.requiredConfigs = requiredConfigs;
    }
}
