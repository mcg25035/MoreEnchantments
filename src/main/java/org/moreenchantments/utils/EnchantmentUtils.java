package org.moreenchantments.utils;

import org.bukkit.Bukkit;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;
import org.moreenchantments.ItemNBTUtils;
import org.moreenchantments.Main;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EnchantmentUtils {

    public static boolean hasCustomEnchantment(ItemStack item, Class<?> enchantment){
        boolean result = false;
        try{
            String enchName = "moreenchantments:"+enchantment.getDeclaredField("name").get(String.class);
            ArrayList<String> enchantments = ItemNBTUtils.getCustomEnchantments(item);
            enchantments.contains(enchName);
        }
        catch (Exception ignored){
            ignored.printStackTrace();
        }
        return result;
    }

    public static boolean isEnchantmentBook(ItemStack item){
        return item.getType().equals(Material.ENCHANTED_BOOK);
    }

    public static boolean enchantmentBookContains(ItemStack item, Enchantment enchantment){
        return ((EnchantmentStorageMeta)(item.getItemMeta())).hasStoredEnchant(enchantment);
    }

    public static void removeVirtualEnchantment(ItemStack item){
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        if (meta.hasItemFlag(ItemFlag.HIDE_ENCHANTS)) meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);

        if (item.getType() == Material.TRIDENT) item.removeEnchantment(Enchantment.INFINITY);
        else item.removeEnchantment(Enchantment.LOYALTY);
    }

    public static void addVirtualEnchantment(ItemStack item){
        if (item.getType() == Material.TRIDENT) item.addUnsafeEnchantment(Enchantment.INFINITY, 1);
        else item.addUnsafeEnchantment(Enchantment.LOYALTY, 1);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(meta);
    }

    public static boolean hasVanillaEnchantments(ItemStack item){
        ItemStack checkItem = item.clone();
        removeVirtualEnchantment(checkItem);
        Map<Enchantment, Integer> enchantments = checkItem.getEnchantments();
        return !enchantments.isEmpty();
    }

    public static ArrayList<String> mergeCustomEnchantments(ArrayList<String> ce1, ArrayList<String> ce2){
        if (ce1 == null) ce1 = new ArrayList<>();
        if (ce2 == null) ce2 = new ArrayList<>();
        ArrayList<String> result = new ArrayList<>();
        result.addAll(ce1);
        result.addAll(ce2);
        if (result.isEmpty()) return null;
        return result;
    }

    public static Integer getEnchantmentLevel(@Nonnull ItemStack item, @Nonnull Enchantment enchantment) {
        if (!isEnchantmentBook(item)) {
            return item.containsEnchantment(enchantment) ? item.getEnchantments().get(enchantment) : 0;
        }

        return enchantmentBookContains(item, enchantment) ?
                ((EnchantmentStorageMeta) (item.getItemMeta())).getStoredEnchantLevel(enchantment) : 0;
    }

    public static boolean isEnchantmentCompatible(ItemStack result, ArrayList<String> customEnchantments) {
        if (customEnchantments == null || customEnchantments.isEmpty()) {
            return true;
        }

        Main main = Main.getThis();

        try {
            for (String i : customEnchantments) {
                Enchantment[] notCompatibleVanilla = main.constructedEnchantments.get(i).notCompatibleVanilla;
                if (notCompatibleVanilla != null) {
                    for (Enchantment ii : notCompatibleVanilla) {
                        boolean containEnchantedBook;
                        if (EnchantmentUtils.isEnchantmentBook(result)) {
                            containEnchantedBook = EnchantmentUtils.enchantmentBookContains(result, ii);
                        } else {
                            containEnchantedBook = result.containsEnchantment(ii);
                        }

                        if (containEnchantedBook) {
                            return false;
                        }
                    }
                }

                String[] notCompatibleCustom = main.constructedEnchantments.get(i).notCompatibleCustom;
                if (notCompatibleCustom != null) {
                    for (String ii : notCompatibleCustom) {
                        if (customEnchantments.contains(ii)) {
                            return false;
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }

        return true;
    }

    public static void setCustomEnchantmentLore(ItemStack item, ArrayList<String> customEnchantments) {
        if (customEnchantments == null || customEnchantments.isEmpty()) return;

        Main main = Main.getThis();
        List<String> lores = new ArrayList<>();
        for (String i : customEnchantments){
            lores.add("§7"+main.languageMapping.get(i));
        }
        ItemMeta itemMeta = item.getItemMeta();
        assert itemMeta != null;
        itemMeta.setLore(lores);
        item.setItemMeta(itemMeta);
    }

    /**
     * means that is a tools or armor or something can be enchanted by all vanilla enchantments
     */
    public static boolean isEnchantable(Material itemType) {
        return Enchantment.MENDING.canEnchantItem(new ItemStack(itemType, 1));
    }

    public static boolean isValidToEnchant(ItemStack aSlot, ItemStack bSlot) {
        if (aSlot.getType().equals(Material.ENCHANTED_BOOK) && bSlot.getType().equals(Material.ENCHANTED_BOOK)){
            return true;
        }

        if (EnchantmentUtils.isEnchantable(aSlot.getType()) && bSlot.getType().equals(Material.ENCHANTED_BOOK)){
            return true;
        }

        if (aSlot.getType().equals(bSlot.getType()) && EnchantmentUtils.isEnchantable(aSlot.getType())){
            return true;
        }

        return false;
    }
}
