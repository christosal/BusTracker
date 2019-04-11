import java.io.*;
import java.net.*;
import java.util.*;

public class Broker extends Node implements Runnable,Serializable {
    private int port;
    private String IPv4;
    private int brokerID;
    private ArrayList<Publisher> registeredPublishers = new ArrayList<>();
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

    public Broker(int port,int brokerID){
        this.port=port;
        this.brokerID=brokerID;
    }

    public void calculateKeys(){
        Reader.readFiles();
        String broker_1_HashIP = sha1(local_brokers.get(1).getIPv4()+local_brokers.get(1).getPort());
        String broker_2_HashIP = sha1(local_brokers.get(2).getIPv4()+local_brokers.get(2).getPort());
        String broker_3_HashIP = sha1(local_brokers.get(3).getIPv4()+local_brokers.get(3).getPort());
        for (Topic topic:Reader.Topics){
            String hashedTopic = sha1(topic.getBusLine());
            if (hashedTopic.compareTo(broker_1_HashIP)<0){
                local_brokers.get(1).ResponsibilityLines.add(topic);
            }else if(hashedTopic.compareTo(broker_2_HashIP)<0){
                local_brokers.get(2).ResponsibilityLines.add(topic);
            }else{
                local_brokers.get(3).ResponsibilityLines.add(topic);
            }
        }

        for (Topic topic:local_brokers.get(1).ResponsibilityLines){
            System.out.print(topic.getBusLine()+",");
        }
        System.out.println("");
        for (Topic topic:local_brokers.get(2).ResponsibilityLines){
            System.out.print(topic.getBusLine()+",");
        }
        System.out.println("");
        for (Topic topic:local_brokers.get(3).ResponsibilityLines){
            System.out.print(topic.getBusLine()+",");
        }
    }

    public void init(int serverPort,int numberOfBrokers) {
        ServerSocket providerSocket=null;
        Socket connection=null;
        try{
            providerSocket = new ServerSocket(serverPort);
            DatagramSocket socket = new DatagramSocket();
            IPv4=InetAddress.getLocalHost().getHostAddress();
            System.out.println("Broker"+this.getBrokerID()+":"+ this.getIPv4() +":"+serverPort+" is listening...");
            //Adds broker to brokers List
            local_brokers.put(this.getBrokerID(),this);
            flagForBrokers++;
            if (flagForBrokers==numberOfBrokers){
                System.out.println("---STATUS INFO--- \"All Brokers of address \'"+ this.getIPv4()+"\' are running\"");
                startClient(8085,local_brokers);
            }
            while (true) {
                connection = providerSocket.accept();

                ObjectOutputStream out = new ObjectOutputStream(connection.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(connection.getInputStream());

                System.out.println(in.readUTF());
                //System.out.println((Message) in.readUnshared());
                // System.out.println((Message) in.readObject());

                in.close();
                out.close();
                connection.close();
            }
        }catch (IOException ioException){
            ioException.printStackTrace();
        }finally {
            try {
                DatagramSocket socket = new DatagramSocket();
                providerSocket.close();
                System.out.println("Server:"+socket.getLocalAddress()+":"+serverPort+" is listening..");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    public void startClient(int port,Object object) {
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


    @Override
    public void run() {
        this.init(port,3);
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

    public int getBrokerID() {
        return brokerID;
    }

    public ArrayList<Publisher> getRegisteredPublishers() {
        return registeredPublishers;
    }

    public ArrayList<Topic> getResponsibilityLines() {
        return ResponsibilityLines;
    }

    //End of Getters Setters

}


