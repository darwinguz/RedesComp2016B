package ec.edu.epn.redes.tcp;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.swing.JOptionPane;

public class ClientTCP {

	private static int SERVER_PORT = 9090;

	public static void main(String[] args) throws UnknownHostException, IOException {

		String serverAddress = JOptionPane.showInputDialog(
				"Ingrese direccion IP de la máquina que esta\n" + "corriendo el servidor en el puerto: " + SERVER_PORT);

		String datosSuma = JOptionPane.showInputDialog("Ingrese una suma ejemplo: a+b");

		Socket clientSocket = new Socket(serverAddress, SERVER_PORT);

		// obtener el paquete que me envia el Servidor
		InputStreamReader inputSream = new InputStreamReader(clientSocket.getInputStream());

		// leyendo el mensaje
		BufferedReader input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		// nos permite enviar datos al servidor destino
		DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

		// enviamos el mensaje codificado en UTF
		out.writeUTF(datosSuma);

		String answer = input.readLine();

		JOptionPane.showMessageDialog(null, answer);

		System.exit(0);
	}
}