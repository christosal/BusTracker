import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.Scanner;

public class MasterServerCMD implements Runnable {


    public static void main(String args[]){
        MasterServerCMD cmd = new MasterServerCMD();
        Thread t = new Thread(cmd);

        t.start();
    }


    public void startClient(int port,String command) {
        Socket requestSocket = null;
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        try {
            requestSocket = new Socket(Reader.readBrokerIP(), port);

            out = new ObjectOutputStream(requestSocket.getOutputStream());
            in = new ObjectInputStream(requestSocket.getInputStream());

            out.writeObject(command);
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
        while (true){
            System.out.print("Give your command: ");
            Scanner scan= new Scanner(System.in);
            String command= scan.nextLine();
            if (command.toLowerCase()!="exit") {
                startClient(8085,command);
            }
        }
    }
}
