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
import dev.jorel.commandapi.arguments.DoubleArgument;
import dev.jorel.commandapi.arguments.EnchantmentArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
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
                .withPermission(CustomItems.ADMIN_COMMAND_PERMISSION)
                .withArguments(new LiteralArgument("create"))
                .withArguments(new StringArgument("displayName"))
                .withArguments(new DoubleArgument("attackDamage"))
                .withArguments(new DoubleArgument("attackSpeed"))
                .withArguments(new IntegerArgument("durability"))
                .executesPlayer((player, args) -> {
                    final ItemStack itemstack = player.getInventory().getItemInMainHand();

                    if (itemstack.getType().equals(Material.AIR))
                    {
                        CommandAPI.fail("ItemStack is AIR");
                    }

                    apply(player, itemstack, args, null);

                    plugin.getFileConfig().set("customitems." + args[0], plugin.serialize(itemstack));

                    player.sendMessage("Custom item created");
                })
                .register();

        new CommandAPICommand("customitems")
                .withPermission(CustomItems.ADMIN_COMMAND_PERMISSION)
                .withArguments(new LiteralArgument("create"))
                .withArguments(new StringArgument("displayName"))
                .withArguments(new DoubleArgument("attackDamage"))
                .withArguments(new DoubleArgument("attackSpeed"))
                .withArguments(new IntegerArgument("durability"))
                .withArguments(new EnchantmentArgument("enchantment"))
                .withArguments(new IntegerArgument("level", 1, 5))
                .executesPlayer((player, args) -> {
                    final ItemStack itemstack = player.getInventory().getItemInMainHand();

                    if (itemstack.getType().equals(Material.AIR))
                    {
                        CommandAPI.fail("ItemStack is AIR");
                    }

                    final EnchantmentObject o = new EnchantmentObject((Enchantment) args[4], (int) args[5]);

                    apply(player, itemstack, args, o);

                    plugin.getFileConfig().set("customitems." + args[0], plugin.serialize(itemstack));

                    player.sendMessage("Custom item created");
                })
                .register();
    }

    private static void apply(Player player, ItemStack itemStack, Object[] args, EnchantmentObject o)
    {
        final double genericattackdamage = -player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getDefaultValue();
        final double genericattackspeed = -player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).getDefaultValue();

        final String displayname = (String) args[0];
        final double attackdamage = (double) args[1];
        final double attackspeed = (double) args[2];
        final int durability = (int) args[3];

        final Damageable itemmeta = ((Damageable) itemStack.getItemMeta());
        final DecimalFormat decimalformat = new DecimalFormat("0.00");

        double newattackdamage = Double.valueOf(decimalformat.format(genericattackdamage + attackdamage));
        double newattackspeed = Double.valueOf(decimalformat.format(genericattackspeed + attackspeed));

        if (o != null)
        {
            newattackdamage += .5 * o.getLevel() + .5;
        }

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
        itemmeta.setDamage(itemStack.getType().getMaxDurability() - durability);

        final Component component = Component.text("Attack Damage: " + (Math.abs(genericattackdamage) + newattackdamage));
        final Component component1 = Component.text("Attack Speed: " + (Math.abs(genericattackspeed) + newattackspeed));

        itemmeta.lore(List.of(component, component1));

        itemStack.setItemMeta(itemmeta);

        if (o != null)
        {
            itemStack.addEnchantment(o.getEnchantment(), o.getLevel());
        }
    }

    static class EnchantmentObject
    {
        private final Enchantment enchantment;
        private final int level;

        public EnchantmentObject(Enchantment enchantment, int level)
        {
            this.enchantment = enchantment;
            this.level = level;
        }

        public Enchantment getEnchantment()
        {
            return this.enchantment;
        }

        public int getLevel()
        {
            return this.level;
        }
    }
}
