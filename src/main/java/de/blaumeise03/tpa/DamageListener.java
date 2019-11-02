/*
 * Copyright (c) 2019 Blaumeise03
 */

package de.blaumeise03.tpa;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Date;

public class DamageListener implements Listener {

    @EventHandler
    public void onDamage(EntityDamageEvent e){
        if(e.getEntity() instanceof Player){
            Player p = (Player) e.getEntity();
            for(TPARequest r : TPA.requestMap.values()){
                if(r.getSenderPlayer() == p){
                    r.setLastDamage(new Date().getTime());
                    break;
                }
            }

        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e){
        for(TPARequest request : TPA.requestMap.values()){
            if(request.getSenderPlayer() == e.getPlayer()){
                if(e.getTo() != null && request.isCountdown()){
                    if(e.getFrom().getBlockX() != e.getTo().getBlockX() || e.getFrom().getBlockY() != e.getTo().getBlockY() || e.getFrom().getBlockZ() != e.getTo().getBlockZ()){
                        TPA.requestMap.remove(request.getTargetPlayer());
                        break;
                    }
                }
            }
        }
    }
}
