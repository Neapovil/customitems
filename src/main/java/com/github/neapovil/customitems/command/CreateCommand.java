package com.github.neapovil.customitems.command;

import java.text.DecimalFormat;
import java.util.List;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import com.github.neapovil.customitems.CustomItems;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.BooleanArgument;
import dev.jorel.commandapi.arguments.DoubleArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.ItemStackArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public final class CreateCommand
{
    private static final CustomItems plugin = CustomItems.getInstance();

    public static final void register()
    {
        new CommandAPICommand("customitems")
                .withPermission(CustomItems.USER_COMMAND_PERMISSION)
                .withArguments(new LiteralArgument("create").withPermission(CustomItems.ADMIN_COMMAND_PERMISSION))
                .withArguments(new ItemStackArgument("itemstack"))
                .withArguments(new StringArgument("displayName"))
                .withArguments(new DoubleArgument("attackDamage"))
                .withArguments(new DoubleArgument("attackSpeed"))
                .withArguments(new BooleanArgument("full_enchanted"))
                .withArguments(new IntegerArgument("custom_model_data").replaceSuggestions(ArgumentSuggestions.strings(info -> new String[] { "-1" })))
                .executesPlayer((player, args) -> {
                    final ItemStack itemstack = (ItemStack) args[0];

                    if (itemstack.getType().equals(Material.AIR))
                    {
                        throw CommandAPI.fail("ItemStack is AIR");
                    }

                    apply(player, itemstack, args);

                    plugin.getFileConfig().set("customitems." + args[1], plugin.serialize(itemstack));

                    player.sendMessage("Custom item created");
                })
                .register();
    }

    private static void apply(Player player, ItemStack itemStack, Object[] args)
    {
        final double genericattackdamage = -player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getDefaultValue();
        final double genericattackspeed = -player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).getDefaultValue();

        final String displayname = (String) args[1];
        final double attackdamage = (double) args[2];
        final double attackspeed = (double) args[3];
        final boolean fullenchant = (boolean) args[4];
        final int custommodeldata = (int) args[5];

        final Damageable itemmeta = ((Damageable) itemStack.getItemMeta());
        final DecimalFormat decimalformat = new DecimalFormat("0.00");

        double newattackdamage = Double.valueOf(decimalformat.format(genericattackdamage + attackdamage));
        double newattackspeed = Double.valueOf(decimalformat.format(genericattackspeed + attackspeed));

        final AttributeModifier attributemodifier = new AttributeModifier(
                UUID.randomUUID(),
                "generic.attack_damage",
                newattackdamage,
                Operation.ADD_NUMBER,
                EquipmentSlot.HAND);
        final AttributeModifier attributemodifier1 = new AttributeModifier(
                UUID.randomUUID(),
                "generic.attack_speed",
                newattackspeed,
                Operation.ADD_NUMBER,
                EquipmentSlot.HAND);

        itemmeta.displayName(Component.text(displayname, NamedTextColor.LIGHT_PURPLE, TextDecoration.ITALIC));

        itemmeta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, attributemodifier);
        itemmeta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, attributemodifier1);

        double realattackdamage = Math.abs(genericattackdamage) + newattackdamage;

        if (fullenchant)
        {
            realattackdamage += .5 * 5 + .5;
        }

        final Component component = Component.text("Attack Damage: " + realattackdamage);
        final Component component1 = Component.text("Attack Speed: " + (Math.abs(genericattackspeed) + newattackspeed));

        itemmeta.lore(List.of(component, component1));

        if (custommodeldata != -1)
        {
            itemmeta.setCustomModelData(custommodeldata);
        }

        itemStack.setItemMeta(itemmeta);

        if (fullenchant)
        {
            final Enchantment sharpness = Enchantment.DAMAGE_ALL;
            final Enchantment fireaspect = Enchantment.FIRE_ASPECT;
            final Enchantment unbreaking = Enchantment.DURABILITY;

            if (sharpness.canEnchantItem(itemStack))
            {
                itemStack.addEnchantment(sharpness, 5);
            }

            if (fireaspect.canEnchantItem(itemStack))
            {
                itemStack.addEnchantment(fireaspect, 2);
            }

            if (unbreaking.canEnchantItem(itemStack))
            {
                itemStack.addEnchantment(unbreaking, 3);
            }
        }
    }
}
