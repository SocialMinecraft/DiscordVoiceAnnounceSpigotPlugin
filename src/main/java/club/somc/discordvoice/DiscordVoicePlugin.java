package club.somc.discordvoice;

import club.somc.protos.DiscordVoice;
import club.somc.protos.DiscordVoiceEntered;
import club.somc.protos.DiscordVoiceLeft;
import com.google.protobuf.InvalidProtocolBufferException;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Nats;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeoutException;

public class DiscordVoicePlugin extends JavaPlugin {

    Connection nc;

    @Override
    public void onEnable() {
        super.onEnable();
        this.saveDefaultConfig();

        try {
            this.nc = Nats.connect(getConfig().getString("natsUrl"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        Dispatcher dispatcher = nc.createDispatcher((msg) -> {
            Bukkit.getScheduler().runTask(this, () -> {
                try {
                    if (msg.getSubject().equals("discord.voice.entered")) {
                        DiscordVoiceEntered event = null;
                        event = DiscordVoiceEntered.parseFrom(msg.getData());
                        Bukkit.broadcastMessage(ChatColor.WHITE + "["+event.getUsername()+"] " +
                                ChatColor.LIGHT_PURPLE + "Joined the Discord Voice Chat.");
                    }

                    if (msg.getSubject().equals("discord.voice.left")) {
                        DiscordVoiceLeft event = null;
                        event = DiscordVoiceLeft.parseFrom(msg.getData());
                        Bukkit.broadcastMessage(ChatColor.WHITE + "["+event.getUsername()+"] " +
                                ChatColor.LIGHT_PURPLE + "Left the Discord Voice Chat.");
                    }
                } catch (InvalidProtocolBufferException e) {
                    throw new RuntimeException(e);
                }
            });
        });
        dispatcher.subscribe("discord.voice.*");
    }

    @Override
    public void onDisable() {
        super.onDisable();

        if (this.nc != null) {
            try {
                this.nc.drain(Duration.ofSeconds(5));
            } catch (TimeoutException e) {
                //throw new RuntimeException(e);
            } catch (InterruptedException e) {
                //throw new RuntimeException(e);
            }
        }
    }
}
