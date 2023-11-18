package org.moreenchantments.enchantments;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.itemutils.ItemUtils;
import org.moreenchantments.MoreEnchantments;
import org.moreenchantments.books.MoneyMendingBook;


import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class MoneyMendingEnchantment {
    public static String name = "money_mending";
    public Enchantment[] notCompatibleVanilla = {Enchantment.MENDING, Enchantment.ARROW_INFINITE};
    public String[] notCompatibleCustom = {""};
    public String[] requiredPlugins = {"Essentials"};
    public String[] requiredConfigs = {"costPerDurability"};
    public MoneyMendingBook book = new MoneyMendingBook();

    public MoreEnchantments main = MoreEnchantments.getThis();

    public void PlayerItemDamageEvent(PlayerItemDamageEvent event){
        Essentials ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");

        Player player = event.getPlayer();
        User essPlayer = ess.getUser(player);
        ItemStack equipment = event.getItem();

        if (ItemUtils.itemGetNbtPath(equipment,"CustomEnchantments") == null){
            return;
        }

        if (!(((ArrayList)(ItemUtils.itemGetNbtPath(equipment,"CustomEnchantments"))).contains("moreenchantments:money_mending"))){
            return;
        }

        Damageable damagedEquipment = (Damageable) equipment.getItemMeta();
        BigDecimal damage = BigDecimal.valueOf(damagedEquipment.getDamage()+event.getDamage());

        BigDecimal playerBalance = essPlayer.getMoney();
        if (playerBalance.compareTo(damage) < 0){
            return;
        }

        damagedEquipment.setDamage(0);
        equipment.setItemMeta(damagedEquipment);
        player.getEquipment().setItemInMainHand(equipment);

        try{
            essPlayer.setMoney(playerBalance.subtract(damage));
        }
        catch (Exception ignored){}
    }

    public void EntitySpawnEvent(EntitySpawnEvent event){
        if (!event.getEntityType().equals(EntityType.WANDERING_TRADER)){
            return;
        }

        WanderingTrader wanderingLeash = ((WanderingTrader)(event.getEntity()));

        MerchantRecipe merchant = new MerchantRecipe(MoneyMendingBook.item(),1);
        merchant.addIngredient(new ItemStack(Material.BOOK,1));
        merchant.addIngredient(new ItemStack(Material.EMERALD_BLOCK, (int) (27+Math.round(Math.random()*10))));

        wanderingLeash.setRecipe(wanderingLeash.getRecipes().size()-1,merchant);
    }

    public void PrepareAnvilEvent(PrepareAnvilEvent event){
        System.out.println(event.getInventory().getItem(0));
        if (event.getInventory().getItem(0) == null){
            return;
        }
        if (event.getInventory().getItem(1) != null){
            return;
        }
        if (event.getInventory().getItem(0).equals(Material.ENCHANTED_BOOK))
        if (!(((EnchantmentStorageMeta)(event.getInventory().getItem(0).getItemMeta())).hasStoredEnchant(Enchantment.MENDING))){
            return;
        }
//        event.setResult(event.getInventory().getItem(0));
        event.setResult(MoneyMendingBook.item());
        event.getInventory().setRepairCost(301);
        event.getInventory().setMaximumRepairCost(300);
        ItemMeta itemMeta = event.getResult().getItemMeta();
        itemMeta.setDisplayName("\u00a7e[\u00a7fClick\u00a7e] \u00a7cBuy the book with price 500$!");
        event.getResult().setItemMeta(itemMeta);
        ItemStack result = ItemUtils.itemSetNbtPath(event.getResult(), "money_mending_trade", true);
        event.setResult(result);
    }

    public void InventoryClickEvent(InventoryClickEvent event){
        ItemStack currentItem = event.getCurrentItem();
        HumanEntity player = event.getWhoClicked();

        if (ItemUtils.itemGetNbtPath(currentItem, "money_mending_trade") == null){
            return;
        }

        Essentials ess = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
        User essPlayer = ess.getUser(player);

        ItemStack returnItem;
        if (essPlayer.getMoney().compareTo(BigDecimal.valueOf(500))>=0){
            returnItem = MoneyMendingBook.item();
            player.sendMessage("\u00a7f[\u00a7aMoreEnchantments\u00a7f] \u00a7eYou buy a MoneyMending enchanted book with \u00a7c500\u00a7f$");
            player.getLocation().getWorld().playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
            try{
                essPlayer.setMoney(essPlayer.getMoney().subtract(BigDecimal.valueOf(500)));
            }
            catch (Exception ignored){

            }
        }
        else{
            returnItem = event.getInventory().getItem(0);
            player.sendMessage("\u00a7f[\u00a7aMoreEnchantments\u00a7f] \u00a7eYour money does not enough to buy it.");
            player.getLocation().getWorld().playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1, 0);
        }

        player.setItemOnCursor(returnItem);
        event.getInventory().clear();

    }
}
