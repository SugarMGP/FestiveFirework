package io.github.sugarmgp.festivefirework.util;

import org.bukkit.entity.Firework;

public class RemoveFirework implements Runnable {
    private final Firework firework;

    public RemoveFirework(final Firework firework) {
        this.firework = firework;
    }

    @Override
    public void run() {
        this.firework.remove();
    }
}
