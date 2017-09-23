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
     *
     *      Need to work with threads to detect when we get connections from other peers
     */
    public void run() {
        System.out.println("Starting server on port..." + theServer.getLocalPort());
        while (true) {
            try {
                Socket server = theServer.accept();
                Thread t2 = new ChatPeerServer(server);
                t2.start();
            }
            catch (IOException e) {
                //peers.remove(server);
                continue;
                //System.out.println("  ");
            }
        }
    }

    public static void main(String[] args) throws IOException {
        int listenerPort, connectionId;
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
                            try {
                                Socket client = new Socket(serverAddr, Integer.parseInt(commandArr[2]));
                                // TODO Maybe a new thread for the peer client, so when peer that was connected exits, we close sockets and remove socket from list.
                                Thread t3 = new ChatPeerClient(client, Integer.parseInt(commandArr[2]));
                                t3.start();
                                if (client.isConnected()) {
                                    System.out.println("Cool, connected now");
                                    peers.add(client);
                                    peerPorts.add(Integer.parseInt(commandArr[2]));
                                    byte port = (byte) listenerPort;
                                    DataOutputStream outputStreamer = new DataOutputStream(client.getOutputStream());
                                    outputStreamer.writeInt(listenerPort);
                                    outputStreamers.add(outputStreamer);
                                    //outputStreamer.close();
                                    continue;
                                } else {
                                    System.out.println("Not connected, try again....");
                                    break;
                                }
                            } catch (UnknownHostException e) {
                                System.err.println("Don't know the host: " + testHostname + ". Try again");
                                break;
                            } catch (IOException e){
                                System.out.println("Server's Closed");
                                break;
                            }
                        }
                    /*
                     *      list case here, needs work
                     */
                    case "list":
                        System.out.println("Connection ID   ||  IP Address  ||  Ports");
                        for (int i = 0; i < peers.size(); i++) {
                            String[] addressSock = peers.get(i).getInetAddress().toString().split("\\/");
                            System.out.println(i + "    ||    " + addressSock[1] + "    ||    " + peerPorts.get(i));
                        }
                        break;
                    case "terminate":
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
                                outputStreamers.get(sendId).writeUTF(finMessage);
                                System.out.println(finMessage);
                            } catch (NumberFormatException e) {
                                System.out.println(commandArr[1] + " was not a valid number");
                                break;
                            }
                            break;
                        }

                        break;
                    case "exit":
                        for (int i = 0; i < peers.size(); i++){
                            peers.get(i).close();
                        }
                        theServer.close();
                        System.out.println("So long!");
                        System.exit(1);
                    case "print":
                        System.out.println(peerPorts.size());
                        break;
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
/*
 * New thread that gets created whenever a socket connects to the listening server.
 *
 *
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
            System.out.println("Connection detected from... " + peerSocket.getRemoteSocketAddress());
            // DEBUG
            // int length = inputStreamer.readInt();
            System.out.println("server addr: " + peerSocket.getInetAddress().getHostAddress());
            System.out.println("port: " + peerSocket.getPort());
            // DEBUG
            // System.out.println("test1" + peerSocket.getInetAddress().getCanonicalHostName());
            // DEBUG
            // System.out.println("Other: " + peerSocket.getInetAddress().toString());
            ChatMain.peers.add(peerSocket);
            try {
                DataInputStream inputStreamer = new DataInputStream(peerSocket.getInputStream());
                peerPort = inputStreamer.readInt();
                System.out.println("read port: " + peerPort);
                ChatMain.peerPorts.add(peerPort);


                while (true) {
                    System.out.println(inputStreamer.readUTF());
                    continue;
                }
            } catch (EOFException e) {
                ChatMain.peers.remove(peerSocket);
                ChatMain.peerPorts.remove(peerPort);
                System.out.println("Connection severed");
            } catch (SocketException e) {
                System.out.println("SOCKET EXCEPTION");
                ChatMain.peers.remove(peerSocket);
                ChatMain.peerPorts.remove(peerPort);
            }
        } catch (Exception e) {
            // ChatMain.peerPorts.remove(peerPort);
            ChatMain.peerPorts.remove((Integer) peerPort);
            ChatMain.peers.remove(peerSocket);
            //System.out.println("  ");
        }
    }
}
