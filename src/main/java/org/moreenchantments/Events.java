package org.moreenchantments;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.moreenchantments.utils.EnchantmentUtils;
import org.moreenchantments.utils.ListUtils;
import org.moreenchantments.utils.UUIDUtils;

import java.util.ArrayList;

public class Events implements Listener {


    @EventHandler
    public void PrepareAnvilEvent(PrepareAnvilEvent event){
        ItemStack aSlot = event.getInventory().getItem(0);
        ItemStack bSlot = event.getInventory().getItem(1);

        if (aSlot == null || bSlot == null) return;

        ItemStack result = event.getResult();

        ArrayList<String> aCustomEnchantments = ItemNBTUtils.getCustomEnchantments(aSlot);
        ArrayList<String> bCustomEnchantments = ItemNBTUtils.getCustomEnchantments(bSlot);
        ArrayList<String> customEnchantments = EnchantmentUtils.mergeCustomEnchantments(aCustomEnchantments,bCustomEnchantments);
        if (customEnchantments == null) return;
        if (!EnchantmentUtils.isValidToEnchant(aSlot, bSlot)) return;

        customEnchantments = ListUtils.removeDuplicates(customEnchantments);

        if (result == null) result = aSlot.clone();

        if (result.getType().equals(Material.AIR)){
            result = aSlot.clone();
        }

        result = ItemNBTUtils.setCustomEnchantments(result, customEnchantments);

        int aSlotHighestRespirationLevel = EnchantmentUtils.getEnchantmentLevel(aSlot, Enchantment.OXYGEN);
        int bSlotHighestRespirationLevel = EnchantmentUtils.getEnchantmentLevel(bSlot, Enchantment.OXYGEN);
        int highestRespiration = Math.max(aSlotHighestRespirationLevel,bSlotHighestRespirationLevel);

        if (EnchantmentUtils.hasVanillaEnchantments(result)) EnchantmentUtils.removeVirtualEnchantment(result);

        EnchantmentUtils.setCustomEnchantmentLore(result, customEnchantments);

        if (highestRespiration == 0){
            ItemMeta rItemMeta = result.getItemMeta();
            assert rItemMeta != null;
            rItemMeta.removeEnchant(Enchantment.OXYGEN);
            result.setItemMeta(rItemMeta);
        }

        if (!EnchantmentUtils.hasVanillaEnchantments(result)) EnchantmentUtils.addVirtualEnchantment(result);

        result = UUIDUtils.itemUpdateUUID(result);
        event.setResult(result);

        if (!EnchantmentUtils.isEnchantmentCompatible(result, customEnchantments)) event.setResult(new ItemStack(Material.AIR));
    }

    @EventHandler
    public void InventoryClickEvent(InventoryClickEvent event){
        if (event.getClickedInventory() == null){
            return;
        }

        if (event.getCurrentItem() == null){
            return;
        }

        if (event.getClickedInventory().getType() == InventoryType.GRINDSTONE && event.getSlotType() == InventoryType.SlotType.RESULT){
            if (ItemNBTUtils.containsCustomEnchantments(event.getCurrentItem())){
                ItemStack result = ItemNBTUtils.setCustomEnchantments(event.getCurrentItem(), new ArrayList<>());
                ItemMeta resultMeta = result.getItemMeta();
                assert resultMeta != null;
                resultMeta.setLore(new ArrayList<>());
                result.setItemMeta(resultMeta);
                EnchantmentUtils.removeVirtualEnchantment(result);
                event.setCurrentItem(result);
            }
        }

        Inventory inventory = event.getInventory();
        HumanEntity player = event.getWhoClicked();
        if (!(inventory instanceof AnvilInventory)){
            return;
        }

        if (((AnvilInventory) inventory).getRepairCost() > ((Player)player).getLevel()){
            return;
        }
        if (!event.getSlotType().equals(InventoryType.SlotType.RESULT)){
            return;
        }

        if (!event.getWhoClicked().getItemOnCursor().getType().equals(Material.AIR)){
            event.setResult(Event.Result.DENY);
            return;
        }

        if (event.getCurrentItem().getType().equals(Material.AIR)){
            return;
        }

        if (inventory.getItem(1) != null){
            if (inventory.getItem(1).getAmount() > 1){
                return;
            }
        }

        if (event.isShiftClick()){
            player.getInventory().addItem(event.getCurrentItem());
        }
        else{
            player.setItemOnCursor(event.getCurrentItem());
        }
        inventory.clear();

        Location inventoryLocation = inventory.getLocation();
        assert inventoryLocation != null;
        inventoryLocation.getWorld().playSound(inventoryLocation, Sound.BLOCK_ANVIL_USE, (float) 1, (float) (1.035-Math.random()*0.15));
    }

}
