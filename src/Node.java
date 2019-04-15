import java.io.*;
import java.net.*;
import java.security.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.logging.*;
import javax.xml.bind.DatatypeConverter;

public class Node {

    public static HashMap<Integer,Broker> local_brokers  = new HashMap<>(); // <BrokerID,Broker>
    public static int flagForBrokers = 0; //Flag to know when all brokers are running



    /*public void startClient(int port,Object object) {
        Socket requestSocket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        String message;
        try {
            requestSocket = new Socket(InetAddress.getByName(Reader.readBrokerIP()), port);

            out = new ObjectOutputStream(requestSocket.getOutputStream());
            in = new ObjectInputStream(requestSocket.getInputStream());

            out.writeObject(object);
            out.flush();

            //out.writeUnshared(new Message(101, "test data")); // Stelnei antikeimena, kaluterh apo writeObject
            // out.writeObject(new Message(101, "test data")); // Stelnei antikeimena
            //out.flush();
        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
                requestSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }*/

    public static String getLocalIP(){
        InetAddress i=null;
        try{
            Enumeration e = NetworkInterface.getNetworkInterfaces();
            while(e.hasMoreElements())
            {
                NetworkInterface n = (NetworkInterface) e.nextElement();
                Enumeration ee = n.getInetAddresses();

                while (ee.hasMoreElements())
                {
                    i = (InetAddress) ee.nextElement();
                }
            }

        }catch (Exception e){

        }
        return i.getHostAddress();
    }

    /**
     * Hashing with SHA1
     *
     * @param input String to hash
     * @return String hashed
     */
    public static String sha1(String input) {
        String sha1 = null;
        try {
            MessageDigest msdDigest = MessageDigest.getInstance("SHA-1");
            msdDigest.update(input.getBytes("UTF-8"), 0, input.length());
            sha1 = DatatypeConverter.printHexBinary(msdDigest.digest());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sha1;
    }

}
