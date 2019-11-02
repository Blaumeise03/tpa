/*
 * Copyright (c) 2019 Blaumeise03
 */

package de.blaumeise03.tpa;

import org.bukkit.entity.Player;

public class TPARequest {
    private Player targetPlayer;
    private Player senderPlayer;
    long time;
    private long lastDamage;
    private boolean countdown = false;

    public TPARequest(Player targetPlayer, Player senderPlayer, long time) {
        this.targetPlayer = targetPlayer;
        this.senderPlayer = senderPlayer;
        this.time = time;
        lastDamage = -1;
    }

    public long getLastDamage() {
        return lastDamage;
    }

    public void setLastDamage(long lastDamage) {
        this.lastDamage = lastDamage;
    }

    public boolean isCountdown() {
        return countdown;
    }

    public void setCountdown(boolean countdown) {
        this.countdown = countdown;
    }

    public Player getTargetPlayer() {
        return targetPlayer;
    }

    public Player getSenderPlayer() {
        return senderPlayer;
    }
}
