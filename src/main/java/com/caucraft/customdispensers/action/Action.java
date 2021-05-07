package com.caucraft.customdispensers.action;

import com.caucraft.customdispensers.ActionResult;
import com.caucraft.customdispensers.Config;
import com.caucraft.customdispensers.Recipe;
import com.caucraft.customdispensers.RecipeContext;

public interface Action {

    ActionResult act(RecipeContext context, Recipe recipe);
    default void validate(Recipe recipe, int actionNum) {};

    static Action parseAction(Config config, String action, int actionNumber) {
        String[] args = action.split(" +");
        try {
            return Config.getEnum(args[0], SupportedActions.class, null).parseAction.apply(config, args, actionNumber);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Could not parse action " + args[0] + " (" + action + ")", ex);
        }
    }

    default int getExpectedItems() { return 0; }
    default int getExpectedBlocks() { return 0; }
    default int getExpectedEntities() { return 0; }

    enum SupportedActions {
        CONSUME_ITEM(ConsumeItem::parseAction),
        DROP_ITEM(DropItem::parseAction),
        ADD_ITEM(AddItem::parseAction),
        INSERT_ITEM(InsertItem::parseAction),
        DAMAGE_ITEM(DamageItem::parseAction),
        SET_BLOCK(SetBlock::parseAction),
        BREAK_BLOCK(BreakBlock::parseAction),
//        ATTACK_ENTITY(AttackEntity::parseAction),
        PLAY_SOUND(PlaySound::parseAction),
        PARTICLE(Particle::parseAction);

        public final ParseFunction parseAction;

        SupportedActions(ParseFunction parseAction) {
            this.parseAction = parseAction;
        }
    }

    @FunctionalInterface
    static interface ParseFunction {
        Action apply(Config config, String[] args, int actionNumber);
    }
}
