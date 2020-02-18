package skywolf46.TrafficLogger;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import net.minecraft.server.v1_12_R1.Packet;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import skywolf46.CommandAnnotation.v1_3R1.API.MinecraftAbstractCommand;
import skywolf46.CommandAnnotation.v1_3R1.CommandAnnotation;
import skywolf46.CommandAnnotation.v1_3R1.Data.CommandArgument;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class TrafficLogger extends JavaPlugin {
    private static HashMap<String, TrafficData> list = new HashMap<>();
    private static AtomicInteger trafficTime = new AtomicInteger(0);

    @Override
    public void onEnable() {
        CommandAnnotation.forceInit(this);
        for (PacketType t : PacketType.values())
            list.put(t.name(), new TrafficData());
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(TrafficLogger.this, PacketType.values()) {
            @Override
            public void onPacketReceiving(PacketEvent event) {

                list.get(event.getPacketType().name()).addPacket((Packet) event.getPacket().getHandle(), true);
            }

            @Override
            public void onPacketSending(PacketEvent event) {
                list.get(event.getPacketType().name()).addPacket((Packet) event.getPacket().getHandle(), false);
            }
        });
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                trafficTime.incrementAndGet();
                for (TrafficData td : list.values())
                    td.nextTime();
                trafficTime.incrementAndGet();
            }
        }, 20L, 20L);

        MinecraftAbstractCommand.builder()
                .command("/trafficlog")
                .add(new MinecraftAbstractCommand() {
                    @Override
                    public boolean onCommand(CommandArgument commandArgument) {
                        CommandSender cs = commandArgument.get(CommandSender.class);
                        if (!cs.hasPermission("trafficLogger.admin") && !cs.isOp()) {
                            cs.sendMessage("§cPermission denied.");
                            return false;
                        }
                        File trLog = new File(TrafficLogger.this.getDataFolder(), "traffic.log");
                        try {
                            if (!trLog.exists()) {
                                trLog.getParentFile().mkdirs();
                                trLog.createNewFile();
                            }
                            BufferedWriter br = new BufferedWriter(new FileWriter(trLog, false));
                            br.append("TrafficLogger Packet Report\r\n");
                            for (Map.Entry<String, TrafficData> as : list.entrySet()) {
                                br.append("- ").append(as.getKey()).append("(").append(as.getValue().isInbound() ? "Resv" : "Send").append("): ").append(String.valueOf(as.getValue().getPacketCount().get())).append(" times (").append(String.valueOf(as.getValue().getTrafficSize().get())).append(" bytes)");
                                int total = 0;
                                int max = 0;
                                for (AtomicInteger ai : as.getValue().getPacketTimes().values()) {
                                    total += ai.get();
                                    if(max < ai.get())
                                        max = ai.get();
                                }
                                int avg = (int) ((double)as.getValue().getPacketCount().get() / (double)getTrafficTime().get());
                                br.append(" [Total: ").append(String.valueOf(total)).append(", Max ").append(String.valueOf(max)).append(", Avg ").append(String.valueOf(avg)).append("]");
                                br.append("\r\n");
                            }
                            br.flush();
                            br.close();
                        } catch (Exception ex) {
                            cs.sendMessage("§cError occured while parsing traffic");
                        }
                        cs.sendMessage("§aTraffic log saved at plugins/TrafficLogger/traffic.log");
                        return false;
                    }

                    @Override
                    public int getCommandPriority() {
                        return 0;
                    }
                })
                .complete();
    }

    public static AtomicInteger getTrafficTime() {
        return trafficTime;
    }
}


