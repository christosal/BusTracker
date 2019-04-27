package com.example.bustracker_app;

import java.io.*;
import java.util.*;

public class Reader implements Runnable {

    public static ArrayList<Topic> Topics = new ArrayList<>();
    public static ArrayList<HashMap<String,String>> BusPositions = new ArrayList<HashMap<String, String>>();


    public static String readBrokerIP(){
        String file ="text\\ServerIP.txt";
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


        //Read "busLineNew.txt" and stores busId in ArrayList Topics
        String fileBusLines ="text\\busLinesNew.txt";
        String busid=null;
        try{
            BufferedReader reader = new BufferedReader(new FileReader(fileBusLines));
            while ((busid=reader.readLine()) != null) {
                String[] tokens = busid.split(",");
                Topics.add(new Topic(tokens[1]));
            }
            reader.close();
        }catch (Exception e){
            e.printStackTrace();
        }

        //Read "busPositionsNew.txt" and stores elements in ArrayList<HashMap<String,String> BusPositions
        String fileBusPositions ="text\\busPositionsNew.txt";
        String line=null;
        try{
            BufferedReader reader = new BufferedReader(new FileReader(fileBusPositions));
            while ((line=reader.readLine()) != null) {
                String[] tokens = line.split(",");
                HashMap<String,String> demo = new HashMap<>();
                demo.put("LineCode",tokens[0]);
                demo.put("RouteCode",tokens[1]);
                demo.put("vechicleID",tokens[2]);
                demo.put("latitude",tokens[3]);
                demo.put("longitude",tokens[4]);
                demo.put("timestamp",tokens[5]);
                BusPositions.add(demo);
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
