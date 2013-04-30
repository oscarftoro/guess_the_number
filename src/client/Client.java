package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.UnknownHostException;

/**
 * Created with IntelliJ IDEA.
 * User: oscar
 * Date: 4/21/13
 * Time: 4:34 PM
 */
public class Client extends Thread {

    DatagramPacket packet;
    static byte[] buffer = new byte[256];
    int port = 7890;
    String numberOut, fromServer;

    public Client(int port){
        this.port = port;
    }

    public void connect() throws IOException{

        try (DatagramSocket udpClientSocket = new DatagramSocket(port);
             PrintWriter out = new PrintWriter(numberOut);
             BufferedReader consoleIn = new BufferedReader(
                     new InputStreamReader(System.in));
        ){

            DatagramPacket outPacket = new DatagramPacket(buffer,buffer.length);
            DatagramPacket inPacket = new DatagramPacket(buffer,buffer.length);

            fromServer = new String(inPacket.getData(),0,inPacket.getLength());
            numberOut = new String(outPacket.getData(),0,inPacket.getLength());
            while(fromServer != null) {
                udpClientSocket.receive(inPacket);


                udpClientSocket.send(outPacket);
            }

        } catch (IOException e){
            System.out.println("Socket error. Was not possible to connect with the server");
            e.getMessage();
        }

    }

    @Override
    public void run() {
        try {
            connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}
