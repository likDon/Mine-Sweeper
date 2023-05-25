package server;

import java.io.*;
import java.net.*;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.List;

public class Server implements Runnable {
    final static int PORT = 9009;
    private ServerSocket serverSocket;

    public Server() {
        new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(PORT);
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("accept connection");
                new clientThread(socket).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class clientThread extends Thread {
        private Socket socket;
        BufferedReader fromClient;
        private PrintWriter toClient;
        public clientThread(Socket socket) {
            this.socket = socket;
        }
        public void run() {
            try {
                fromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                toClient = new PrintWriter(socket.getOutputStream());

                while (true) {
                    String msg = fromClient.readLine();
                    System.out.println("leaderboard request "+msg);
                    if (msg.equals("easy") || msg.equals("regular") || msg.equals("hard")) {
                        /* return leaderboard to client */
                        File file = new File(msg + ".txt");

                        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
                        FileChannel channel = randomAccessFile.getChannel();
                        FileLock lock = null;
                        while (true) {
                            try {
                                lock = channel.lock();
                                break;
                            } catch (Exception e) {
                                System.out.println("wait for file lock");
                            }
                        }
                        String line = randomAccessFile.readLine();
                        while (line != null) {
                            toClient.println(line);
                            toClient.flush();
                            line = randomAccessFile.readLine();
                            System.out.println(line);
                        }
                        lock.release();
                        channel.close();
                        randomAccessFile.close();
                        toClient.println("finish");
                        toClient.flush();
                    } else {
                        System.out.println("new record:"+msg);
                        /* add new record */
                        String[] infoList = msg.split(" ", 0); // {level, name, time}
                        String name = infoList[1];
                        int time = Integer.parseInt(infoList[2]);
                        msg = name+" "+time;
                        List<String> newList = new ArrayList<>();

                        File file = new File(infoList[0]+".txt");
                        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
                        FileChannel channel = randomAccessFile.getChannel();
                        FileLock lock = null;
                        while (true) {
                            try {
                                lock = channel.lock();
                                break;
                            } catch (Exception e) {
                                System.out.println("wait for file lock");
                            }
                        }
                        String line = randomAccessFile.readLine();
                        while (line != null) {
                            if (Integer.parseInt(line.split(" ",0)[1]) < time) {
                                newList.add(line);
                            } else {
                                newList.add(msg);
                                msg = line;
                            }
                            line = randomAccessFile.readLine();
                        }
                        if (newList.size() < 10) {
                            newList.add(msg);
                        }
                        randomAccessFile.seek(0);
                        for(int i=0; i<newList.size(); i++) {
                            randomAccessFile.write(newList.get(i).getBytes());
                            if (i!=newList.size()-1) {
                                randomAccessFile.writeBytes(System.getProperty("line.separator"));
                            }
                        }
                        lock.release();
                        channel.close();
                        randomAccessFile.close();
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    public static void main(String[] args) { new Server();
    }
}