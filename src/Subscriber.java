import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

public class Subscriber implements Runnable, Serializable {

    public  HashMap<Integer,Broker> brokers  = new HashMap<>(); // <BrokerID,Broker>
    private Topic preferedTopic = new Topic("022");

    public static void main(String args[]){
        Subscriber subscriber=new Subscriber();
        Thread t=new Thread(subscriber);
        t.start();
    }


    public void register(Broker broker, Topic topic){
        System.out.println("Connecting to Broker"+broker.getBrokerID()+": "+broker.getIPv4()+":"+broker.getPort()+"...");
        Socket requestSocket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        try {
            requestSocket = new Socket(broker.getIPv4(), broker.getPort());

            out = new ObjectOutputStream(requestSocket.getOutputStream());
            in = new ObjectInputStream(requestSocket.getInputStream());

            out.writeObject("wd");
            out.flush();

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

    public void disconnect(Broker broker){

    }

    public void visualizeData(Topic topic,Value value){

    }

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
                brokers = (HashMap<Integer, Broker>) in.readObject();
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

    private Broker findMyBroker(){
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
        connectToMasterServer(8085,"subscriber");
        if (brokers.size()==0){
            System.out.println("No brokers are running");
        }else{
            Broker myBroker = findMyBroker();
            if (myBroker==null){
                System.out.println("No Broker is responsible for Topic:"+preferedTopic.getBusLine());
            }else {
                register(findMyBroker(),preferedTopic);
            }
        }
        for (Integer brokerid : brokers.keySet()) {
            System.out.print("Broker"+brokerid+":"+brokers.get(brokerid).getIPv4()+":"+brokers.get(brokerid).getPort()+" has: ");
            for (Topic topic : brokers.get(brokerid).getResponsibilityLines()) {
                System.out.print(topic.getBusLine()+" , ");
            }
            System.out.println("");
        }
    }
}
