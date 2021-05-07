package com.caucraft.customdispensers.action;

import com.caucraft.customdispensers.ActionResult;
import com.caucraft.customdispensers.Config;
import com.caucraft.customdispensers.Recipe;
import com.caucraft.customdispensers.RecipeContext;
import com.caucraft.customdispensers.action.reference.EntityReference;
import com.caucraft.customdispensers.action.reference.ItemReference;
import com.google.common.collect.Multimap;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;

@Deprecated
public class AttackEntity implements Action {

    private final EntityReference entityRef;
    private final ItemReference itemRef;

    public AttackEntity(EntityReference entityRef, ItemReference itemRef) {
        this.entityRef = entityRef;
        this.itemRef = itemRef;
    }

    @Override
    public int getExpectedEntities() {
        return 1;
    }

    @Override
    public ActionResult act(RecipeContext context, Recipe recipe) {
        Entity entity = entityRef.getTarget(context, recipe);
        ItemStack item = itemRef.getTarget(context, recipe);
        Multimap<Attribute, AttributeModifier> attrMap = item.getItemMeta().getAttributeModifiers(EquipmentSlot.HAND);
//        Collection<AttributeModifier> attrs = attrMap.get(Attribute.GENERIC_ATTACK_DAMAGE);
        System.out.println(item.getItemMeta().getAttributeModifiers());
        Collection<AttributeModifier> attrs = item.getItemMeta().getAttributeModifiers(Attribute.GENERIC_ATTACK_DAMAGE);

        double damage = 2;
        for (AttributeModifier mod : attrs) {
            switch (mod.getOperation()) {
                case ADD_NUMBER: {
                    damage += mod.getAmount();
                    break;
                }
                case ADD_SCALAR: {
                    damage += damage * (1 + mod.getAmount());
                    break;
                }
                case MULTIPLY_SCALAR_1: {
                    damage *= 1 + mod.getAmount();
                    break;
                }
                default: {
                    throw new UnsupportedOperationException("AttributeModifier Operation not supported: " + mod.getOperation().name());
                }
            }
        }

        EntityDamageByBlockEvent evt = new EntityDamageByBlockEvent(context.event.getBlock(), entity, EntityDamageEvent.DamageCause.CUSTOM, damage);
        Bukkit.getServer().getPluginManager().callEvent(evt);
        if (!evt.isCancelled()) {
            entity.setLastDamageCause(evt);
        }

        if (entity instanceof Damageable) {
            Damageable damageable = (Damageable) entity;
            damageable.damage(damage);
        } else if (entity instanceof Minecart) {
            Minecart minecart = (Minecart) entity;
            minecart.setDamage(damage);
        } else {
            // TODO how damage entity?

        }
        entity.setLastDamageCause(evt);

        return new ActionResult(new ItemStack[] { item }, null, entity);
    }

    @Override
    public void validate(Recipe recipe, int actionNum) {
        entityRef.validate(recipe);
        itemRef.validate(recipe);
    }

    public static Action parseAction(Config config, String[] args, int actionNumber) {
        EntityReference entityRef = EntityReference.getReference(config, args[1], actionNumber);
        ItemReference itemRef = ItemReference.getReference(config, args[2], actionNumber);
        return new AttackEntity(entityRef, itemRef);
    }
}
