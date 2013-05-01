package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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
    String fromServer, clientNumber;
    public InetAddress host;
    public Client(int port) throws UnknownHostException{
        this.port = port;
        host = InetAddress.getByName("localhost");
    }

    public void connect() throws IOException{

        try (DatagramSocket udpClientSocket = new DatagramSocket();
             BufferedReader consoleIn = new BufferedReader(
                     new InputStreamReader(System.in));
        ){

            DatagramPacket inPacket = new DatagramPacket(buffer,buffer.length);

            while(true) {

                //prepare the number to send
                System.out.print("guess a number between 0 and 100 :");
                clientNumber = consoleIn.readLine();

                //put the number in packet to deliver
                buffer = clientNumber.getBytes(); //convert String to bytes
                DatagramPacket outPacket = new DatagramPacket(buffer,buffer.length,host,port);

                //send and receive packages
                udpClientSocket.send(outPacket);
                udpClientSocket.receive(inPacket);

                fromServer = new String(inPacket.getData(),0,inPacket.getLength());
                System.out.println("Server says: " + fromServer );

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
