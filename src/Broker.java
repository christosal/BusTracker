import java.io.*;
import java.net.*;
import java.util.*;

public class Broker extends Node implements Runnable,Serializable {
    private int port;
    private String IPv4;
    private int brokerID;
    private boolean brokerIsRunning =false; //flag that indicates if a broker is running or not
    private ArrayList<Publisher> registeredPublishers = new ArrayList<>();
    private ArrayList<Subscriber> registeredSubscribers = new ArrayList<>();
    private ArrayList<Topic> ResponsibilityLines = new ArrayList<>();

    public static void main(String args[]) {

            Broker broker1 = new Broker(8080,1);
            Broker broker2 = new Broker(8083,2);
            Broker broker3 = new Broker(3001,3);

            Thread t1 = new Thread(broker1);
            Thread t2 = new Thread(broker2);
            Thread t3 = new Thread(broker3);

            t1.start();
            t2.start();
            t3.start();

            try {
                t1.join();
                t2.join();
                t3.join();
            } catch (Exception e) {
                System.out.println("Threads Interrupted");
            }


    }

    public Broker(){}

    public Broker(int port,int brokerID){
        this.port=port;
        this.brokerID=brokerID;
    }



    public void initBroker(int serverPort,int numberOfBrokers) {
        ServerSocket brokerSocket=null;
        try{
            brokerSocket = new ServerSocket(serverPort);
            brokerIsRunning=true;
            DatagramSocket socket = new DatagramSocket();
            IPv4=InetAddress.getLocalHost().getHostAddress();
            System.out.println("Broker"+this.getBrokerID()+":"+ this.getIPv4() +":"+serverPort+" is listening...");
            //Adds broker to brokers List
            local_brokers.put(this.getBrokerID(),this);
            flagForBrokers++;
            if (flagForBrokers==numberOfBrokers){
                System.out.println("---STATUS INFO--- \"All Brokers of address \'"+ this.getIPv4()+"\' are running\"");
                flagForBrokers=0;
                connectToMasterServer(8085,local_brokers);
            }
            while (brokerIsRunning) {
                new ClientHandler(brokerSocket.accept(),this).start();
            }
        }catch (IOException ioException){
            ioException.printStackTrace();
        }
    }

    public void connectToMasterServer(int port,Object object) {
        Socket requestSocket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        String message;
        try {
            requestSocket = new Socket(Reader.readBrokerIP(), port);

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
    }


    private static class ClientHandler  extends Thread {
        private Socket clientSocket;
        private Broker parentBroker;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private boolean clientHandlerIsRunning =false; //flag that indicates if a handler for each connection is running or not

        public ClientHandler(Socket socket,Broker parentBroker) {
            this.clientSocket = socket;
            this.parentBroker = parentBroker;
        }

        public void run() {
            System.out.println("Broker:"+parentBroker.getBrokerID()+"--> A new Subscriber connected");
            try {
                out = new ObjectOutputStream(clientSocket.getOutputStream());
                in = new ObjectInputStream(clientSocket.getInputStream());
                while(clientHandlerIsRunning){

                    Object recievedObject = in.readObject();

                    if (recievedObject instanceof Subscriber){
                        parentBroker.getRegisteredSubscribers().add((Subscriber) recievedObject);

                    }
                }
                in.close();
                out.close();
                clientSocket.close();

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void run() {
        this.initBroker(port,3);
    }


    //Getters Setters

    public int getPort() {
        return port;
    }

    public String getIPv4() {
        return IPv4;
    }

    public void setIPv4(String IPv4) {
        this.IPv4 = IPv4;
    }

    public ArrayList<Publisher> getRegisteredPublishers() {
        return registeredPublishers;
    }

    public ArrayList<Subscriber> getRegisteredSubscribers() {
        return registeredSubscribers;
    }

    public int getBrokerID() {
        return brokerID;
    }

    public ArrayList<Topic> getResponsibilityLines() {
        return ResponsibilityLines;
    }

    //End of Getters Setters

}


