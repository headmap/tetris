package net.tetris.dom;

public interface GameLevel {
    boolean accept(GlassEvent event);

    void apply();
}
