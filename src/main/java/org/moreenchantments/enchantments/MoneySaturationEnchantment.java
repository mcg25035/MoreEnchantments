package org.moreenchantments.enchantments;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.moreenchantments.ItemNBTUtils;
import org.moreenchantments.books.MoneyMendingBook;
import org.moreenchantments.books.MoneySaturationBook;
import org.moreenchantments.utils.EnchantmentUtils;
import org.moreenchantments.utils.UUIDUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MoneySaturationEnchantment extends AbstractCustomEnchantment {

    public MoneySaturationEnchantment() {
        super(
                "money_saturation",
                new String[]{""},
                new Enchantment[]{},
                new String[]{"Essentials"},
                new String[]{"costPerFood"}
        );
        Bukkit.getPluginManager().registerEvents(this, main);
    }

    @EventHandler
    public void FoodLevelChangeEvent(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        Bukkit.getScheduler().runTaskLater(main, () -> {
            PlayerInventory inventory = player.getInventory();
            ItemStack[] armorContents = inventory.getArmorContents();

            boolean hasEnchant = false;
            for (ItemStack armor : armorContents) {
                if (armor != null && ItemNBTUtils.containsCustomEnchantment(armor, "moreenchantments:money_saturation")) {
                    hasEnchant = true;
                    break;
                }
            }

            if (!hasEnchant) return;

            int foodLevel = player.getFoodLevel();
            if (foodLevel >= 20) return;

            int foodDifference = 20 - foodLevel;
            double costPerFood;
            try{
                costPerFood = (Double)(main.config.get("costPerFood"));
            }
            catch (Exception ignored){
                costPerFood = (Integer)(main.config.get("costPerFood"));
            }
            double cost = foodDifference * costPerFood;

            Essentials essentials = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");
            if (essentials == null) return;

            User user = essentials.getUser(player);
            if (user == null) return;

            BigDecimal playerBalance = user.getMoney();
            if (playerBalance.compareTo(BigDecimal.valueOf(cost)) < 0) return;

            try{
                user.setMoney(playerBalance.subtract(BigDecimal.valueOf(cost)));
            }
            catch (Exception ignored){}
            player.setFoodLevel(20);
            player.getLocation().getWorld().playSound(player, Sound.ENTITY_PLAYER_LEVELUP, 1, 0);
        }, 0);
    }

    @EventHandler
    public void EntitySpawnEvent(EntitySpawnEvent event){
        if (!event.getEntityType().equals(EntityType.WANDERING_TRADER)) return;

        WanderingTrader wanderingLeash = ((WanderingTrader)(event.getEntity()));

        MerchantRecipe merchant = new MerchantRecipe(MoneySaturationBook.item(),1);
        merchant.addIngredient(new ItemStack(Material.EMERALD, (int) (27+Math.round(Math.random()*10))));
        merchant.addIngredient(new ItemStack(Material.BOOK,1));
        List<MerchantRecipe> recipes = new ArrayList<>(wanderingLeash.getRecipes());
        recipes.add(merchant);

        wanderingLeash.setRecipes(recipes);
    }

    @EventHandler
    public void PrepareItemCraftEvent(PrepareItemCraftEvent event) {
        ItemStack[] craftTable = event.getInventory().getMatrix();
        for (int i=0; i<craftTable.length; i++){
            if (i%2 == 0) continue;
            if (craftTable[i] == null) return;
            if (!craftTable[i].getType().isEdible()) return;
        }

        if (craftTable[4] == null) return;
        if (!craftTable[4].getType().equals(Material.ENCHANTED_BOOK)) return;

        event.getInventory().setResult(MoneySaturationBook.item());
    }
}
