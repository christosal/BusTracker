import java.io.*;
import java.net.*;
import java.util.*;

public class Broker extends Node implements Runnable,Serializable {
    private int port;
    private String IPv4;
    private int brokerID;
    private static String numberOfBrokers;
    private boolean brokerIsRunning =false; //flag that indicates if a broker is running or not
    private HashMap<Integer,ClientHandler> registeredPublishers = new HashMap<>();
    private HashMap<Integer,ClientHandler> registeredSubscribers = new HashMap<>();
    private ArrayList<Topic> ResponsibilityLines = new ArrayList<>();

    public static void main(String args[]) {

        setLocalIP();
        System.out.print("Give number of brokers:");
            Scanner scan= new Scanner(System.in);
            numberOfBrokers = scan.nextLine();
            for (int i =0; i<Integer.parseInt(numberOfBrokers);i++){
                Broker broker = new Broker(findFreePort(),i);
                Thread t = new Thread(broker);
                t.start();
            }

            //Broker broker1 = new Broker(8080,1);
            //Broker broker2 = new Broker(8083,2);


            //Thread t1 = new Thread(broker1);
            //Thread t2 = new Thread(broker2);


           // t1.start();
           // t2.start();


            //try {
            //    t1.join();
            //    t2.join();
           // } catch (Exception e) {
            //    System.out.println("Threads Interrupted");
           // }




    }




    public Broker(){}

    public Broker(int port,int brokerID){
        this.port=port;
        this.brokerID=brokerID;
    }


    /*
    * Initializes each Broker.
    * It also takes the number of Brokers (it can be replaced with a global counter* but its working ;) )
    * For each new connection, a new Thread (ClientHandler) is created in order to serve each Client
    *
    * * */
    public void initBroker(int serverPort,int numberOfBrokers) {
        ServerSocket brokerSocket=null;
        try{
            brokerSocket = new ServerSocket(serverPort);
            Socket clientSocket;
            brokerIsRunning=true;
            DatagramSocket socket = new DatagramSocket();
            IPv4=MACHINE_IP;
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
                clientSocket = brokerSocket.accept();
                System.out.println("Broker"+this.getBrokerID()+": "+this.getIPv4()+":"+this.getPort()+"---> New client request received : " + clientSocket);

                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());

                System.out.println("Creating a new handler for a client...");

                new Thread(new ClientHandler(clientSocket,this,in,out)).start();

            }
        }catch (IOException ioException){
            ioException.printStackTrace();
        }
    }


    /*
    * Connects to MasterServer in order to send him all the information
    *
    * */
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



    /*
    * Thread that executes on every new connection on the Broker
    * It's responsible to keep the connection with the Subscribers and the Publishers (it receives a Value Object) until they finish
    * It also handles force-stops of each connection -> EOFExceptions
    * */
    private static class ClientHandler  implements Runnable {
        private Socket clientSocket;
        private Broker parentBroker;
        private ObjectOutputStream out;
        private ObjectInputStream in;
        private Subscriber myClientSubscriber=null; //Stores a Subscriber object in order the ClientHandler to be able to handle the connections
        private Value myClientValue=null; //Stores a Value object in order the ClientHandler to be able to handle the connections
        private static int GlobalID;
        private int ClientID;
        private boolean clientHandlerIsRunning =false; //flag that indicates if a handler for each connection is running or not

        public ClientHandler(Socket socket,Broker parentBroker,ObjectInputStream in,ObjectOutputStream out) {
            this.clientSocket = socket;
            this.parentBroker = parentBroker;
            this.in=in;
            this.out=out;
            this.ClientID=GlobalID++;
            clientHandlerIsRunning=true;
        }

        public void run() {

            while(clientHandlerIsRunning){
                try {
                    Object recievedObject = in.readObject();
                    if (recievedObject instanceof Subscriber) {
                        myClientSubscriber= (Subscriber) recievedObject;
                        parentBroker.getRegisteredSubscribers().put(this.ClientID,this);
                        System.out.println("Broker"+parentBroker.getBrokerID()+": "+parentBroker.getIPv4()+":"+parentBroker.getPort()+"---> A new subscriber (#"+this.ClientID+") with topic("+  myClientSubscriber.getPreferedTopic().getBusLine()+") added to list");
                    }else if (recievedObject instanceof Value){
                        myClientValue = (Value) recievedObject;
                        if (!checkIfExistsInPubs(this.ClientID)){
                            parentBroker.getRegisteredPublishers().put(this.ClientID,this);
                            System.out.println("Broker"+parentBroker.getBrokerID()+": "+parentBroker.getIPv4()+":"+parentBroker.getPort()+"---> A new publisher (#"+this.ClientID+") with vechicleID("+myClientValue.getBus().getVechicleId() +") and topic("+  myClientValue.getBus().getBusLineId()+") added to list");
                        }
                        pull(myClientValue);
                    }else if(recievedObject==null){
                        //If pubslisher stops transmiting sends a null object
                        System.out.println("Broker"+parentBroker.getBrokerID()+": "+parentBroker.getIPv4()+":"+parentBroker.getPort()+"---> A publisher (#"+this.ClientID+") with vechicleID("+myClientValue.getBus().getVechicleId() +") and topic("+  myClientValue.getBus().getBusLineId()+") stopped transmitting and removed from list");
                        clientHandlerIsRunning=false;
                        pull(myClientValue,"sendStop");
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }catch (IOException e){
                    if (this.myClientSubscriber!=null){
                        for (Integer brokerid : parentBroker.getRegisteredSubscribers().keySet()) {
                            if (parentBroker.getRegisteredSubscribers().get(brokerid).ClientID==this.ClientID){
                                System.out.println("Broker"+parentBroker.getBrokerID()+": "+parentBroker.getIPv4()+":"+parentBroker.getPort()+"---> A subscriber (#"+this.ClientID+") with topic("+parentBroker.getRegisteredSubscribers().get(brokerid).myClientSubscriber.getPreferedTopic().getBusLine()+") disconnected and removed from list");
                                parentBroker.getRegisteredSubscribers().remove(brokerid);
                                clientHandlerIsRunning=false;
                                break;
                            }
                        }
                    }
                    else if (this.myClientValue!=null){
                        for (Integer id : parentBroker.getRegisteredPublishers().keySet()) {
                            if (parentBroker.getRegisteredPublishers().get(id).ClientID==this.ClientID){
                                System.out.println("Broker"+parentBroker.getBrokerID()+": "+parentBroker.getIPv4()+":"+parentBroker.getPort()+"---> A publisher (#"+this.ClientID+") with vechicleID("+myClientValue.getBus().getVechicleId() +") and topic("+  myClientValue.getBus().getBusLineId()+") disconnected and removed from list");
                                pull(myClientValue,"");
                                clientHandlerIsRunning=false;
                                break;
                            }
                        }
                    }

                }


            }

            try{
                this.in.close();
                this.out.close();
                clientSocket.close();
            }catch (IOException e){
                e.printStackTrace();
            }

        }

        /*
        * Is responsible to forward to all subscribers values for the topic they are registered for
        *
        * */
        private synchronized void pull(Value value){
            for (Integer id:parentBroker.getRegisteredSubscribers().keySet()){
                if (value.getBus().getBusLineId().equals(parentBroker.getRegisteredSubscribers().get(id).myClientSubscriber.getPreferedTopic().getBusLine())){
                    try{
                        parentBroker.getRegisteredSubscribers().get(id).out.writeObject(value);
                        parentBroker.getRegisteredSubscribers().get(id).out.flush();
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
        }

        private synchronized void pull(Value value,String message){
            for (Integer id:parentBroker.getRegisteredSubscribers().keySet()){
                if (value.getBus().getBusLineId().equals(parentBroker.getRegisteredSubscribers().get(id).myClientSubscriber.getPreferedTopic().getBusLine())){
                    try{
                        parentBroker.getRegisteredSubscribers().get(id).out.writeObject("Stopped");
                        parentBroker.getRegisteredSubscribers().get(id).out.flush();
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }
            }
            parentBroker.getRegisteredPublishers().remove(this.ClientID);
        }

        private boolean checkIfExistsInPubs(int id){
            for (Integer pubID:parentBroker.getRegisteredPublishers().keySet()){
                if (pubID==id) {
                    return true;
                }
            }
            return false;
        }
    }



    @Override
    public void run() {
        this.initBroker(port,Integer.parseInt(numberOfBrokers));
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

    public HashMap<Integer,ClientHandler> getRegisteredPublishers() {
        return registeredPublishers;
    }

    public HashMap<Integer,ClientHandler> getRegisteredSubscribers() {
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


