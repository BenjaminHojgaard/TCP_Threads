package com.company;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Scanner;


public class TCPS
{

    public static final int PORT = 6666;
    public static ServerSocket serverSocket = null; // Server gets found
    public static Socket openSocket = null;         // Server communicates with the client
    private static final int maxClientCount = 5;
    private static final clientThread[] clientThreads = new clientThread[maxClientCount];

    public static void main(String[] args) throws IOException
    {
        try
        {
            configureServer();
        }
        catch(Exception e)
        {
            System.out.println(" Connection fails: " + e);
        }
        finally
        {
            serverSocket.close();
            System.out.println("Server port closed");
        }

    }

    public static void configureServer() throws UnknownHostException, IOException
    {
        // get server's own IP address
        String serverIP = InetAddress.getLocalHost().getHostAddress();
        System.out.println("Server ip: " + serverIP);

        // create a socket at the predefined port
        serverSocket = new ServerSocket(PORT);

        while(true) {
            // Start listening and accepting requests on the serverSocket port, blocked until a request arrives
            openSocket = serverSocket.accept();
            System.out.println("Server accepts requests at: " + openSocket);

            for (int i = 0; i < maxClientCount; i++) {
                if (clientThreads[i] == null) {
                    (clientThreads[i] = new clientThread(openSocket, clientThreads)).start();
                    System.out.println(Arrays.toString(clientThreads));
                    break;
                }
            }

        }

    }

    public static void connectClient(Socket openSocket) throws IOException
    {
        String request, response;

        // two I/O streams attached to the server socket:
        Scanner in;         // Scanner is the incoming stream (requests from a client)
        PrintWriter out;    // PrintWriter is the outcoming stream (the response of the server)
        in = new Scanner(openSocket.getInputStream());
        out = new PrintWriter(openSocket.getOutputStream(),true);
        // Parameter true ensures automatic flushing of the output buffer

        // Server keeps listening for request and reading data from the Client,
        // until the Client sends "stop" requests

//        while(in.hasNextLine())
//        {
//            request = in.nextLine();
//
//            if(request.equals("stop"))
//            {
//                out.println("Good bye, client!");
//                System.out.println("Log: " + request + " client!");
//                break;
//            }
//            else
//            {
//                // server responses
//                response = new StringBuilder(request).reverse().toString();
//                out.println(response);
//                // Log response on the server's console, too
//                System.out.println("Log: " + response);
//            }
//        }
    }





}

class clientThread extends Thread {

        Socket cSocket;
        clientThread[] threads;
        int maxCount;
        PrintStream os;
        Scanner sis;

        public clientThread(Socket cSocket, clientThread[] threads) {
            this.cSocket = cSocket;
            this.threads = threads;
            maxCount = threads.length;
        }

        public void run() {
            clientThread[] threads = this.threads;
            try {

                sis = new Scanner(cSocket.getInputStream());
                os = new PrintStream(cSocket.getOutputStream(), true);


                while (true) {
                    String line = sis.nextLine();
                    System.out.println("Line: " + line);

                    if (line.equalsIgnoreCase("quit")) {
                        break;
                    }

                    synchronized (this) {
                        for (int i = 0; i < maxCount; i++) {
                            if (threads[i] == null) {
                                continue;
                            }
                            if (threads[i] != null) {
                                threads[i].os.println("A user wrote: " + line);
                            }

                        }
                    }

                }

                synchronized (this) {
                    for (int i = 0; i < maxCount; i++) {
                        if(threads[i] != null) {
                            threads[i].os.println("A user disconnected.");
                        }
                    }
                }

                synchronized (this) {
                    for (int i = 0; i < maxCount; i++) {
                        if(threads[i] == this) {
                            threads[i] = null;
                        }
                    }
                }

                sis.close();
                os.close();
                cSocket.close();

            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }

    }


