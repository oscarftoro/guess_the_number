package server;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.LinkedList;

/**
 * Created with IntelliJ IDEA.
 * User: oscar
 * Date: 4/21/13
 * Time: 4:01 PM
 *
 */


public class Server {

    public static int generatedNumber;
    public static byte[] buffer = new byte[256];
    public static String messageIn,numberIn, message, currentNumber, clientName, gameMessage,
            gameMessageRest,nameOfTheWinner;
    public static boolean notGuessed = true;
    public static final Splitter SPLITTER = Splitter.on('¤');
    public static LinkedList<Object[]> clientsList = new LinkedList<>();
    public static LinkedList<String> clientsNames = new LinkedList<>();
    //this is the responsible for the framing of our packets
    static Joiner framer = Joiner.on("¤").skipNulls();
    static InetAddress winnerInetAddress;
    static int winnerPort;
    public static void main(String[] args) throws Exception {
        //generate a number
        generatedNumber = new Double(Math.random() * 100).intValue();
        System.out.println("our number: " + generatedNumber);

        while(notGuessed){
            //try with resources close this objects after use them
            try(DatagramSocket udpSocket = new DatagramSocket(7890);


                ){

                System.out.println("Welcome to guess the number...I'm the server");
                DatagramPacket inPacket = new DatagramPacket(buffer,buffer.length);
                udpSocket.receive(inPacket); //receive packets from the outside world
                messageIn = new String(inPacket.getData(),0,inPacket.getLength());

                //I'm using Google common library's Split again, it is just awesome
                //Splitting happens here
                String[] response = Iterables.toArray(SPLITTER.split(messageIn), String.class);

                clientName = response[0];  //first element is the name of the client
                numberIn = response[1]; // second element is  the number

                //add client to the list

                if(!clientsNames.contains(clientName)) {

                   Object[] clientData = new  Object[3];
                   clientData[0] = clientName;
                   clientData[1] =  inPacket.getAddress();
                   clientData[2] =  inPacket.getPort();

                    clientsList.add(clientData);
                    clientsNames.add(clientName);
                }
                System.out.println("clients connected...");
                for(String clientName : clientsNames){
                    System.out.println(clientName); //print the name of connected clients
                }
                //read the protocol - unpack the packet
                System.out.println("client " + clientName + " says: " + numberIn);
                if(numberIn != currentNumber){

                    currentNumber = numberIn;
                    message = checkNumber(clientName,inPacket.getAddress(),inPacket.getPort(), numberIn);
                }
                //prepare to analyze the packet to send
                String[] checkMessage = Iterables.toArray(SPLITTER.split(message), String.class);
                //if first part is "die"
                //broadcast the end of the game
                if(checkMessage[0].equals("die") && clientsList.size() > 0){
                 //gameMessageRest nameOfTheWinner
                    for(Object[] clientData : clientsList){

                        if(clientData[0].equals(nameOfTheWinner)){
                            clientsList.remove(clientData);//remove the winner from the list
                            //send message to the winner  [die][message]
                            gameMessageRest = framer.join("die", "Congratulations "+nameOfTheWinner + "\n you guessed the number" + "\n thanks for participate...");
                            buffer = gameMessageRest.getBytes();
                            DatagramPacket outPacket = new DatagramPacket(buffer,buffer.length, winnerInetAddress,
                                   winnerPort);
                            udpSocket.send(outPacket);//send to every player

                        }
                    }
                    for(Object[] clientData : clientsList){ //broadcast the result
                        gameMessageRest = framer.join("die", nameOfTheWinner + " guessed the number\n thanks for participate...");
                        buffer = gameMessageRest.getBytes();
                        DatagramPacket outPacket = new DatagramPacket(buffer,buffer.length, (InetAddress)clientData[1],
                                (Integer)clientData[2]);
                        udpSocket.send(outPacket);//send to every player

                    }
                }else{

                //prepare the packet
                buffer = message.getBytes();
                DatagramPacket outPacket = new DatagramPacket(buffer,buffer.length, inPacket.getAddress(),
                inPacket.getPort());
                udpSocket.send(outPacket);
                }

            } catch (IOException e){
                System.out.println("Problems with the Socket,probably the port is already taken");
                e.getMessage();
                System.exit(1);

            }

        }
    }
    //THE GAME
    private static String checkNumber(String name,InetAddress address, int port, String receivedNumber){

        try{
            int number = Integer.parseInt(receivedNumber);

            if(number > generatedNumber){
                //frame message to send [order][message]
                gameMessage = framer.join("go"," your number is greater than the number I though");
                return gameMessage;

            } else if (number < generatedNumber) {
                //frame message to send [order][message]
                gameMessage = framer.join("go"," this number is lower than the number you have to guess");
                return gameMessage;

            } else {
                //frame message to send [order][message]
                gameMessage = framer.join("die", "you guessed my number!");

                notGuessed = false; //to stop the loop
                nameOfTheWinner = name;
                winnerInetAddress = address;
                winnerPort = port;

                //print the winner on the server
                System.out.println("And the winner is... " + name);
                return gameMessage;
            }

        } catch(NumberFormatException e){
            return "Please, enter a valid number";
        }
    }



}
