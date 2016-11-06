package ec.edu.epn.redes.mensaje.udp;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import javax.swing.JOptionPane;

/**
 * Trivial client for the date server.
 */
public class ClientUDP {

	private static int SERVER_PORT = 9090;

	/**
	 * Runs the client as an application. First it displays a dialog box asking
	 * for the IP address or hostname of a host running the date server, then
	 * connects to it and displays the data that the server send.
	 */

	public static void main(String[] args) throws IOException {

		String serverAddress = JOptionPane.showInputDialog(
				"Enter IP Address of a machine that is\n" + "running the date service on port " + SERVER_PORT + ":");

		// Send packet (Request)
		DatagramSocket clientSocket = new DatagramSocket();
		byte bufferSend[] = serverAddress.getBytes();
		DatagramPacket sendPacket = new DatagramPacket(bufferSend, bufferSend.length,
				InetAddress.getByName(serverAddress), SERVER_PORT);
		clientSocket.send(sendPacket);

		// Receive packet
		byte bufferReceive[] = new byte[128];
		DatagramPacket receivePacket = new DatagramPacket(bufferReceive, bufferReceive.length);
		clientSocket.receive(receivePacket);

		// Transforma bytes a String
		InputStream myInputStream = new ByteArrayInputStream(receivePacket.getData());
		BufferedReader input = new BufferedReader(new InputStreamReader(myInputStream));
		String answer = input.readLine();

		// Despliega mensaje
		JOptionPane.showMessageDialog(null, answer);
		clientSocket.close();
		System.exit(0);
	}

}