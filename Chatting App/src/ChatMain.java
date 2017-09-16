import java.net.*;
import java.io.*;
import java.util.*;

public class ChatMain extends Thread {
    private static ServerSocket theServer;
    private static List<Socket> peers = new ArrayList<>();
    public ChatMain(int listeningport) throws IOException {
        theServer = new ServerSocket(listeningport);
        theServer.setSoTimeout(0);
    }
    /*
     *      Thread run method.
     *
     *      Need to work with threads to detect when we get connections from other peers
     */
    public void run() {
        System.out.println("Starting server on port..." + theServer.getLocalPort());
        while (true) {
            try {
                Socket server = theServer.accept();
                server.setSoTimeout(0);
                System.out.println("Connection detected from... " + server.getRemoteSocketAddress());
                // DEBUG
                System.out.println("server addr: " + server.getInetAddress().getHostAddress() + " : port: " + server.getPort() );
                peers.add(server);
                try {
                    DataInputStream in = new DataInputStream(server.getInputStream());
                    // TODO EOFException == closed socket on other end, close gracefully
                    System.out.println(in.readUTF());
                    server.close();
                } catch (EOFException e) {
                    peers.remove(server);
                    System.out.println("Connection severed");
                }
            } catch (SocketException e) {
                System.out.println("Cya");
                break;
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    public static void main(String[] args) throws IOException {
        int listenerPort, connectionId;
        String commandOne, userInput;
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
//      while ((userInput = stdIn.readLine()) != null) {
        if (args.length != 1) {
            System.err.println("Usage: ChatMain < Listening Port >");
            System.exit(1);
        }
        listenerPort = Integer.parseInt(args[0]);
        Thread t = new ChatMain(listenerPort);
        t.start();
        try {
            System.out.println("Hello please type a command. Type help for a list of commands");
            while (true) {
                userInput = stdIn.readLine();
                String[] commandArr = userInput.split("\\s");
                commandOne = commandArr[0];
                switch (commandOne) {
                    // help
                    case "help":
                        System.out.println("type help for this screen");
                        System.out.println("type myip for the IP address of this ChatMain process");
                        System.out.println("type myport for the listening port on this ChatMain process");
                        System.out.println("type connect <destination> <port number> to connect to a ChatMain process at <destination> with the specified <port number>");
                        System.out.println("type terminate <connection id> to terminate the connection with the specified <connection id>");
                        System.out.println("type send <connection id> <message> to send a message containing <message> to the specified <connection id>");
                        System.out.println("type exit, to close this program");
                        break;
                    // myip
                    case "myip":
                        // TODO DELETE System.out.println("Your IP Address seems to be... " + Inet4Address.getLocalHost());
                        System.out.println("Your IP Address seems to be... " + Inet4Address.getLocalHost().getHostAddress());
                        break;
                    // myport
                    case "myport":
                        System.out.println("This process is listening on port... " + theServer.getLocalPort());
                        break;
                    /*
                     *      connect case here,
                     *
                     *      placeholder logic, basically, split the command into an array, if the length of that array is enough (3,)
                     *      then it accepts, probably needs even more cases (ip is wrong, no connection, etc)
                     */
                    case "connect":
                        if (commandArr.length > 3) {
                            System.out.println("Too many arguments");
                            System.out.println("connect <destination> <port number>");
                            break;
                        } else if (commandArr.length < 3) {
                            System.out.println("Too few arguments");
                            System.out.println("connect <destination> <port number>");
                            break;
                        } else {
                            // TODO hostname verification......
                            String testHostname = commandArr[1];
                            InetAddress serverAddr = InetAddress.getByName(testHostname);
                            // TODO DELETE Thread t2 = ClientRunner(testHostname, commandArr[2]);
                            try{
                            Socket client = new Socket(serverAddr, Integer.parseInt(commandArr[2]));
                            if (client.isConnected()) {
                                System.out.println("Cool, connected now");
                                peers.add(client);
                                break;
                            } else {
                                System.out.println("Not connected, try again....");
                                break;
                            }
                            } catch (UnknownHostException e){
                                System.err.println("Don't know the host: " + testHostname + ". Try again");
                                break;
                            }
                        }
                    /*
                     *      list case here, needs work
                     */
                    case "list":
                        System.out.println("Connection ID   ||  IP Address  ||  Ports");
                        for (int i = 0; i < peers.size(); i++){
                            System.out.println(i + "    ||    " + peers.get(i).getInetAddress() + "    ||    " + peers.get(i).getPort());
                        }
                        break;
                    case "terminate":
                        // TODO check if connection id exists
                        if (commandArr.length != 2){
                            System.err.println("Too few, or too many arguments");
                            System.out.println("Correct usage: terminate <connection id>");
                        }
                        try {
                            int terminateId = Integer.parseInt(commandArr[1]);
                            if (!peers.get(terminateId).equals(null)){
                                System.out.println("Disconnecting from: " + peers.get(terminateId).getInetAddress() + " on port: " + peers.get(terminateId).getPort());
                                peers.get(terminateId).close();
                                peers.remove(terminateId);
                            } else{
                                System.err.println("Connection ID specified not found");
                                break;
                            }
                        } catch (NumberFormatException e){
                            System.err.println(commandArr[1] + " was not a valid number, try again");
                            break;
                        } catch (IndexOutOfBoundsException e){
                            System.err.println("Index out of bounds query, try again.");
                            break;
                        }
                        break;
                    /*
                     *      send case here, right now it cuts the command into an array, if it is lower than 3,
                     *      it will spit out correct usage. afterwards it will join the message (array index 2 - length of command array)
                     *      and the String finMessage will be the desired message to send over the socket.
                     */
                    case "send":
                        if (commandArr.length < 3) {
                            System.out.println("send <connection id> <message>");
                        } else {
                            try {
                                int sendId = Integer.parseInt(commandArr[1]);
                                System.out.println(commandArr[0]);
                                String[] messageArr = new String[commandArr.length - 2];
                                int j = 0;
                                for (int i = 2; i < commandArr.length; i++) {
                                    messageArr[j] = commandArr[i];
                                    System.out.println(commandArr[i]);
                                    j++;
                                }
                                // TODO send finMessage to specified socket peers.get(sendId)
                                System.out.println("Sending Message to IP" + peers.get(sendId).getInetAddress() + " on port: " + peers.get(sendId).getPort());
                                String finMessage = String.join(" ", messageArr);
                                System.out.println(finMessage);
                            } catch (NumberFormatException e) {
                                System.out.println(commandArr[1] + " was not a valid number");
                                break;
                            }
                            break;
                        }

                        break;
                    case "exit":
                        theServer.close();
                        System.out.println("So long!");
                        System.exit(1);
                    default:
                        System.out.println("Not a command, type help to view a list of available commands");
                }
            }
        } catch (NumberFormatException e) {
            System.out.println(args[0] + " was not a valid number");
            System.err.println("Usage: ChatMain <Listening Port>");
            System.exit(1);
        }
    }
}
