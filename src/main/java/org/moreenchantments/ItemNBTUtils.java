package org.moreenchantments;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBTList;
import de.tr7zw.nbtapi.iface.ReadableNBT;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public class ItemNBTUtils {
    public static boolean containsCustomEnchantments(ItemStack item) {
        try{
            ReadableNBT nbt = NBT.itemStackToNBT(item);
            if (!nbt.hasTag("components")) return false;
            ReadableNBT components = nbt.getCompound("components");
            assert components != null;
            if (!components.hasTag("minecraft:custom_data")) return false;
            ReadableNBT customData = components.getCompound("minecraft:custom_data");
            assert customData != null;
            return customData.hasTag("CustomEnchantments");
        }
        catch (Exception e){
            return false;
        }
    }

    public static ArrayList<String> getCustomEnchantments(ItemStack item) {
        ReadableNBT nbt = NBT.itemStackToNBT(item);
        ReadableNBT components = nbt.getCompound("components");
        if (components == null) return new ArrayList<>();
        ReadableNBT customData = components.getCompound("minecraft:custom_data");
        if (customData == null) return new ArrayList<>();
        ArrayList<String> customEnchantments = new ArrayList<>();
        customData.getStringList("CustomEnchantments").forEach(customEnchantments::add);
        return customEnchantments;
    }

    public static ItemStack setCustomEnchantments(ItemStack item, Collection<String> customEnchantments) {
        ReadWriteNBT nbt = NBT.itemStackToNBT(item);
        ReadWriteNBT components = nbt.getOrCreateCompound("components");
        ReadWriteNBT customData = components.getOrCreateCompound("minecraft:custom_data");
        ReadWriteNBTList<String> list = customData.getStringList("CustomEnchantments");
        customEnchantments.forEach(list::add);
        return NBT.itemStackFromNBT(nbt);
    }

//    public static ItemStack setUUID(ItemStack item, UUID uuid) {
//        ReadWriteNBT nbt = NBT.itemStackToNBT(item);
//        ReadWriteNBT components = nbt.getOrCreateCompound("components");
//        components.setString("UUID", uuid.toString());
//        return NBT.itemStackFromNBT(nbt);
//    }
    public static ItemStack setUUID(ItemStack item, UUID uuid) {
        ReadWriteNBT nbt = NBT.itemStackToNBT(item);
        ReadWriteNBT components = nbt.getOrCreateCompound("components");
        ReadWriteNBT customData = components.getOrCreateCompound("minecraft:custom_data");
        customData.setString("UUID", uuid.toString());
        return NBT.itemStackFromNBT(nbt);
    }

//    public static boolean containsUUID(ItemStack item) {
//        ReadableNBT nbt = NBT.itemStackToNBT(item);
//        if (!nbt.hasTag("components")) return false;
//        ReadableNBT components = nbt.getCompound("components");
//        assert components != null;
//        return components.hasTag("UUID");
//    }
    public static boolean containsUUID(ItemStack item) {
        ReadableNBT nbt = NBT.itemStackToNBT(item);
        if (!nbt.hasTag("components")) return false;
        ReadableNBT components = nbt.getCompound("components");
        assert components != null;
        if (!components.hasTag("minecraft:custom_data")) return false;
        ReadableNBT customData = components.getCompound("minecraft:custom_data");
        assert customData != null;
        return customData.hasTag("UUID");
    }

//    public static UUID getUUID(ItemStack item) {
//        ReadableNBT nbt = NBT.itemStackToNBT(item);
//        ReadableNBT components = nbt.getCompound("components");
//        assert components != null;
//        return UUID.fromString(components.getString("UUID"));
//    }
    public static UUID getUUID(ItemStack item) {
        ReadableNBT nbt = NBT.itemStackToNBT(item);
        ReadableNBT components = nbt.getCompound("components");
        ReadableNBT customData = components.getCompound("minecraft:custom_data");
        return UUID.fromString(customData.getString("UUID"));
    }

//    public static boolean containsCustomEnchantment(ItemStack itemStack, String enchantment) {
//        ReadableNBT nbt = NBT.itemStackToNBT(itemStack);
//        ReadableNBT components = nbt.getCompound("components");
//        assert components != null;
//        if (!components.hasTag("CustomEnchantments")) return false;
//        return components.getStringList("CustomEnchantments").contains(enchantment);
//    }
    public static boolean containsCustomEnchantment(ItemStack itemStack, String enchantment) {
        ReadableNBT nbt = NBT.itemStackToNBT(itemStack);
        if (!nbt.hasTag("components")) return false;
        ReadableNBT components = nbt.getCompound("components");
        assert components != null;

        if (!components.hasTag("minecraft:custom_data")) return false;
        ReadableNBT customData = components.getCompound("minecraft:custom_data");
        assert customData != null;

        return customData.getStringList("CustomEnchantments").contains(enchantment);
    }

//    public static ItemStack setBoolean(ItemStack item, String key, boolean value) {
//        ReadWriteNBT nbt = NBT.itemStackToNBT(item);
//        ReadWriteNBT components = nbt.getOrCreateCompound("components");
//        components.setBoolean(key, value);
//        return NBT.itemStackFromNBT(nbt);
//    }
    public static ItemStack setBoolean(ItemStack item, String key, boolean value) {
        ReadWriteNBT nbt = NBT.itemStackToNBT(item);
        ReadWriteNBT components = nbt.getOrCreateCompound("components");
        ReadWriteNBT customData = components.getOrCreateCompound("minecraft:custom_data");
        customData.setBoolean(key, value);
        return NBT.itemStackFromNBT(nbt);
    }

//    public static boolean getBoolean(ItemStack item, String key) {
//        ReadableNBT nbt = NBT.itemStackToNBT(item);
//        if (!nbt.hasTag("components")) return false;
//        ReadableNBT components = nbt.getCompound("components");
//        assert components != null;
//        if (!components.hasTag(key)) return false;
//        return components.getBoolean(key);
//    }
    public static boolean getBoolean(ItemStack item, String key) {
        ReadableNBT nbt = NBT.itemStackToNBT(item);
        if (!nbt.hasTag("components")) return false;
        ReadableNBT components = nbt.getCompound("components");
        assert components != null;
        if (!components.hasTag("minecraft:custom_data")) return false;
        ReadableNBT customData = components.getCompound("minecraft:custom_data");
        assert customData != null;
        return customData.getBoolean(key);
    }
}
