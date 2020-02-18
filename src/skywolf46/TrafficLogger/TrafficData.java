package skywolf46.TrafficLogger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.server.v1_12_R1.Packet;
import net.minecraft.server.v1_12_R1.PacketDataSerializer;
import net.minecraft.server.v1_12_R1.PacketPlayOutMap;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class TrafficData {
    private HashMap<Integer, AtomicInteger> packetTimes = new HashMap<>();
    private AtomicLong trafficSize = new AtomicLong();
    private AtomicLong packetCount = new AtomicLong();
    private static HashMap<Class, Integer> cls = new HashMap<>();
    private boolean isInbound;

    public void addPacket(Packet p, boolean in) {
        isInbound = in;
        ByteBuf buf = Unpooled.buffer();
        PacketDataSerializer pds = new PacketDataSerializer(buf);
        try {
            p.b(pds);
        } catch (IOException e) {
            e.printStackTrace();
        }
        trafficSize.addAndGet(buf.readableBytes());
        packetCount.incrementAndGet();
        packetTimes.computeIfAbsent(TrafficLogger.getTrafficTime().get(),a -> new AtomicInteger()).incrementAndGet();
    }

    public void nextTime() {

    }

    public AtomicLong getPacketCount() {
        return packetCount;
    }

    public AtomicLong getTrafficSize() {
        return trafficSize;
    }

    public boolean isInbound() {
        return isInbound;
    }

    public HashMap<Integer, AtomicInteger> getPacketTimes() {
        return packetTimes;
    }
}
