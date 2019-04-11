import java.io.*;
import java.util.*;

public class Reader implements Runnable {

    public static ArrayList<Topic> Topics = new ArrayList<>();


    public static String readBrokerIP(){
        String file ="text\\Brokers_IP.txt";
        String broker_ip=null;
        try{
            BufferedReader reader = new BufferedReader(new FileReader(file));
            broker_ip = reader.readLine();
            reader.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return broker_ip;
    }

    public synchronized static void readFiles(){
        String file ="text\\busLinesNew.txt";
        String busid=null;
        try{
            BufferedReader reader = new BufferedReader(new FileReader(file));
            while ((busid=reader.readLine()) != null) {
                String[] tokens = busid.split(",");
                Topics.add(new Topic(tokens[1]));
            }
            reader.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        readFiles();
    }
}
