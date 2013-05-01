package client;

import java.net.UnknownHostException;

/**
 * Created with IntelliJ IDEA.
 * User: oscar
 * Date: 4/30/13
 * Time: 8:16 PM
 */
public class StartClient {

    public static void main(String[] args) {

        Client client = null;
        try {
            client = new Client(7890);
        } catch (UnknownHostException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        client.start();
    }
}
