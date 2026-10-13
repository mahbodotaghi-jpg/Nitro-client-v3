package dev.nitro.module.modules;

import dev.nitro.module.Category;
import dev.nitro.module.Module;
import dev.nitro.setting.NumberSetting;

public class Flight extends Module {
    private final NumberSetting speed = add(new NumberSetting("Speed", 0.6, 0.1, 3.0, 0.1));

    public Flight() { super("Flight", "Velocity flight. Space = up, Shift = down.", Category.MOVEMENT); }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        double s = speed.get();
        double rad = Math.toRadians(mc.player.getYaw());
        float f = mc.player.input.movementForward;
        float side = mc.player.input.movementSideways;

        double vx = (-Math.sin(rad) * f + Math.cos(rad) * side) * s;
        double vz = (Math.cos(rad) * f + Math.sin(rad) * side) * s;
        double vy = mc.options.jumpKey.isPressed() ? s : mc.options.sneakKey.isPressed() ? -s : 0;

        mc.player.setVelocity(vx, vy, vz);
    }
}
