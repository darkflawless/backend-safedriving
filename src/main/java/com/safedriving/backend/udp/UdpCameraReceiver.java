package com.safedriving.backend.udp;

import com.safedriving.backend.websocket.RealTimeBroadcaster;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Base64;

/**
 * Chạy một background thread để mở cổng socket UDP, bóc tách packet và đẩy sang WebSocket.
 */
public class UdpCameraReceiver {

    private static final Logger log = LoggerFactory.getLogger(UdpCameraReceiver.class);

    private final int port;
    private final int bufferSize;
    private final RealTimeBroadcaster broadcaster;

    private DatagramSocket socket;
    private Thread listenerThread;
    private volatile boolean running = false;

    public UdpCameraReceiver(int port, int bufferSize, RealTimeBroadcaster broadcaster) {
        this.port = port;
        this.bufferSize = bufferSize;
        this.broadcaster = broadcaster;
        start();
    }

    private void start() {
        try {
            socket = new DatagramSocket(port);
            running = true;
            log.info("[UDP] Camera receiver đang lắng nghe trên cổng {}", port);

            listenerThread = new Thread(this::listenLoop, "udp-camera-listener");
            listenerThread.setDaemon(true);
            listenerThread.start();
        } catch (Exception e) {
            log.error("[UDP] Không thể mở socket UDP trên cổng {}: {}", port, e.getMessage());
        }
    }

    private void listenLoop() {
        byte[] buffer = new byte[bufferSize];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

        while (running) {
            try {
                socket.receive(packet); // Chờ nhận packet từ App
                processPacket(packet.getData(), packet.getLength());
            } catch (Exception e) {
                if (running) {
                    log.error("[UDP] Lỗi nhận packet: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * Packet format: [8 bytes driverId (long)] + [N bytes JPEG raw frame]
     */
    private void processPacket(byte[] data, int length) {
        if (length < 9) {
            log.warn("[UDP] Packet quá ngắn ({} bytes), bỏ qua", length);
            return;
        }

        // Tách 8 byte đầu -> driverId
        long driverId = bytesToLong(data, 0);

        // Byte còn lại là dữ liệu ảnh JPEG
        byte[] frameBytes = new byte[length - 8];
        System.arraycopy(data, 8, frameBytes, 0, frameBytes.length);

        // Encode Base64 để gửi qua WebSocket
        String base64Frame = Base64.getEncoder().encodeToString(frameBytes);

        // Đẩy frame lên Web Admin
        broadcaster.sendCameraFrame(driverId, base64Frame);
    }

    private long bytesToLong(byte[] data, int offset) {
        long value = 0;
        for (int i = 0; i < 8; i++) {
            value = (value << 8) | (data[offset + i] & 0xFF);
        }
        return value;
    }

    @PreDestroy
    public void stop() {
        running = false;
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        log.info("[UDP] Camera receiver đã dừng.");
    }
}
