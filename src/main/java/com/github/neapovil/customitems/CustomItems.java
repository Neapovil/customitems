package com.github.neapovil.customitems;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.file.FileConfig;
import com.github.neapovil.customitems.command.CreateCommand;
import com.github.neapovil.customitems.command.DeleteCommand;
import com.github.neapovil.customitems.command.GetCommand;

public final class CustomItems extends JavaPlugin
{
    private static CustomItems instance;
    public static final String USER_COMMAND_PERMISSION = "customitems.command";
    public static final String ADMIN_COMMAND_PERMISSION = "customitems.command.admin";
    private FileConfig config;

    @Override
    public void onEnable()
    {
        instance = this;

        this.saveResource("customitems.json", false);

        this.config = FileConfig.builder(new File(this.getDataFolder(), "customitems.json"))
                .autoreload()
                .autosave()
                .build();
        this.config.load();

        CreateCommand.register();
        GetCommand.register();
        DeleteCommand.register();
    }

    @Override
    public void onDisable()
    {
    }

    public static CustomItems getInstance()
    {
        return instance;
    }

    public FileConfig getFileConfig()
    {
        return this.config;
    }

    public String serialize(ItemStack itemstack)
    {
        try
        {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeInt(1);

            dataOutput.writeObject(itemstack.serializeAsBytes());

            dataOutput.close();

            return Base64Coder.encodeLines(outputStream.toByteArray());
        }
        catch (Exception e)
        {
            this.getLogger().severe("Unable to serialize itemstack: " + e.getMessage());
        }

        return null;
    }

    public ItemStack deserialize(String base64)
    {
        try
        {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(base64));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

            dataInput.readInt();

            byte[] itemstackobject = (byte[]) dataInput.readObject();

            ItemStack itemstack = ItemStack.deserializeBytes(itemstackobject);

            dataInput.close();

            return itemstack;
        }
        catch (Exception e)
        {
            this.getLogger().severe("Unable to deserialize base64: " + e.getMessage());
        }

        return null;
    }

    public String[] getCustomItems()
    {
        final UnmodifiableConfig config = this.config.get("customitems");
        return config.entrySet().stream().map(e -> e.getKey()).toArray(String[]::new);
    }
}
