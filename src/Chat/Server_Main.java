package Chat;

import Chat.Server.SimpleServer;

public class Server_Main {
    //Server
    public static void main(String[] args)
    {
       SimpleServer chatServer = new SimpleServer();
       chatServer.run();
    }
}