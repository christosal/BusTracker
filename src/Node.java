import java.io.*;
import java.net.*;
import java.security.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.*;
import javax.xml.bind.DatatypeConverter;

public class Node {

    public static HashMap<Integer,Broker> local_brokers  = new HashMap<>(); // <BrokerID,Broker>
    public static int flagForBrokers = 0; //Flag to know when all brokers are running
    public static String MACHINE_IP = null;


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
    /**
     * Returns a free port number on localhost.
     *
     * Heavily inspired from org.eclipse.jdt.launching.SocketUtil (to avoid a dependency to JDT just because of this).
     * Slightly improved with close() missing in JDT. And throws exception instead of returning -1.
     *
     * @return a free port number on localhost
     * @throws IllegalStateException if unable to find a free port
     */
    public static int findFreePort() {
        ServerSocket socket = null;
        try {
            socket = new ServerSocket(0);
            socket.setReuseAddress(true);
            int port = socket.getLocalPort();
            try {
                socket.close();
            } catch (IOException e) {
                // Ignore IOException on close()
            }
            return port;
        } catch (IOException e) {
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                }
            }
        }
        throw new IllegalStateException("Could not find a free TCP/IP port to start embedded Jetty HTTP Server on");
    }


    public static void setLocalIP(){

        ArrayList<InetAddress> addresses = new ArrayList<>();
        try{
            Enumeration e = NetworkInterface.getNetworkInterfaces();
            while(e.hasMoreElements())
            {
                NetworkInterface n = (NetworkInterface) e.nextElement();
                Enumeration ee = n.getInetAddresses();

                while (ee.hasMoreElements())
                {
                    addresses.add((InetAddress) ee.nextElement());
                }
            }

        }catch (Exception e){

        }
        int counter=1;
        System.out.println("Please choose the correct ip addresses of the below network interfaces:");
        for (InetAddress a:addresses){
            System.out.println(counter+") "+a);
            counter++;
        }
        System.out.print("Choose a number: ");
        Scanner scan= new Scanner(System.in);
        String choosenNumber = scan.nextLine();
        MACHINE_IP = addresses.get(Integer.parseInt(choosenNumber)-1).getHostAddress();
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
