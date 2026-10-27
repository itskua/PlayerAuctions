package com.minekarta.playerauction.common;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.io.Serializable;

public final class SerializedItem implements Serializable {
    private final String data;

    private SerializedItem(String data) {
        this.data = data;
    }

    public String getBase64() {
        return data;
    }

    public ItemStack toItemStack() {
        if (data == null || data.isEmpty()) {
            return null;
        }
        try {
            byte[] bytes = Base64.getDecoder().decode(data);
            try {
                return ItemStack.deserializeBytes(bytes);
            } catch (Exception ex) {
                // Fallback for legacy BukkitObjectInputStream serialized data
                try (ByteArrayInputStream is = new ByteArrayInputStream(bytes);
                     BukkitObjectInputStream ois = new BukkitObjectInputStream(is)) {
                    return (ItemStack) ois.readObject();
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Could not deserialize ItemStack", e);
        }
    }

    public static SerializedItem fromItemStack(ItemStack itemStack) {
        if (itemStack == null) {
            return new SerializedItem(null);
        }
        try {
            return new SerializedItem(Base64.getEncoder().encodeToString(itemStack.serializeAsBytes()));
        } catch (Exception e) {
            try (ByteArrayOutputStream os = new ByteArrayOutputStream();
                 BukkitObjectOutputStream oos = new BukkitObjectOutputStream(os)) {
                oos.writeObject(itemStack);
                return new SerializedItem(Base64.getEncoder().encodeToString(os.toByteArray()));
            } catch (IOException ioException) {
                throw new IllegalStateException("Could not serialize ItemStack", ioException);
            }
        }
    }

    public static SerializedItem fromBase64(String base64) {
        return new SerializedItem(base64);
    }

    @Override
    public String toString() {
        // Avoid logging potentially huge base64 string
        return "SerializedItem{data='...'}";
    }
}

