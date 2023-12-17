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
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.itemutils.ItemUtils;
import org.moreenchantments.utils.ListUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Events implements Listener {
    MoreEnchantments main = MoreEnchantments.getThis();
    public void eventPass(String eventName, Object ... args){
        for (String i : main.eventsFunctionMapping.keySet()){
            if (i.equals(eventName)){
                for (Consumer<Object[]> ii : main.eventsFunctionMapping.get(i)){
                    ii.accept(args);
                }
            }
        }
    }

    @EventHandler
    public void PlayerItemDamageEvent(PlayerItemDamageEvent event){
        String eventName = new Object(){}.getClass().getEnclosingMethod().getName();
        eventPass(eventName, event);
    }

    @EventHandler
    public void EntitySpawnEvent(EntitySpawnEvent event){
        String eventName = new Object(){}.getClass().getEnclosingMethod().getName();
        eventPass(eventName, event);
    }

    @EventHandler
    public void PrepareAnvilEvent(PrepareAnvilEvent event){
        String eventName = new Object(){}.getClass().getEnclosingMethod().getName();
        eventPass(eventName, event);

        ItemStack aSlot = event.getInventory().getItem(0);
        ItemStack bSlot = event.getInventory().getItem(1);

        if (aSlot == null || bSlot == null){
            return;
        }

        // Start - Emergency Fix - TRIDENT_WITH_MONEYMENDING
//        if (aSlot.getType().equals(Material.TRIDENT)){
//            return;
//        }
        // End - Emergency Fix - TRIDENT_WITH_MONEYMENDING

        ItemStack result = event.getResult();

        ArrayList aCustomEnchantments = (ArrayList) (ItemUtils.itemGetNbtPath(aSlot, "CustomEnchantments"));
        ArrayList bCustomEnchantments = (ArrayList) (ItemUtils.itemGetNbtPath(bSlot, "CustomEnchantments"));
        ArrayList customEnchantments = main.mergeCustomEnchantments(aCustomEnchantments,bCustomEnchantments);

        boolean validEnchant = false;

        if (aSlot.getType().equals(Material.ENCHANTED_BOOK) && bSlot.getType().equals(Material.ENCHANTED_BOOK)){
            validEnchant = true;
        }

        if (Enchantment.MENDING.canEnchantItem(new ItemStack(aSlot.getType(), 1)) && bSlot.getType().equals(Material.ENCHANTED_BOOK)){
            validEnchant = true;
        }

        if (aSlot.getType().equals(bSlot.getType()) && Enchantment.MENDING.canEnchantItem(new ItemStack(aSlot.getType(), 1))){
            validEnchant = true;
        }

        if (!validEnchant){
            return;
        }

        if (customEnchantments == null){
            return;
        }

        customEnchantments = ListUtils.removeDuplicates(customEnchantments);

        if (result == null){
            result = aSlot.clone();
        }

        if (result.getType().equals(Material.AIR)){
            result = aSlot.clone();
        }

        result = ItemUtils.itemSetNbtPath(result, "CustomEnchantments", customEnchantments);

        int aRL = 0;
        int bRL = 0;

        if ((!main.isEnchantmentBook(aSlot)) && aSlot.containsEnchantment(Enchantment.OXYGEN)){
            aRL = aSlot.getEnchantments().get(Enchantment.OXYGEN);
        }
        if (main.isEnchantmentBook(aSlot) && main.enchantmentBookContains(aSlot, Enchantment.OXYGEN)){
            aRL = ((EnchantmentStorageMeta)(aSlot.getItemMeta())).getStoredEnchantLevel(Enchantment.OXYGEN);
        }

        if ((!main.isEnchantmentBook(bSlot)) && bSlot.containsEnchantment(Enchantment.OXYGEN)){
            bRL = bSlot.getEnchantments().get(Enchantment.OXYGEN);
        }
        if (main.isEnchantmentBook(bSlot) && main.enchantmentBookContains(bSlot, Enchantment.OXYGEN)){
            bRL = ((EnchantmentStorageMeta)(bSlot.getItemMeta())).getStoredEnchantLevel(Enchantment.OXYGEN);
        }


        int highestRespiration = Math.max(aRL,bRL);


        if (main.hasVanillaEnchantments(result)){
            main.removeVirtualEnchantment(result);
        }

        List<String> lores = new ArrayList<>();
        for (Object i : customEnchantments){
            lores.add("§7"+main.languageMapping.get((String) i));
        }
        ItemMeta rItemMeta = result.getItemMeta();
        assert rItemMeta != null;
        rItemMeta.setLore(lores);

        if (highestRespiration == 0){
            rItemMeta.removeEnchant(Enchantment.OXYGEN);
        }

        result.setItemMeta(rItemMeta);

        if (!main.hasVanillaEnchantments(result)){
            main.addVirtualEnchantment(result);
        }

        result = main.itemUpdateUUID(result);

        event.setResult(result);

        boolean enchantmentCompatible = true;
        try{
            for (Object i : customEnchantments) {
                Enchantment[] notCompatibleVanilla = ((Enchantment[]) (main.getFieldFromInstance("notCompatibleVanilla", main.constructedEnchantments.get((String) i))));
                for (Enchantment ii : notCompatibleVanilla) {
                    boolean containEnchantedBook;
                    if (main.isEnchantmentBook(result)){
                        containEnchantedBook = main.enchantmentBookContains(result, ii);
                    }
                    else {
                        containEnchantedBook = result.containsEnchantment(ii);
                    }

                    if (containEnchantedBook) {
                        enchantmentCompatible = false;
                        break;
                    }
                }

                String[] notCompatibleCustom = ((String[]) (main.getFieldFromInstance("notCompatibleCustom", main.constructedEnchantments.get((String) i))));
                for (String ii : notCompatibleCustom) {
                    if (customEnchantments.contains(ii)) {
                        enchantmentCompatible = false;
                        break;
                    }
                }
            }
        }
        catch (Exception ignored){}

        if (!enchantmentCompatible){
            event.setResult(new ItemStack(Material.AIR));
        }

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
            if (ItemUtils.itemGetNbtPath(event.getCurrentItem(), "CustomEnchantments") != null){
                ItemStack result = ItemUtils.itemSetNbtPath(event.getCurrentItem(), "CustomEnchantments", new ArrayList<>());
                ItemMeta resultMeta = result.getItemMeta();
                assert resultMeta != null;
                resultMeta.setLore(new ArrayList<>());
                result.setItemMeta(resultMeta);
                main.removeVirtualEnchantment(result);
                event.setCurrentItem(result);
            }
        }

        String eventName = new Object(){}.getClass().getEnclosingMethod().getName();
        eventPass(eventName, event);

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

    @EventHandler
    public void InventoryInteractEvent(InventoryClickEvent event){
        String eventName = new Object(){}.getClass().getEnclosingMethod().getName();
        eventPass(eventName, event);

    }

    @EventHandler
    public void ProjectileLaunchEvent(ProjectileLaunchEvent event){
        String eventName = new Object(){}.getClass().getEnclosingMethod().getName();
        eventPass(eventName, event);
    }
}
