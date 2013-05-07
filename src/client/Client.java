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

    //Variables necessary to operate
    DatagramPacket packet;
    static byte[] buffer = new byte[256];
    int port = 7890;
    String fromServer, clientNumber, clientName;
    public InetAddress host;
    boolean youNotGuessTheNumber = true;
    BufferedReader consoleIn;


    public Client(int port) throws UnknownHostException{
        this.port = port;
        host = InetAddress.getByName("localhost");

    }

    public void connect() throws IOException{
        class Operator {

            BufferedReader bufferedReader;
            DatagramSocket datagramSocket;
            DatagramPacket datagramPacket;

            Operator(DatagramSocket datagramSocket, BufferedReader bufferedReader,
                     DatagramPacket datagramPacket){

                this.bufferedReader = bufferedReader;
                this.datagramSocket = datagramSocket;
                this.datagramPacket = datagramPacket;

            }

            public void welcome(){

                System.out.println("Welcome to guess the number");
                System.out.println("What is your name? :");
                try {
                    clientName = bufferedReader.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            public void prepareNumber(){
                //prepare the number to send
                System.out.print("guess a number between 0 and 100 :");
                try {
                    clientNumber = bufferedReader.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                //put the number in packet to deliver
                buffer = clientNumber.getBytes(); //convert String to bytes
                DatagramPacket outPacket = new DatagramPacket(buffer,buffer.length,host,port);

                //send and receive packages
                try {
                    datagramSocket.send(outPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    datagramSocket.receive(datagramPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                fromServer = new String(datagramPacket.getData(),0,datagramPacket.getLength());

            }

            public void informWinner(){
                System.out.println("Server says: " + fromServer );
                System.out.println("You won");
                youNotGuessTheNumber = false;
            }
        }
        //try with resources close the objects after use it, cool stuff from Java 7
        try (DatagramSocket udpClientSocket = new DatagramSocket();
             BufferedReader consoleIn = new BufferedReader(
                     new InputStreamReader(System.in));
        ){

            DatagramPacket inPacket = new DatagramPacket(buffer,buffer.length);
            Operator operator = new Operator(udpClientSocket, consoleIn,inPacket);
            while(youNotGuessTheNumber) {

               if(clientName == null){
                operator.welcome();

               }else {

                   //first time
                   if(fromServer == null){
                       operator.prepareNumber();

                       //if you do not guess the number
                   } else if (fromServer != null && !fromServer.equals("you guessed my number!")) {

                       //print received info
                       System.out.println("Server says: " + fromServer );

                       //Try again and send to the server
                       //prepare the number to send
                       operator.prepareNumber();

                   } else {
                       operator.informWinner();
                   }
               }
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
