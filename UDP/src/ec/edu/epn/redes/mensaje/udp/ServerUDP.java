package ec.edu.epn.redes.mensaje.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * A UDP server that runs on port 9091. When a client connects, it sends the
 * client's name, then closes the connection with that client. Arguably just
 * about the simplest server you can write.
 */
public class ServerUDP {

	private static int PORT = 9080;

	/**
	 * Runs the server.
	 */
	public static void main(String[] args) throws IOException {

		DatagramSocket serverSocket = new DatagramSocket(PORT);
		System.err.println("Server listening on port " + PORT + " using UDP connection\n");
		long initialTime = System.currentTimeMillis();
		System.out.println("Tiempo Inicial: " + initialTime + "\n");

		try {
			while (true) {
				// Receive packet
				byte bufferReceive[] = new byte[128];
				DatagramPacket receivePacket = new DatagramPacket(bufferReceive, bufferReceive.length);
				serverSocket.receive(receivePacket);
				InetAddress clientAdress = receivePacket.getAddress();
				int clientPort = receivePacket.getPort();
				System.out.println("Client port: " + clientPort + "\n");

				// Send packet
				byte bufferSend[] = "Darwin".getBytes(); // Cambiar por el
															// nombre del
															// estudiante que
															// corresponda
				DatagramPacket sendPacket = new DatagramPacket(bufferSend, bufferSend.length, clientAdress, clientPort);
				long endTime = System.currentTimeMillis();
				System.out.println("Tiempo Final: " + endTime + "\n");
				long sendTime = endTime - initialTime;
				System.out.println("Envia el paquete en: " + sendTime + " ms\n");
				serverSocket.send(sendPacket);

			}
		} finally {
			serverSocket.close();
		}

	}

}