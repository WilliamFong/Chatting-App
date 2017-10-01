import java.net.*;
import java.io.*;
import java.util.*;

public class ChatMain extends Thread {
    private static ServerSocket theServer;
    public static List<Socket> peers = new ArrayList<>();
    public static List<Integer> peerPorts = new ArrayList<>();
    public static List<DataOutputStream> outputStreamers = new ArrayList<>();

    public ChatMain(int listeningport) throws IOException {
        theServer = new ServerSocket(listeningport);
        theServer.setSoTimeout(0);
    }

    /*
     *      Thread run method.
     *      when a socket is accepted, we go and make a new thread for the serversocket.
     */
    public void run() {
        System.out.println("Starting server on port..." + theServer.getLocalPort());
        try {
            Socket server = theServer.accept();
            Thread t2 = new ChatPeerServer(server);
            t2.start();
        } catch (IOException e) {
            //peers.remove(server);
            System.out.print("...");
            //System.out.println("  ");
        }

    }

    public static void main(String[] args) throws IOException {
        int listenerPort;
        String commandOne, userInput;
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
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
                    case "help": {
                        System.out.println("type help for this screen");
                        System.out.println("type myip for the IP address of this ChatMain process");
                        System.out.println("type myport for the listening port on this ChatMain process");
                        System.out.println("type connect <destination> <port number> to connect to a ChatMain process at <destination> with the specified <port number>");
                        System.out.println("type terminate <connection id> to terminate the connection with the specified <connection id>");
                        System.out.println("type send <connection id> <message> to send a message containing <message> to the specified <connection id>");
                        System.out.println("type exit, to close this program");
                        break;
                        // myip
                    }
                    case "myip": {
                        // TODO DELETE System.out.println("Your IP Address seems to be... " + Inet4Address.getLocalHost());
                        System.out.println("Your IP Address seems to be... " + Inet4Address.getLocalHost().getHostAddress());
                        break;
                        // myport
                    }
                    case "myport": {
                        System.out.println("This process is listening on port... " + theServer.getLocalPort());
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
                                    peerPorts.add(Integer.parseInt(commandArr[2]));
                                    DataOutputStream outputStreamer = new DataOutputStream(client.getOutputStream());
                                    outputStreamer.writeInt(listenerPort);
                                    Thread t3 = new ChatPeerClient(client, outputStreamer);
                                    t3.start();
                                    outputStreamers.add(outputStreamer);
                                    continue;
                                } else {
                                    System.out.println("Not connected, try again....");
                                    break;
                                }
                            } catch (UnknownHostException e) {
                                System.err.println("Don't know the host: " + testHostname + ". Try again");
                                break;
                            } catch (IOException e) {
                                System.out.println("Server's Closed");
                                break;
                            }
                        }
                    }
                    case "list": {
                        System.out.println("Connection ID   ||  IP Address  ||  Ports");
                        for (int i = 0; i < peers.size(); i++) {
                            String[] addressSock = peers.get(i).getInetAddress().toString().split("\\/");
                            System.out.println(i + "    ||    " + addressSock[1] + "    ||    " + peerPorts.get(i));
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
                                    break;
                                } else {
                                    System.out.println("NOPE NOT CLOSED");
                                }
                            } else {
                                System.err.println("Connection ID specified not found");
                                break;
                            }
                        } catch (NumberFormatException e) {
                            System.err.println(commandArr[1] + " was not a valid number, try again");
                            break;
                        } catch (IndexOutOfBoundsException e) {
                            System.err.println("Index out of bounds query, try again.");

                            break;
                        }
                        break;
                    /*
                     *      send case here, right now it cuts the command into an array, if it is lower than 3,
                     *      it will spit out correct usage. afterwards it will join the message (array index 2 - length of command array)
                     *      and the String finMessage will be the desired message to send over the socket.
                     */
                    }
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
                        theServer.close();
                        System.out.println("So long!");
                        System.exit(1);
                    }
                    case "print": {
                        System.out.println(" peers size: " + peers.size());
                        System.out.println(" output streamers: " + outputStreamers.size());
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

/*
 * New thread that gets created whenever a socket connects to the listening server.
 */
class ChatPeerServer extends Thread {
    Socket peerSocket;
    int peerPort;

    ChatPeerServer(Socket peerSocket) {
        this.peerSocket = peerSocket;
    }

    public void run() {
        try {
            peerSocket.setSoTimeout(0);

            ChatMain.peers.add(peerSocket);
            try {
                DataInputStream inputStreamer = new DataInputStream(peerSocket.getInputStream());
                peerPort = inputStreamer.readInt();
                System.out.println("Connection detected from... " + peerSocket.getInetAddress() + ", listening on port: " + peerPort);

                ChatMain.peerPorts.add(peerPort);
                while (true) {
                    System.out.println("Message from: " + peerSocket.getInetAddress() + " listening on port:  " + peerPort + "\nMessage Content: " + inputStreamer.readUTF());
                    continue;
                }
            } catch (EOFException e) {
                ChatMain.peers.remove(peerSocket);
                ChatMain.peerPorts.remove(peerPort);
                System.out.println("Connection severed");
            } catch (SocketException e) {
                System.out.print("Server closing");
                ChatMain.peers.remove(peerSocket);
                ChatMain.peerPorts.remove(peerPort);
            }
        } catch (Exception e) {
            System.out.println("Something happened with the connection from " + peerSocket.getInetAddress() + " port: " + peerPort + "\nDisconnecting...");
            ChatMain.peerPorts.remove((Integer) peerPort);
            ChatMain.peers.remove(peerSocket);
        }
    }
}

/*
 *  Another thread made here, to handle server abruptly closing.
 */
class ChatPeerClient extends Thread {
    Socket peerSocket;
    int peerPort;
    DataOutputStream outputStream;

    ChatPeerClient(Socket peerSocket, DataOutputStream outputStream) {
        this.peerSocket = peerSocket;
        this.outputStream = outputStream;
    }

    public void run() {
        try {
            peerSocket.setSoTimeout(0);
            System.out.println("Connection started with peer... " + peerSocket.getRemoteSocketAddress() + ", On Port: " + peerSocket.getPort());
            ChatMain.peers.add(peerSocket);
            DataInputStream inputStreamer = new DataInputStream(peerSocket.getInputStream());
            String input = inputStreamer.readUTF();
        } catch (EOFException | SocketException e) {
            // ChatMain.peerPorts.remove(peerPort);
            ChatMain.peerPorts.remove((Integer) peerPort);
            ChatMain.peers.remove(peerSocket);
            ChatMain.outputStreamers.remove(outputStream);
            // System.out.println("EOF Exception");
            System.out.println("Peer Disconnected.");
        } catch (IOException e) {
            System.out.print("...");
        }
    }
}