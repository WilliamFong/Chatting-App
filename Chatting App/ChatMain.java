import java.net.*;
import java.io.*;
import java.util.*;

public class ChatMain extends Thread {
    private static ServerSocket theServer;
    private static String[][] connections = null;

    public ChatMain(int listeningport) throws IOException {
        theServer = new ServerSocket(listeningport);
        theServer.setSoTimeout(0);
    }
    /*
     *      Thread run method.
     *
     *      Need to work with threads to detect when we get connections from other peers
     *
     *
     */
    public void run() {
        System.out.println("Starting server on port..." + theServer.getLocalPort());
        while (true) {
            try {
                Socket server = theServer.accept();
                server.setSoTimeout(0);
                System.out.println("Connection detected from... " + server.getRemoteSocketAddress());
                String clientPort = String.valueOf(server.getPort());
                String[] incomingServer = {server.getInetAddress().getHostAddress(), clientPort};
                System.out.println("server addr: " + server.getInetAddress().getHostAddress() + " : port: " + clientPort );
                nuConnectionArray(incomingServer);
                try {
                    DataInputStream in = new DataInputStream(server.getInputStream());
                    // todo EOFException == closed socket on other end, close gracefully
                    System.out.println(in.readUTF());
                    server.close();
                } catch (EOFException EOFe) {
                    System.out.println("Connection severed");
                }
            } catch (SocketException soe) {
                System.out.println("Cya");
                break;
            } catch (IOException ioe) {
                ioe.printStackTrace();
                break;
            }
        }
    }

    /*
     *      Array of array to handle the connections.
     *
     *      Just adds to connections[][].
     *
     *      Should find a better way of handling them... Maybe ArrayList?
     *
     */
    public static String[][] nuConnectionArray(String[] newconnection) {
        try {
            if (connections[0][0].equals(null)) {
                System.out.println("New connection to add to connection array");
            }
        } catch (NullPointerException nullex) {

            connections = new String[][]{{newconnection[0], newconnection[1]}};
            return connections;
        }
        int newconnectionpos = connections.length + 1;
        String[][] newConnectionArray = new String[newconnectionpos][2];

        for (int i = 0; i < connections.length; i++) {
            for (int j = 0; j < 2; j++) {
                newConnectionArray[i][j] = connections[i][j];
            }
        }

        System.out.println("new connection array length: " + newConnectionArray.length);
        newConnectionArray[newconnectionpos - 1][0] = newconnection[0];
        newConnectionArray[newconnectionpos - 1][1] = newconnection[1];
        connections = newConnectionArray;
        return newConnectionArray;
    }
    /*
     *      TODO create method to delete connection based on user input.
     *
     *
     *
     */
    public static String[][] delConnection(int choice) {
        System.out.println("placeholder");
        return connections;
    }
    public static void main(String[] args) throws IOException {
        int listenerPort;
        String[][] currConnArray = null;
        Socket clientSocket = null;
        ServerSocket serverSocket;
        // cases
        int menuChoice = 3, connectionId;
        String commandOne, userInput;
        Boolean sending;
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
//        while ((userInput = stdIn.readLine()) != null) {
        if (args.length != 1) {
            System.err.println("Usage: ChatMain < Listening Port >");
            System.exit(1);
        }
        listenerPort = Integer.parseInt(args[0]);
        Thread t = new ChatMain(listenerPort);
        t.start();
        try {
            //clientSocket = serverSocket.accept();
            System.out.println("Hello please type a command. Type help for a list of commands");
            // System.out.println("here we are");
            while (true) {
                userInput = stdIn.readLine();
                //System.out.println(userInput);
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
                        System.out.println("Your IP Address seems to be... " + Inet4Address.getLocalHost());
                        System.out.println("Or your IP Address seems to be... " + Inet4Address.getLocalHost().getHostAddress());
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
                     *
                     *
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
                            //Thread t2 = ClientRunner(testHostname, commandArr[2]);
                            Socket client = new Socket(serverAddr, Integer.parseInt(commandArr[2]));
                            String[] newconnection = {testHostname, commandArr[2]};

                            if (client.isConnected()) {
                                System.out.println("Cool, connected now");
                                currConnArray = nuConnectionArray(newconnection);
                                // debug stuff. printing out connection array for listing
                                for (int i = 0; i < currConnArray.length; i++) {
                                    for (int j = 0; j < currConnArray[0].length; j++) {
                                        System.out.print(currConnArray[i][j] + " ");
                                    }
                                    System.out.println(" ");
                                }
                                break;
                            } else {
                                System.out.println("Not connected, try again....");
                                break;
                            }
                        }
                    /*
                     *      list case here, needs work
                     *
                     *
                     */
                    case "list":
                        try {
                            if (connections[0][0] == null) {
                                System.out.println("No Connections!!");
                            } else {
                                System.out.println("connection id: ip, port");
                                for (int i = 0; i < connections.length; i++) {
                                    System.out.print(i + ": ");
                                    for (int j = 0; j < connections[0].length; j++) {
                                        System.out.print(connections[i][j] + " ");
                                    }
                                    System.out.println(" ");
                                }
                            }
                        } catch (NullPointerException NPE) {
                            System.out.println("No Connections Detected");
                        }
                        break;
                    case "terminate":
                        break;
                    /*
                     *      send case here, right now it cuts the command into an array, if it is lower than 3,
                     *      it will spit out correct usage. afterwards it will join the message (array index 2 - length of command array)
                     *      and the String finMessage will be the desired message to send over the socket.
                     *
                     *
                     *
                     */
                    case "send":
                        //sending = true;
                        // System.out.println(userInput);
                        //String[] sendArr = userInput.split("\\s");
                        if (commandArr.length < 3) {
                            System.out.println("send <connection id> <message>");
                        } else {
                            try {
                                connectionId = Integer.parseInt(commandArr[1]);
                                System.out.println(commandArr[0]);
                                System.out.println(connectionId);
                                String[] messageArr = new String[commandArr.length - 2];
                                int j = 0;
                                for (int i = 2; i < commandArr.length; i++) {
                                    messageArr[j] = commandArr[i];
                                    System.out.println(commandArr[i]);
                                    j++;
                                }
                                String finMessage = String.join(" ", messageArr);
                                System.out.println(finMessage);
                            } catch (NumberFormatException ne) {
                                System.out.println(commandArr[1] + " was not a valid number");
                                break;
                            }
                            break;
                        }

                        break;
                    case "exit":
                        // TODO: close socket here
                        // closeTheServer();
                        theServer.close();
                        System.out.println("So long!");
                        System.exit(1);
                    default:
                        System.out.println("Not a command, type help to view a list of available commands");
                }
                // System.out.println("echo: " + stdIn.readLine());
            }
        } catch (NumberFormatException pn) {
            System.out.println(args[0] + " was not a valid number");
            System.err.println("Usage: ChatMain <Listening Port>");
            System.exit(1);
        }
    }
}
