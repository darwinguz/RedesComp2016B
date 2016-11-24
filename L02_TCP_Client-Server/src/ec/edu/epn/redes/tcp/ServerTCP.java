package ec.edu.epn.redes.tcp;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.StringTokenizer;

public class ServerTCP {
	private static int PORT = 9090;

	public static void main(String argv[]) throws IOException {
		ServerSocket serverSocket = new ServerSocket(PORT);
		System.out.println("Servidor escuchando en puerto: " + PORT);

		try {
			while (true) {
				Socket socketCliente = serverSocket.accept();
				// recibimoa los datos del cliente en este caso: a+b
				DataInputStream in = new DataInputStream(socketCliente.getInputStream());

				StringTokenizer tokens = new StringTokenizer(in.readUTF(), "+");
				int a = Integer.parseInt(tokens.nextToken());
				int b = Integer.parseInt(tokens.nextToken());
				int c = a + b;
				
				PrintWriter out = new PrintWriter(socketCliente.getOutputStream(), true);
				out.println(a + "+" + b + "=" + c);
				System.out.println("Conexion establecida con: " + socketCliente.toString());
			}
		} finally {
			serverSocket.close();
		}
	}
}