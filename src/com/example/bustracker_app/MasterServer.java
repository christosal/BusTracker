package com.example.bustracker_app;

import java.io.*;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class MasterServer extends Node implements Runnable{
    public static HashMap<Integer, Broker> Masterbrokers  = new HashMap<>(); // <BrokerID,com.example.bustracker_app.Broker>

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


    public void initMasterServer(int serverPort) {
        ServerSocket MasterServerSocket=null;
        try{
            MasterServerSocket = new ServerSocket(serverPort);
            setLocalIP();
            System.out.println("MasterServer:"+ MACHINE_IP +":"+serverPort+" is up and running...");
            while (true) {
                new ClientHandler(MasterServerSocket.accept()).start();
            }
        }catch (IOException ioException){
            ioException.printStackTrace();
        }finally {
            try {
                DatagramSocket socket = new DatagramSocket();
                System.out.println("MasterServer:"+socket.getLocalAddress()+":"+serverPort+" is down..");
                MasterServerSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    private static class ClientHandler extends Thread {
        private Socket clientSocket;
        private ObjectOutputStream out;
        private ObjectInputStream in;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {

            try {
                out = new ObjectOutputStream(clientSocket.getOutputStream());
                in = new ObjectInputStream(clientSocket.getInputStream());
                System.out.println(clientSocket);
                Object recievedObject = in.readObject();
                if (recievedObject instanceof HashMap) {
                    Masterbrokers.putAll((HashMap) recievedObject);
                    for (Integer brokerid : Masterbrokers.keySet()) {
                        String key = brokerid.toString();
                        String address_and_ip = Masterbrokers.get(brokerid).getIPv4() + ":" + Masterbrokers.get(brokerid).getPort();
                        System.out.println("-->Broker" + key + ": " + address_and_ip + " is running...");
                    }
                } else if (recievedObject instanceof String) {
                    if (recievedObject.equals("alloc")) {
                        calculateKeys();
                        System.out.println("");
                    } else if (recievedObject.equals("connect")) {
                        out.writeObject(Masterbrokers);
                        //out.writeObject("Hello");
                        out.flush();
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

    public static void calculateKeys() {
        Reader.readFiles();
        for (Topic topic : Reader.Topics) {
            String hashedTopic = sha1(topic.getBusLine());
            int counter = 0;
            for (Integer brokerid : Masterbrokers.keySet()) {
                counter++;
                String address_and_ip = Masterbrokers.get(brokerid).getIPv4() + Masterbrokers.get(brokerid).getPort();
                if (counter == Masterbrokers.size()) {
                    Masterbrokers.get(brokerid).getResponsibilityLines().add(topic);
                } else {
                    if (hashedTopic.compareTo(address_and_ip) < 0) {
                        Masterbrokers.get(1).getResponsibilityLines().add(topic);
                        break;
                    }
                }
            }
        }
        System.out.println("---STATUS INFO--- \'Allocation was successful!\'");
        for (Integer brokerid : Masterbrokers.keySet()) {
            System.out.print("Broker"+brokerid+":"+Masterbrokers.get(brokerid).getIPv4()+":"+Masterbrokers.get(brokerid).getPort()+" has: ");
            for (Topic topic : Masterbrokers.get(brokerid).getResponsibilityLines()) {
                System.out.print(topic.getBusLine()+" , ");
            }
            System.out.println("");
        }
    }

    @Override
    public void run() {
        initMasterServer(8085);
    }
}
