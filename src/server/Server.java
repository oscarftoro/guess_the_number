package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

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
    public static String numberIn,numberOut, message, currentNumber;
    public static boolean notGuessed = true;

    public static void main(String[] args) throws Exception {
        //generate a number
        generatedNumber = new Double(Math.random() * 100).intValue();
        StringBuffer stringBuffer = new StringBuffer();
        while(notGuessed){
            //try with resources close this objects after use them
            try(DatagramSocket udpSocket = new DatagramSocket(7890);

                ){

                DatagramPacket outPacket = new DatagramPacket(buffer,buffer.length);
                DatagramPacket inPacket = new DatagramPacket(buffer,buffer.length);

                System.out.println("Wellcome to guess the number...I'm the server");
                udpSocket.receive(inPacket); //receive packets from the outside world

                numberIn = new String(inPacket.getData(),0,inPacket.getLength());
                numberOut = new String(outPacket.getData(),0,inPacket.getLength());

                if(numberIn != currentNumber){
                    currentNumber = numberIn;
                    message = checkNumber(numberIn);
                }

                udpSocket.send(outPacket);


            } catch (IOException e){
                System.out.println("Problems with the Socket");
                e.getMessage();

            }

        }
    }

    private static String checkNumber(String receivedNumber){

        try{
            int number = Integer.parseInt(receivedNumber);

            if(number > generatedNumber){
                return " your number is greater than the number I though";
            } else if (number < generatedNumber) {
                return " this number is lower than the number you have to guess";
            } else {

                notGuessed = false;
                return ("you guessed my number!");
            }

        } catch(NumberFormatException e){
            return "Please, enter a valid number";
        }
    }



}
