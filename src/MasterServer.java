import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class MasterServer implements Runnable{
    public static HashMap<Integer,Broker> Masterbrokers  = new HashMap<>(); // <BrokerID,Broker>

    public static void main(String args[]) {

        MasterServer God = new MasterServer();

        Thread t1 = new Thread(God);

        t1.start();

        try {
            t1.join();
        } catch (Exception e) {
            System.out.println("Threads Interrupted");
        }


    }


    public void initServer(int serverPort) {
        ServerSocket providerSocket=null;
        Socket connection=null;
        try{
            providerSocket = new ServerSocket(serverPort);
            DatagramSocket socket = new DatagramSocket();
            System.out.println("MasterServer:"+ InetAddress.getLocalHost().getHostAddress() +":"+serverPort+" is up and running...");
            while (true) {
                connection = providerSocket.accept();

                ObjectOutputStream out = new ObjectOutputStream(connection.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(connection.getInputStream());

                //System.out.println(in.readUTF());
                //System.out.println((Message) in.readUnshared());
                try{
                    Object recievedObject = in.readObject();
                    if (recievedObject instanceof HashMap){
                        Masterbrokers.putAll((HashMap) recievedObject);
                        for (Integer brokerid: Masterbrokers.keySet()){
                            String key = brokerid.toString();
                            String address_and_ip = Masterbrokers.get(brokerid).getIPv4()+":"+Masterbrokers.get(brokerid).getPort();
                            System.out.println("-->Broker"+key + ": " +address_and_ip+ " has been initiallized and running" );
                        }
                    }else if (recievedObject instanceof String){
                        System.out.println(recievedObject);
                    }
                }catch (ClassNotFoundException e){
                    e.printStackTrace();
                }

                in.close();
                out.close();
                connection.close();
            }
        }catch (IOException ioException){
            ioException.printStackTrace();
        }finally {
            try {
                DatagramSocket socket = new DatagramSocket();
                System.out.println("MasterServer:"+socket.getLocalAddress()+":"+serverPort+" is down..");
                providerSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        initServer(8085);
    }
}
