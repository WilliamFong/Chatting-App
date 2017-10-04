import java.net.*;
import java.io.*;
import java.util.*;

public class ChatMain extends Thread {
    public static List<Socket> peers = new ArrayList<>();
    public static List<Integer> peerPorts = new ArrayList<>();
    public static List<DataOutputStream> outputStreamers = new ArrayList<>();
    public static int listenerPort;

    public static boolean portChecker(int port) {
        boolean result = true;
        for (int i = 0; i < ChatMain.peers.size(); i++) {
            if (port == ChatMain.peers.get(i).getPort()) {
                result = false;
            } else {
                continue;
            }
        }
        return result;
    }

    public static void peerChecker(int port) throws IOException {
        for (int i = 0; i < peers.size(); i++) {
            if (port == peers.get(i).getPort()) {
                peers.get(i).close();
                peers.remove(i);
                break;
            } else
                continue;
        }
    }

    public static void main(String[] args) throws IOException {
        String commandOne, userInput;
        //ServerSocket peerServerSocket = null;
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        if (args.length != 1) {
            System.err.println("Usage: ChatMain < Listening Port >");
            System.exit(1);
        }
        try {
            listenerPort = Integer.parseInt(args[0]);
            System.out.println("Starting server on port..." + listenerPort);
            Thread t = new MainServer(listenerPort);
            t.start();
            System.out.println("Hello please type a command. Type help for a list of commands");
            while (true) {
                userInput = stdIn.readLine();
                String[] commandArr = userInput.split("\\s");
                commandOne = commandArr[0];
                switch (commandOne) {
                    // help
                    case "help": {
                        System.out.println("type help for this screen");
                        System.out.println("type myip for the IP address of this ChatMain process");
                        System.out.println("type myport for the listening port on this ChatMain process");
                        System.out.println("type connect <destination> <port number> to connect to a ChatMain process at <destination> with the specified <port number>");
                        System.out.println("type terminate <connection id> to terminate the connection with the specified <connection id>");
                        System.out.println("type send <connection id> <message> to send a message containing <message> to the specified <connection id>");
                        System.out.println("type exit, to close this program");
                        break;
                    }
                    case "myip": {
                        // TODO DELETE System.out.println("Your IP Address seems to be... " + Inet4Address.getLocalHost());
                        System.out.println("Your IP Address seems to be... " + Inet4Address.getLocalHost().getHostAddress());
                        break;
                    }
                    case "myport": {
                        System.out.println("This process is listening on port... " + listenerPort);
                        break;
                    }
                    case "connect": {
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
                            try {
                                Socket client = new Socket(serverAddr, Integer.parseInt(commandArr[2]));
                                if (client.isConnected()) {
                                    Thread t3 = new ChatPeerClient(client, listenerPort);
                                    t3.start();
                                    continue;
                                } else {
                                    System.out.println("Not connected, try again....");
                                    break;
                                }
                            } catch (UnknownHostException e) {
                                System.err.println("Don't know the host: " + testHostname + ". Try again");
                                break;
                            } catch (IOException e) {
                                System.out.println("Server is closed");
                                break;
                            }
                        }
                    }
                    case "list": {
                        if (peers.size() == 0){
                            System.out.println("No Connections Found");
                        } else {
                            System.out.println("Connection ID   ||  IP Address  ||  Ports");
                            for (int i = 0; i < peers.size(); i++) {
                                String[] addressSock = peers.get(i).getInetAddress().toString().split("\\/");
                                System.out.println(i + "    ||    " + addressSock[1] + "    ||    " + peerPorts.get(i));
                            }
                        }
                        break;
                    }
                    case "terminate": {
                        // TODO check if connection id exists
                        if (commandArr.length != 2) {
                            System.err.println("Too few, or too many arguments");
                            System.out.println("Correct usage: terminate <connection id>");
                        }
                        try {
                            int terminateId = Integer.parseInt(commandArr[1]);
                            if (!peers.get(terminateId).equals(null)) {
                                System.out.println("Disconnecting from: " + peers.get(terminateId).getInetAddress() + " on port: " + peers.get(terminateId).getPort());
                                //outputStreamers.get(terminateId).writeUTF("SHUT OFF");
                                peers.get(terminateId).close();
                                if (peers.get(terminateId).isClosed()) {
                                    System.out.println("Successfully closed");
                                    outputStreamers.remove(terminateId);
                                    peerPorts.remove((Integer) peers.get(terminateId).getPort());
                                    peers.remove(terminateId);
                                } else {
                                    System.out.println("NOPE NOT CLOSED");
                                }
                            } else {
                                System.err.println("Connection ID specified not found");
                            }
                        } catch (NumberFormatException e) {
                            System.err.println(commandArr[1] + " was not a valid number, try again");
                        } catch (IndexOutOfBoundsException e) {
                            System.err.println("Index out of bounds query, try again.");

                        }
                        break;
                    }

                    /* Send Command
                     * Splits command, arguement, and message into array. Tests if the length is less than 3 or not. Then tests if the 2nd element is a number or not
                     * TODO more input checking, should check if given connection id is valid or not. (in peers socket list)
                     * */
                    case "send": {
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
                                    j++;
                                }
                                System.out.println("Sending Message to IP" + peers.get(sendId).getInetAddress() + " on port: " + peers.get(sendId).getPort());
                                String finMessage = String.join(" ", messageArr);
                                outputStreamers.get(sendId).writeUTF(finMessage);
                            } catch (NumberFormatException e) {
                                System.out.println(commandArr[1] + " was not a valid number");
                                break;
                            }
                            break;
                        }

                        break;
                    }
                    case "exit": {
                        for (int i = 0; i < peers.size(); i++) {
                            peers.get(i).close();
                        }
                        MainServer.sockServer.close();
                        System.out.println("So long!");
                        System.exit(1);
                    }
                    /* DEBUG
                     * TODO delete when done
                     * */
                    case "print": {
                        System.out.println(" peers size: " + peers.size());
                        System.out.println(" output streamers: " + outputStreamers.size());
                        System.out.println(" peerPorts size: " + peerPorts.size());
                        break;
                    }
                    case "printall": {
                        for (int i = 0; i < peers.size(); i++) {
                            System.out.print(peers.get(i).getPort());
                        }
                        for (int i = 0; i < outputStreamers.size(); i++) {
                            System.out.println(outputStreamers.get(i).toString());
                        }
                        for (int i = 0; i < peerPorts.size(); i++) {
                            System.out.println("port: " + peerPorts.get(i) + " index at: " + peerPorts.indexOf(peerPorts.get(i)));
                        }

                        break;
                    }
                    default: {
                        System.out.println("Not a command, type help to view a list of available commands");
                    }
                }
            }
        } catch (NumberFormatException e) {
            System.out.println(args[0] + " was not a valid number");
            System.err.println("Usage: ChatMain <Listening Port>");
            System.exit(1);
        }

    }
}

/* Main Listener Server thread.
 *  Listens for incoming connections on the while loop, then accept and create a new thread when one comes in.
 *  TODO might need better exception handling
 * */
class MainServer extends Thread {
    int listeningPort;
    public static ServerSocket sockServer;

    MainServer(int listeningPort) {
        this.listeningPort = listeningPort;

    }

    public void run() {
        //System.out.println("Starting server on port..." + ChatMain.theServer.getLocalPort());
        try {
            sockServer = new ServerSocket(listeningPort);
            sockServer.setSoTimeout(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        while (true) {
            ChatPeerServer cps;
            try {
                //Socket server = ChatMain.theServer.accept();
                cps = new ChatPeerServer(sockServer.accept());
//                new Thread(new ChatPeerServer(server)).start();
                Thread t2 = new Thread(cps);
                //System.out.print("...");
                t2.start();
            } catch (IOException e) {
                System.err.println("IO Closed");
                break;
            }
        }
    }
}

/* Thread to handle new incoming connections
 * Opens inputstreamer to get port, and later message from other peers
 * TODO exception handling
 * TODO check if connection is already made, output right stuff.
 * */
class ChatPeerServer extends Thread {
    Socket peerSocket;
    int peerPort;

    ChatPeerServer(Socket peerSocket) {
        this.peerSocket = peerSocket;
    }

    public void run() {
        try {
            peerSocket.setSoTimeout(0);
            DataInputStream inputStreamer = new DataInputStream(peerSocket.getInputStream());
            peerPort = inputStreamer.readInt();
            System.out.println("Connection detected from... " + peerSocket.getInetAddress() + ", listening on port: " + peerPort);
            // connect to incoming client.
            if (ChatMain.peerPorts.contains(peerPort)) {
                System.out.println("Peer connection complete....");
            } else {
                Socket client = new Socket(peerSocket.getLocalAddress(), peerPort);
                if (client.isConnected()) {
                        /* Create outgoing socket connection with the incoming peer
                        *  should only run if the peerPort is not currently connected to
                        *  TODO keep better track of ports/sockets, breaks when termination occurs
                        * */
                    Thread t3 = new ChatPeerClient(client, ChatMain.listenerPort);
                    // Thread to handle client connection is started here
                    t3.start();
                }

            }
            while (true) {
                System.out.println("Message from: " + peerSocket.getInetAddress() + " listening on port:  " + peerPort + "\nMessage Content: " + inputStreamer.readUTF());
                continue;
            }
        }
        /* All Encompassing Exception, might have to shoot different warnings based on what happens??
         * */
        catch (Exception e) {
            System.out.println("Something happened with the connection from " + peerSocket.getInetAddress() + " port: " + peerPort + "\nDisconnecting...");
            int removeNum = ChatMain.peerPorts.indexOf(peerPort);
            ChatMain.peerPorts.remove(removeNum);
            try {
                ChatMain.peerChecker(peerPort);
                ChatMain.outputStreamers.get(removeNum).close();
                ChatMain.outputStreamers.remove(removeNum);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}

/* Thread for each client socket connection
 * Current way of detecting if a connection is severed, might need rewrite or eventual deletion
 * TODO rewrite???
 * */
class ChatPeerClient extends Thread {
    Socket peerSocket;
    int listenerPort;
    DataOutputStream outputStream;

    ChatPeerClient(Socket peerSocket, int listenerPort) {
        this.peerSocket = peerSocket;
        this.listenerPort = listenerPort;
    }

    public void run() {
        try {
            peerSocket.setSoTimeout(0);

            // System.out.println("\n debug info: " + listenerPort + " and " + peerSocket.getPort() + " also: " + peerSocket.getLocalPort() + "\n");
            if (!ChatMain.portChecker(peerSocket.getPort())) {
                System.out.println("ALREADY CONNECTED TO THIS");

            } else {
                System.out.println("Connection started with peer... " + peerSocket.getRemoteSocketAddress() + ", On Port: " + peerSocket.getPort());
                DataOutputStream outputStream = new DataOutputStream(peerSocket.getOutputStream());
                outputStream.writeInt(listenerPort);
                ChatMain.outputStreamers.add(outputStream);
                ChatMain.peerPorts.add(peerSocket.getPort());
                ChatMain.peers.add(peerSocket);
            }
        } catch (SocketException e) {
            // ChatMain.peerPorts.remove((Integer) listenerPort);
            ChatMain.peers.remove(peerSocket);
            ChatMain.outputStreamers.remove(outputStream);
            System.out.println("Peer Disconnected.");
        } catch (IOException e) {
            System.out.println("IO Exception");
            //ChatMain.peerPorts.remove((Integer) listenerPort);
            //ChatMain.peerPorts.remove(peerSocket.getPort());
            ChatMain.peers.remove(peerSocket);
            ChatMain.outputStreamers.remove(outputStream);
        }
    }
}