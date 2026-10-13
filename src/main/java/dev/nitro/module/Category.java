package dev.nitro.module;

public enum Category {
    COMBAT("Combat"), MOVEMENT("Movement"), RENDER("Render"), WORLD("World");

    public final String display;
    Category(String display) { this.display = display; }
}
