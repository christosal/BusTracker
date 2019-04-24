import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

public class Subscriber implements Runnable, Serializable {

    public  HashMap<Integer, Broker1> brokers  = new HashMap<>(); // <BrokerID,Broker1>
    private Topic preferedTopic;
    private boolean isRunning = false;

    public static void main(String args[]){
        //Subscriber subscriber1=new Subscriber(new Topic("022"));
        Subscriber subscriber2=new Subscriber(new Topic("026"));
        //Thread t1=new Thread(subscriber1);
        Thread t2=new Thread(subscriber2);
        //t1.start();
        t2.start();


    }

    public Subscriber(Topic topic){
        this.preferedTopic = topic;
    }

    //Connects to the appropriate Broker1 and then starts listening for updates
    public void register(Broker1 broker, Topic topic){
        System.out.println("Connecting to Broker1"+broker.getBrokerID()+": "+broker.getIPv4()+":"+broker.getPort()+"...");
        Socket requestSocket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        isRunning=true;

        try {
            requestSocket = new Socket(broker.getIPv4(), broker.getPort());
            System.out.println("Connection established! --> Listening for updates...");
            out = new ObjectOutputStream(requestSocket.getOutputStream());
            in = new ObjectInputStream(requestSocket.getInputStream());

            out.writeObject(this);
            out.flush();

        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        while (isRunning){
            try{
                Object recievedValue = in.readObject();
                if (recievedValue instanceof Value){
                    System.out.println("Recieved from Broker1"+broker.getBrokerID()+": "+broker.getIPv4()+":"+broker.getPort()+"---> Lat:"+((Value) recievedValue).getLatitude()+" , Long:"+((Value) recievedValue).getLongitude());
                }else if (recievedValue.equals("Stopped")){
                    System.out.println("Recieved from Broker1"+broker.getBrokerID()+": "+broker.getIPv4()+":"+broker.getPort()+"---> Transmission stopped working ");
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        try {
            in.close();
            out.close();
            //System.out.println("Disconnected");
            requestSocket.close();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

    }



    public void visualizeData(Topic topic,Value value){

    }


    //Connects to MasterServer in order to receive a list of all running brokers so to find the appropriate one
    public void connectToMasterServer(int port,String message) {
        Socket requestSocket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        try {
            requestSocket = new Socket(Reader.readBrokerIP(), port);

            out = new ObjectOutputStream(requestSocket.getOutputStream());
            in = new ObjectInputStream(requestSocket.getInputStream());


            out.writeObject(message);
            out.flush();

            try{
                brokers = (HashMap<Integer, Broker1>) in.readObject();
            }catch (Exception e){
                e.printStackTrace();
            }

        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
                //System.out.println("Disconnected");
                requestSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    /*
        Searching brokers list (taken through connectToMasterServer()) and returns the Broker1 who is responsible for
        the preferedTopic
    */
    private Broker1 findMyBroker(){
        for (Integer brokerid : brokers.keySet()) {
            for (Topic topic : brokers.get(brokerid).getResponsibilityLines()) {
                if (topic.getBusLine().equals(preferedTopic.getBusLine())){
                    return brokers.get(brokerid);
                }
            }
        }
        return null;
    }

    @Override
    public void run() {
        connectToMasterServer(8085,"connect");
        if (brokers.size()==0){
            System.out.println("No brokers are running");
        }else{
            Broker1 myBroker = findMyBroker();
            if (myBroker==null){
                System.out.println("No Broker1 is responsible for Topic:"+preferedTopic.getBusLine());
            }else {
                for (Integer brokerid : brokers.keySet()) {
                    System.out.print("Broker1"+brokerid+":"+brokers.get(brokerid).getIPv4()+":"+brokers.get(brokerid).getPort()+" has: ");
                    for (Topic topic : brokers.get(brokerid).getResponsibilityLines()) {
                        System.out.print(topic.getBusLine()+" , ");
                    }
                    System.out.println("");
                }
                register(findMyBroker(),preferedTopic);
            }
        }
    }

    public Topic getPreferedTopic() {
        return preferedTopic;
    }
}
