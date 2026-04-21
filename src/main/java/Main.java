import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public class Main {

    public static void main(String[] args) {
        try {
            String targetIp = "127.0.0.1";
            int targetPort = 6454;

            byte[] dmxData = new byte[8];
            dmxData[0] = (byte) 255;  // canale 1
            dmxData[1] = (byte) 120;  // canale 2
            dmxData[2] = (byte) 60;   // canale 3
            dmxData[3] = (byte) 10;   // canale 4
            dmxData[4] = (byte) 0;
            dmxData[5] = (byte) 200;
            dmxData[6] = (byte) 30;
            dmxData[7] = (byte) 90;

            byte[] packet = buildArtDmxPacket(
                    14,     // protocol version
                    1,      // sequence
                    0,      // physical
                    0,      // subUni
                    0,      // net
                    dmxData
            );

            InetAddress address = InetAddress.getByName(targetIp);

            try (DatagramSocket socket = new DatagramSocket()) {
                DatagramPacket datagramPacket =
                        new DatagramPacket(packet, packet.length, address, targetPort);

                socket.send(datagramPacket);

                System.out.println("Pacchetto ArtDmx inviato a " + targetIp + ":" + targetPort);
                System.out.println("Lunghezza pacchetto: " + packet.length);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static byte[] buildArtDmxPacket(int protocolVersion,
                                            int sequence,
                                            int physical,
                                            int subUni,
                                            int net,
                                            byte[] dmxData) {

        int length = dmxData.length;

        if (length < 2 || length > 512) {
            throw new IllegalArgumentException("La lunghezza DMX deve essere tra 2 e 512");
        }

        byte[] packet = new byte[18 + length];

        // ID "Art-Net\0"
        byte[] id = "Art-Net\u0000".getBytes(StandardCharsets.US_ASCII);
        System.arraycopy(id, 0, packet, 0, id.length);

        // OpCode = 0x5000 (ArtDmx) -> little endian
        packet[8] = 0x00;
        packet[9] = 0x50;

        // Protocol Version -> big endian
        packet[10] = (byte) ((protocolVersion >> 8) & 0xFF);
        packet[11] = (byte) (protocolVersion & 0xFF);

        // Sequence
        packet[12] = (byte) (sequence & 0xFF);

        // Physical
        packet[13] = (byte) (physical & 0xFF);

        // SubUni
        packet[14] = (byte) (subUni & 0xFF);

        // Net
        packet[15] = (byte) (net & 0x7F);

        // Length -> big endian
        packet[16] = (byte) ((length >> 8) & 0xFF);
        packet[17] = (byte) (length & 0xFF);

        // DMX Data
        System.arraycopy(dmxData, 0, packet, 18, length);

        return packet;
    }
}