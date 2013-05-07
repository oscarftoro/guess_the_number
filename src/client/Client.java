package client;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

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
    String fromServer, clientNumber, clientName, packetTodeliver,messageFromServer;
    public InetAddress host;
    boolean youNotGuessTheNumber = true;
    BufferedReader consoleIn;
    Splitter splitter = Splitter.on('¤');
    String[] responseFromServer;
    public Client(int port) throws UnknownHostException{
        this.port = port;
        host = InetAddress.getByName("localhost");

    }

    public void connect() throws IOException{
        class Operator {
            //this is the responsible for the framing of our packet
            Joiner framer = Joiner.on("¤").skipNulls();


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
                    //get the number from the client
                    clientNumber = bufferedReader.readLine();

                    //pack the name of user with the number
                    packetTodeliver = framer.join(clientName,clientNumber);

                } catch (IOException e) {
                    e.printStackTrace();
                }

                //put the framed data  in a packet of bytes to deliver
                buffer = packetTodeliver.getBytes(); //convert String to bytes
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
                //array with the response from the server
                responseFromServer =  Iterables.toArray(splitter.split(fromServer), String.class);

            }

            public void informWinner(){

                System.out.println("Server says: " + responseFromServer[1] );

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

                        //if you do not guess the number and the first element of the packet is not "die"
                   } else if (fromServer != null && !responseFromServer[0].equals("die")) {
                       //responseFromServer

                       messageFromServer = responseFromServer[1];
                       //print received info
                       System.out.println("Server says: " + messageFromServer );

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
