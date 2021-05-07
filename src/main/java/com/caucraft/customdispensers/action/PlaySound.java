package com.caucraft.customdispensers.action;

import com.caucraft.customdispensers.ActionResult;
import com.caucraft.customdispensers.Config;
import com.caucraft.customdispensers.Recipe;
import com.caucraft.customdispensers.RecipeContext;
import org.bukkit.Location;
import org.bukkit.Sound;

public class PlaySound implements Action {

    private final Sound sound;
    private final float pitch;
    private final float variance;

    public PlaySound(Sound sound, float pitch, float variance) {
        this.sound = sound;
        this.pitch = pitch;
        this.variance = variance;
    }

    @Override
    public ActionResult act(RecipeContext context, Recipe recipe) {
        Location loc = context.event.getBlock().getLocation().add(0.5, 0.5, 0.5);
        float adjustPitch;
        if (variance <= 0.0F) {
            adjustPitch = pitch;
        } else {
            float min = Math.max(0.5F, pitch - variance);
            float max = Math.min(2.0F, pitch + variance);
            double rnd = Math.random();
            adjustPitch = (float) (min * rnd + max * (1 - rnd));
        }
        loc.getWorld().playSound(loc, sound, 1.0F, adjustPitch);
        return new ActionResult(null, null, null);
    }

    public static Action parseAction(Config config, String[] args, int actionNumber) {
        Sound sound = Config.getEnum(args[1], Sound.class, "Check the Sound javadoc for your server version.");
        float pitch = Math.max(0.5F, Math.min(2.0F, Float.parseFloat(args[2])));
        float variance = 0.0F;
        if (args.length > 3) {
            variance = Float.parseFloat(args[3]);
        }
        return new PlaySound(sound, pitch, variance);
    }
}

