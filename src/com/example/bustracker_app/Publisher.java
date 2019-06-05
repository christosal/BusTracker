package com.example.bustracker_app;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

public class Publisher implements Runnable, Serializable {

    private Bus myBus;
    public HashMap<Integer, Broker> brokers  = new HashMap<>(); // <BrokerID,com.example.bustracker_app.Broker>
    private boolean isRunning=false;

    public static void main(String args[]){
        Reader.readFiles();
        Publisher pub1 = new Publisher(new Bus("821","1804","10007","022"));
        //Publisher pub2 = new Publisher(new Bus("821","1805","10009","022"));
        //Publisher pub2 = new Publisher(new Bus("821","1804","10007","022"));


        Thread t1 = new Thread(pub1);
        t1.start();
        //Thread t2 = new Thread(pub2);
        //t2.start();

        try {
            t1.join();
            //t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Publisher(Bus myBus){
        this.myBus=myBus;
    }

    //Connects to its broker and simulates a real-time scenario every X sec to send values for a specific topic
    public void connectToBroker(Broker broker){
        System.out.println("Connecting to com.example.bustracker_app.Broker"+broker.getBrokerID()+": "+broker.getIPv4()+":"+broker.getPort()+"...");
        Socket requestSocket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        isRunning=true;

        try {
            requestSocket = new Socket(broker.getIPv4(), broker.getPort());
            System.out.println("Connection established! --> Sending updates...");
            out = new ObjectOutputStream(requestSocket.getOutputStream());
            in = new ObjectInputStream(requestSocket.getInputStream());

            for (HashMap<String,String> hashMap:Reader.BusPositions){
                if (myBus.getLineNumber().equals(hashMap.get("LineCode"))&&myBus.getVechicleId().equals(hashMap.get("vechicleID"))&&myBus.getRouteCode().equals(hashMap.get("RouteCode"))){
                    push(new Value(myBus,Double.parseDouble(hashMap.get("latitude")),Double.parseDouble(hashMap.get("longitude"))),out);

                    synchronized (this){
                        try{
                            wait(2000);
                        }catch (InterruptedException e){
                            e.printStackTrace();
                        }
                    }
                }
            }

            out.writeObject(null);
            out.flush();

            System.out.println("Transmition is done!");

        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException ioException) {
            ioException.printStackTrace();
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


    /*
    * Takes as inputs a com.example.bustracker_app.Value Object which includes all the necessary information to be send
    * It also take the ObjectOutputStream of the ClientHandler who is responsible for the connection of each com.example.bustracker_app.Publisher
    *
    * */
    private void push(Value value,ObjectOutputStream out){
        try {
            out.writeObject(value);
            out.flush();
        }catch (IOException e){
            e.printStackTrace();
        }
    }


    /*
       Searching brokers list (taken through connectToMasterServer()) and returns the com.example.bustracker_app.Broker who is responsible for
       the preferedTopic
   */
    private Broker findMyBroker(){
        for (Integer brokerid : brokers.keySet()) {
            for (Topic topic : brokers.get(brokerid).getResponsibilityLines()) {
                if (topic.getBusLine().equals(myBus.getBusLineId())){
                    return brokers.get(brokerid);
                }
            }
        }
        return null;
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

    @Override
    public void run() {
        connectToMasterServer(8085,"connect");
        if (brokers.size()==0){
            System.out.println("No brokers are running");
        }else{
            Broker myBroker = findMyBroker();
            if (myBroker==null){
                System.out.println("No com.example.bustracker_app.Broker is responsible for com.example.bustracker_app.Topic:"+myBus.getBusLineId());
            }else {
                for (Integer brokerid : brokers.keySet()) {
                    System.out.print("com.example.bustracker_app.Broker"+brokerid+":"+brokers.get(brokerid).getIPv4()+":"+brokers.get(brokerid).getPort()+" has: ");
                    for (Topic topic : brokers.get(brokerid).getResponsibilityLines()) {
                        System.out.print(topic.getBusLine()+" , ");
                    }
                    System.out.println("");
                }
                connectToBroker(findMyBroker());
            }
        }
    }
}
