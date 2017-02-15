package com.example.dga_g.servidorbroadcast;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;

import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import java.net.ServerSocket;

public class MainActivity extends AppCompatActivity {

    //puerto del servidor
    static final int SERVER_PORT = 8080;

    //etiquetas
    TextView lblDireccionIP, lblPuerto, lblChat;

    //cadena de texto que contiene el historial del chat
    String strChat = "";

    //lista de todos los clientes conectados
    List<ChatCliente> listaUsuarios;


    ServerSocket socketServidor;

    //metodo para asignar e inicializar las partes de la GUI
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lblDireccionIP = (TextView) findViewById(R.id.lblDireccionIP);
        lblDireccionIP.setText(getDireccionIP());
        lblPuerto = (TextView) findViewById(R.id.lblPuerto);
        lblChat = (TextView) findViewById(R.id.chatmsg);

        listaUsuarios = new ArrayList<>();

        //creamos nuestro objeto de chat servidor sin parametros (no requiere)
        ChatServidor chatServidor = new ChatServidor();

        //empezamos el proceso de escucha del servidor broadcast
        chatServidor.start();
    }

    //metodo para cerrar el socket broadcast del servidor (cuando se cierra la app)
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (socketServidor != null) {
            try {
                socketServidor.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ChatServidor extends Thread {

        //proceso que se ejecuta mientras este abierta la app broadcast
        @Override
        public void run() {
            Socket socketServer = null;

            try {
                //creamos un socket para el servidor con su respectivo puerto
                socketServidor = new ServerSocket(SERVER_PORT);

                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //puerto en el que el servidor se encuentra escuchando
                        lblPuerto.setText("Puerto: " + socketServidor.getLocalPort());
                    }
                });

                while (true) {
                    socketServer = socketServidor.accept();
                    //creamos un nuevo cliente cada vez que el socket detecte una nueva conexion
                    ChatCliente chatCliente = new ChatCliente();
                    //agregamos al cliente a la lista de clientes broadcast
                    listaUsuarios.add(chatCliente);
                    //realizamos un nuevo proceso para cada cliente nuevo que ingrese
                    ConexionThread conexionThread = new ConexionThread(chatCliente, socketServer);
                    //arrancamos con un nuevo hilo para cada cliente
                    conexionThread.start();
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (socketServer != null) {
                    try {
                        socketServer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private class ConexionThread extends Thread {

        Socket socket;
        ChatCliente chatCliente;
        String mensajeEnviar = "";

        //constructor
        ConexionThread(ChatCliente chatCliente, Socket socket){
            this.chatCliente = chatCliente;
            this.socket = socket;

            chatCliente.socketCliente = socket;
            chatCliente.conexionThread = this;
        }

        @Override
        public void run(){
            DataInputStream inputStream = null;
            DataOutputStream outputStream = null;

            try {
                inputStream = new DataInputStream(socket.getInputStream());
                outputStream = new DataOutputStream(socket.getOutputStream());

                //copiamos el nombre del usuario conectado
                String usuario = inputStream.readUTF();

                chatCliente.usuario = usuario;

                //escribimos en la gui el usuario que se ha conectado con su direccion IP y su puerto
                strChat += "@"+ chatCliente.usuario + " conectado" + chatCliente.socketCliente.getInetAddress() + ":" + chatCliente.socketCliente.getPort() + "\n";
                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        lblChat.setText(strChat);
                    }
                });

                outputStream.writeUTF("Bienvenido @" + usuario + "\n");
                outputStream.flush();

                //enviamos a nuestra lista de usuarios conectados en el broadcast
                mensajeBroadcast("@"+ usuario + " se unio al chat.\n");

                //recibimos lo que escribe cada cliente en nuestr
                while(true){
                    if (inputStream.available() > 0) {
                        String nuevoMensaje = inputStream.readUTF();

                        //escribimos en la gui lo que escribe cada usuario
                        strChat += usuario + ": " + nuevoMensaje;
                        MainActivity.this.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                lblChat.setText(strChat);
                            }
                        });

                        //enviamos a todos los usuarios conectados en el broadcast
                        mensajeBroadcast(usuario + ": " + nuevoMensaje);
                    }

                    if(!mensajeEnviar.equals("")){
                        outputStream.writeUTF(mensajeEnviar);
                        outputStream.flush();
                        mensajeEnviar = "";
                    }

                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                //eliminamos al cliente que se ha desconectado
                listaUsuarios.remove(chatCliente);
                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, chatCliente.usuario + " removido.", Toast.LENGTH_LONG).show();
                        strChat += "-- " + chatCliente.usuario + " salió\n";

                        MainActivity.this.runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                lblChat.setText(strChat);
                            }
                        });

                        mensajeBroadcast("-- " + chatCliente.usuario + " salió\n");
                    }
                });
            }

        }

        private void setMensajeEnviar(String mensajeEnviar){
            this.mensajeEnviar = mensajeEnviar;
        }

    }

    //metodo que envia un mensaje a todos los clientes conectados al broadcast
    private void mensajeBroadcast(String mensajeEnviar){
        for(int i = 0; i< listaUsuarios.size(); i++){
            listaUsuarios.get(i).conexionThread.setMensajeEnviar(mensajeEnviar);
            strChat += "- enviar a @" + listaUsuarios.get(i).usuario + "\n";
        }

        //metodo para escribir en la gui el historial del chat
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                lblChat.setText(strChat);
            }
        });
    }

    //metodo que devuelve la direccion ip de cualquier host
    private String getDireccionIP() {
        String direccionIP = "";
        try {
            //creamos un enumerador para recorrer todas la interfaces de red
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces();

            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces.nextElement();
                //creamos un nuevo enumerator para buscar la direccion ipv4 en nuetra lista de redes
                Enumeration<InetAddress> enumInetAddress = networkInterface.getInetAddresses();

                //buscamos nuestra direccion ip requerida en cada interfaz de red
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();
                    if (inetAddress.isSiteLocalAddress()) {
                        direccionIP += "Direccion IP: " + inetAddress.getHostAddress() + "\n";
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
            direccionIP += "Algo salió mal! " + e.toString() + "\n";
        }
        return direccionIP;
    }

    private class ChatCliente {
        String usuario;
        Socket socketCliente;
        ConexionThread conexionThread;
    }

}
