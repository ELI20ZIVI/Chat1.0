package Chat.Server;

public class SimpleServer extends BaseServer
{
    protected String ServerName;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Constructors
    public SimpleServer()
    {
        super();

        ServerName = "Server" + System.currentTimeMillis() / 1000;
    }

    public SimpleServer(String ServerName)
    {
        super();

        this.ServerName = ServerName;
    }

    public SimpleServer(String ServerName, int MaxLoggedClients)
    {
        super();

        this.ServerName = ServerName;
        this.MaxLoggedClients = MaxLoggedClients;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Others
    public void run()
    {
        System.out.println("Server start...");
        while(true)
        {
            ReceivedPacket = receiveDatagramPacket();
            byte[] receivedBuffer = ReceivedPacket.getData();
            int messageLength = ((receivedBuffer[1] & 0xff) << 8) | (receivedBuffer[2] & 0xff);

            if(messageLength != (ReceivedPacket.getLength() - 3))
            {
                sendDatagramPacket(buildMessage(1, new byte[0]), ReceivedPacket.getAddress(), ReceivedPacket.getPort());
                continue;
            }

            switch(receivedBuffer[0])
            {
                case 11:        //Login
                    CLIENT_Login(messageLength);
                    break;

                case 12:        //Logout
                    CLIENT_Logout();
                    break;

                case 20:        //Messaggio pubblico
                    CLIENT_PublicMessage();
                    break;

                case 22:        //Messaggio privato
                    CLIENT_PrivateMessage();
                    break;

                case 40:        //Info
                    CLIENT_SendInfo();
                    break;

                case 42:        //Lista utenti
                    CLIENT_SendUserList();
                    break;

                case 60:        //Risposta a richiesta di ricerca server
                    CLIENT_RespondAsServer();
                    break;

                default:
                    sendDatagramPacket(buildMessage(1, "Bad OpCode".getBytes()), ReceivedPacket.getAddress(), ReceivedPacket.getPort());
                    break;
            }
        }
    }

}
