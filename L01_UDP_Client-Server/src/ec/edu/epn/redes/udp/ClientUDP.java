package ec.edu.epn.redes.udp;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import javax.swing.JOptionPane;

public class ClientUDP {

	private static int SERVER_PORT = 9091;

	public static void main(String[] args) throws IOException {

		String serverAddress = JOptionPane.showInputDialog("Ingrese la direcciòn IP de la maquina que esta \n"
				+ "Corriendo el servicio en el puerto " + SERVER_PORT);
		// Envio del paquete (REQUEST)
		// UDP en java usando SOCKETS
		// DatagramSocket --> UDP en java
		DatagramSocket clientSocket = new DatagramSocket();
		byte bufferSent[] = serverAddress.getBytes();
		DatagramPacket sentPacket = new DatagramPacket(bufferSent, bufferSent.length,
				InetAddress.getByName(serverAddress), SERVER_PORT);
		clientSocket.send(sentPacket);

		// Recibir paquete
		byte bufferRecive[] = new byte[128];
		DatagramPacket recivePacket = new DatagramPacket(bufferRecive, bufferRecive.length);
		clientSocket.receive(recivePacket);// La capa de transporte entrega el
											// paquete

		// Transformamos los byte a string
		InputStream myInputStream = new ByteArrayInputStream(recivePacket.getData());
		BufferedReader input = new BufferedReader(new InputStreamReader(myInputStream));
		String answer = input.readLine();

		// Desplegar mensaje
		JOptionPane.showMessageDialog(null, answer);
		clientSocket.close();
		System.exit(0);

	}
}
