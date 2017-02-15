package com.example.dga_g.clientebroadcast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    //puerto del servidor
    static final int SERVER_PORT = 8080;

    //paneles
    LinearLayout pnlLogin, pnlChat;

    //cuadros de texto
    EditText txtUsuario, txtDireccionIP, txtMensaje;

    //botones
    Button btnConectar, btnEnviar, btnDesconectar;

    //etiquetas
    TextView lblChat, lblPuerto;

    //cadena de texto que contiene el historial del chat
    String strChat;

    //clase principal en la que se realiza la conexion y el envio de datos al servidor
    ChatCliente chatCliente;

    //metodo para asignar las partes de la GUI, inicializarlos e implementar escuchadores
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pnlLogin = (LinearLayout) findViewById(R.id.pnlLogin);
        pnlChat = (LinearLayout) findViewById(R.id.pnlChat);

        txtUsuario = (EditText) findViewById(R.id.txtUsuario);
        txtDireccionIP = (EditText) findViewById(R.id.txtDireccionIP);
        txtMensaje = (EditText) findViewById(R.id.txtMensaje);

        lblPuerto = (TextView) findViewById(R.id.lblPuerto);
        lblPuerto.setText("Puerto: " + SERVER_PORT);
        lblChat = (TextView) findViewById(R.id.chatmsg);

        btnConectar = (Button) findViewById(R.id.btnConectar);
        btnConectar.setOnClickListener(btnConectarListener);
        btnDesconectar = (Button) findViewById(R.id.btnDesconectar);
        btnDesconectar.setOnClickListener(btnDesconectarListener);
        btnEnviar = (Button) findViewById(R.id.btnEnviar);
        btnEnviar.setOnClickListener(btnEnviarListener);
    }

    //escuchador del boton para la conexion
    OnClickListener btnConectarListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            //validamos que ingrese un usuario de usuario
            String usuario = txtUsuario.getText().toString();
            if (usuario.equals("")) {
                Toast.makeText(MainActivity.this, "ERROR: ¡Ingrese el usuario!", Toast.LENGTH_LONG).show();
                return;
            }

            //validamos que se ingrese una direccion IP
            String direccionIP = txtDireccionIP.getText().toString();
            if (direccionIP.equals("")) {
                Toast.makeText(MainActivity.this, "ERROR: ¡Ingrese la direccion IP!", Toast.LENGTH_LONG).show();
                return;
            }

            strChat = "";
            lblChat.setText(strChat);

            pnlLogin.setVisibility(View.GONE);
            pnlChat.setVisibility(View.VISIBLE);

            //inicializamos nuestro objeto con el usuario; la direccion IP y el puerto del servidor broadcast al que nos vamos a conectar
            chatCliente = new ChatCliente(usuario, direccionIP, SERVER_PORT);

            //empezamos el proceso de comunicacion broadcast
            chatCliente.start();
        }

    };

    //escuchador del boton para desconectarnos del broadcast
    OnClickListener btnDesconectarListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (chatCliente == null) {
                return;
            }
            chatCliente.desconectar();
        }
    };

    //escuchador del boton para enviar los mensajes al servidor
    OnClickListener btnEnviarListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if (txtMensaje.getText().toString().equals("")) {
                return;
            }

            if (chatCliente == null) {
                return;
            }

            chatCliente.setMensajeEnviar(txtMensaje.getText().toString() + "\n");
            txtMensaje.setText("");
        }

    };

    //clase que establece la conexion y el envio de mensajes
    private class ChatCliente extends Thread {

        String usuario;
        String direccionIP;
        int puerto;
        String mensajeEnviar;
        boolean salir;

        ChatCliente(String usuario, String direccionIP, int puerto) {
            this.usuario = usuario;
            this.direccionIP = direccionIP;
            this.puerto = puerto;
            this.mensajeEnviar = "";
            this.salir = false;
        }

        //proceso que se ejecuta durante la conexion broadcast
        @Override
        public void run() {
            Socket socketCliente = null;
            DataOutputStream outputStream = null;
            DataInputStream inputStream = null;

            try {
                //creamos un socket de conexion con la direccion ip y el puerto del servidor
                socketCliente = new Socket(direccionIP, puerto);
                outputStream = new DataOutputStream(socketCliente.getOutputStream());
                inputStream = new DataInputStream(socketCliente.getInputStream());
                outputStream.writeUTF(usuario);
                outputStream.flush();

                //ciclo en el que se ejecutara mientras el usuario decida desconectarse del broadcast
                while (!salir) {
                    if (inputStream.available() > 0) {
                        //string que guarda el historial leido en el chat
                        strChat += inputStream.readUTF();

                        //escribimos en la gui el texto que escribe el cliente
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //Toast.makeText(MainActivity.this, strChat, Toast.LENGTH_LONG).show();
                                lblChat.setText(strChat);
                            }
                        });
                    }

                    if (!mensajeEnviar.equals("")) {
                        outputStream.writeUTF(mensajeEnviar);
                        outputStream.flush();
                        mensajeEnviar = "";
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
                final String eString = e.toString();
                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, eString, Toast.LENGTH_LONG).show();
                    }

                });
            } finally {
                if (socketCliente != null) {
                    try {
                        socketCliente.close();
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

                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                MainActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        pnlLogin.setVisibility(View.VISIBLE);
                        pnlChat.setVisibility(View.GONE);
                    }

                });
            }

        }

        private void setMensajeEnviar(String mensajeEnviar) {
            this.mensajeEnviar = mensajeEnviar;
        }

        private void desconectar() {
            this.salir = true;
        }
    }

}
