package dev.nitro.module.modules;

import dev.nitro.module.Category;
import dev.nitro.module.Module;

public class Sprint extends Module {
    public Sprint() { super("Sprint", "Always sprint while moving forward.", Category.MOVEMENT); }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (mc.player.input.movementForward > 0 && !mc.player.horizontalCollision
                && mc.player.getHungerManager().getFoodLevel() > 6) {
            mc.player.setSprinting(true);
        }
    }
}
