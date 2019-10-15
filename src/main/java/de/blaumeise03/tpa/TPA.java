
/*
 *     Copyright (C) 2019  Blaumeise03 - bluegame61@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.blaumeise03.tpa;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class TPA extends JavaPlugin {

    public static Plugin plugin;

    public static Map<Player, TPARequest> requestMap = new HashMap<>();

    @Override
    public void onEnable() {
        getLogger().info("Enabling TPA...");
        plugin = this;
        getLogger().info("Initializing Commands...");
        initCommands();
        getLogger().info("Registering Events...");
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new DamageListener(), this);
        getLogger().info("Complete!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling TPA...");

        getLogger().info("Complete!");
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        Command.executeCommand(args, sender, label);
        return true;
    }

    private void initCommands(){
        new Command("tpa", "Stellt eine Teleportanfrage an einen Spieler.", new Permission("tpa.tpa"), true) {
            @Override
            public void onCommand(String[] args, CommandSender sender) {
                Player playerSender = (Player) sender;
                if(args.length == 0){
                    sender.sendMessage("§4Bitte gebe einen Spielername an!");
                    return;
                }

                String playerName = args[0];
                Player player = Bukkit.getPlayer(playerName);
                if(player == null){
                    sender.sendMessage("§4Dieser Spieler existiert nicht oder er ist nicht online!");
                    return;
                }

                if(player.getWorld() != playerSender.getWorld()){
                    sender.sendMessage("§4Der Spieler befindet sich in einer anderen Dimension!");
                    return;
                }
                for (TPARequest request : requestMap.values()) {
                    if (request.getSenderPlayer() == sender && request.getTargetPlayer() == player) {
                        if (new Date().getTime() <= 60000) {
                            sender.sendMessage("§4Du hast bereits eine Anfrage geschickt!");
                            return;
                        }
                        requestMap.remove(player);
                    }
                }
                if (player == playerSender) {
                    sender.sendMessage("§4Tut mir leid. Du kannst dich nicht zu dir selbst teleportieren.");
                    return;
                }
                //player.sendRawMessage("[\"\",{\"text\":\"" + playerSender.getName() +"\",\"color\":\"dark_green\",\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"" + playerSender.getUniqueId().toString() + "\",\"color\":\"dark_aqua\"}]}}},{\"text\":\" möchte sich zu dir teleportieren. \",\"color\":\"green\"},{\"text\":\"[Annehmen]\",\"color\":\"dark_green\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/tpaAccept " + playerSender.getName() +"\"}},{\"text\":\"  \",\"color\":\"none\"},{\"text\":\"[Ablehnen]\",\"color\":\"dark_red\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/tpaDeny " + playerSender.getName() +"\"}}]");
                TextComponent msg1 = new TextComponent(playerSender.getName());
                msg1.setColor(ChatColor.AQUA);
                TextComponent msg2 = new TextComponent(" will sich zu dir teleportieren! ");
                msg2.setColor(ChatColor.GREEN);
                msg1.addExtra(msg2);
                TextComponent msg3 = new TextComponent("[Annehmen] ");
                msg3.setColor(ChatColor.DARK_GREEN);
                msg3.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaAccept " + playerSender.getName()));
                msg1.addExtra(msg3);
                TextComponent msg4 = new TextComponent("[Ablehnen]");
                msg4.setColor(ChatColor.DARK_RED);
                msg4.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tpaDeny " + playerSender.getName()));
                msg1.addExtra(msg4);

                player.spigot().sendMessage(msg1);
                Date time = new Date();
                requestMap.put(player, new TPARequest(player, (Player) sender, time.getTime()));
                sender.sendMessage("§aAnfrage gesendet!");
            }
        };
        new Command("tpaAccept", "Nehm eine Teleportanfrage an.", new Permission("tpa.tpa"), true){
            @Override
            public void onCommand(String[] args, CommandSender sender){
                if(args.length == 0) return;
                String name = args[0];
                Player player = Bukkit.getPlayer(name);
                if(player == null){
                    sender.sendMessage("§aDieser Spieler ist nicht mehr online!");
                    return;
                }
                TPARequest request = requestMap.get(sender);
                if(request == null){
                    sender.sendMessage("§4Keine Offene Anfrage von diesem Spieler!");
                    return;
                }
                Date time = new Date();
                if(time.getTime() - request.time > 60000){
                    requestMap.remove(sender);
                    sender.sendMessage("§4Die Anfrage ist abgelaufen!");
                }else if(request.getLastDamage() == -1 || (request.getLastDamage() - time.getTime() > 10000)){
                    player.teleport(((Player) sender));
                    player.sendMessage("§aDu wurdest erfolgreich teleportiert!");
                    sender.sendMessage("§aDer Spieler §6" + player.getName() + " §ahat sich zu dir teleportiert!");
                    getLogger().info("Der Spieler " + player.getName() + " hat sich zum Spieler " + sender.getName() + " teleportiert!");
                    requestMap.remove(sender);
                } else if ((request.getLastDamage() - time.getTime() < 60000)) {
                    player.sendMessage("§aDu wirst in 5 Sekunden teleportiert. Beweg dich nicht!");
                    request.setCountdown(true);
                    new BukkitRunnable() {
                        int i = 5;

                        @Override
                        public void run() {
                            i--;
                            if (i < 0) {
                                cancel();
                                return;
                            }
                            player.sendMessage("§6" + i + (i <= 1 ? " Sekunde " : " Sekunden ") + "§abis zum Teleport!");
                            if(i == 0){

                                if(!requestMap.containsKey(sender)){
                                    player.sendMessage("§4Du hast dich bewegt! Teleport abgebrochen!");
                                    cancel();
                                    return;
                                }
                                player.teleport(((Player) sender));
                                player.sendMessage("§aDu wurdest teleportiert!");
                                sender.sendMessage("§6" + player.getName() + " §ahat sich zu dir teleportiert!");
                                getLogger().info("Der Spieler " + sender.getName() + " hat sich zum Spieler " + player.getName() + " teleportiert!");
                                cancel();
                            }
                        }
                    }.runTaskTimer(TPA.plugin, 20, 20);
                }

            }
        };

        new Command("tpaDeny", "Lehn eine Teleportanfrage ab.", new Permission("tpa.tpa"), true){
            @Override
            public void onCommand(String[] args, CommandSender sender){
                TPARequest request = requestMap.get(sender);
                if (request == null) {
                    sender.sendMessage("§4Es steht keine Anfrage von diesem Spieler aus!");
                    return;
                }
                if (new Date().getTime() - request.time > 60000) {
                    sender.sendMessage("§aDiese Anfrage ist bereits ausgelaufen!");
                    requestMap.remove(sender);
                    return;
                }
                sender.sendMessage("§aAnfrage abgelehnt!");
                requestMap.remove(sender);
                request.getSenderPlayer().sendMessage("§4Deine Anfrage wurde von §6" + sender.getName() + " §4abgelehnt!");
            }
        };

    }
}
