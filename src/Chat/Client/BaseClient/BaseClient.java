/*
 * Contiene tutti i metodi necessari a svolgere la funzione di client:
 * - Login
 * - Logout
 * - Messaggio pubblico
 * - Messaggio privato
 * - Lista degli utenti online
 * - Help
 *
 * Non supporta la ricezione di input o messaggi non previsti
 */

package Chat.Client.BaseClient;

import Chat.Utils.Socket;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;

public class BaseClient{
    protected InetAddress ServerIp;
    public int ServerPort;
    
    protected DatagramSocket  Socket;
    protected int ReceiveBufferSize;
    protected int Timeout;
    
    protected String Username;
    protected boolean isLoggedIn;
    protected ArrayList<Chat.Utils.Socket> ServersList;
    
////////////////////////////////////////////////////////////////////////////////
    //Constructors
    public BaseClient(String ServerIp, int ServerPort) throws UnknownHostException, SocketException
    {
        //Ip e porta del server
        this.ServerIp = InetAddress.getByName(ServerIp);
        this.ServerPort = ServerPort;

        //Socket
        Socket = new DatagramSocket();
        
        //Altro
        ReceiveBufferSize = 512;
        isLoggedIn = false;
        Timeout = 500;
    }
    
    public BaseClient(String ServerIp, int ServerPort, int ReceiveBufferSize)
    {
        //Ip e porta del server
        try
        {
            this.ServerIp = InetAddress.getByName(ServerIp);
        } catch(UnknownHostException e) { System.out.println("\u001B[31m" + "Bad ip error!" + "\u001B[0m"); }
        this.ServerPort = ServerPort;
            
        
        //Socket
        try
        {
            Socket = new DatagramSocket();
        }
        catch(SocketException e) { }
        
        //Altro
        this.ReceiveBufferSize = ReceiveBufferSize;
        isLoggedIn = false;
        Timeout = 500;
    }

    //Trova una lista di server disponibili in automatico mandando un messaggio di broadcast -> si connette al primo che ha trovato
    public BaseClient() throws NoServersFoundException,IOException
    {
        //Socket
        Socket = new DatagramSocket();

        //Altro
        ServersList = new ArrayList<>();
        ReceiveBufferSize = 512;
        isLoggedIn = false;
        Timeout = 500;

        //broadcast per cercare un server
        Socket.setBroadcast(true);
        byte[] sendBuf = buildMessage((byte) 60, new byte[0]);
        for(InetAddress address: listAllBroadcastAddresses())
        {
            DatagramPacket broadcastPacket = new DatagramPacket(sendBuf, sendBuf.length, listAllBroadcastAddresses().get(0), 2000);
            Socket.send(broadcastPacket);
        }

        //ricezione risposta
        DatagramPacket p_rece;
        byte[] recBuf = new byte[ReceiveBufferSize];
        p_rece = new DatagramPacket(recBuf, recBuf.length);

        //Salvataggio di tutti gli indirizzi e porte da cui si ha ricevuto una risposta
        Socket.setSoTimeout(500);
        while(true)
        {
            try
            {
                Socket.receive(p_rece);

                if (recBuf[0] == 61)
                {
                    //Aggiunge alla lista l'indirizzo e la porta del server solo se non sono già presenti
                    Chat.Utils.Socket newServer = new Socket(p_rece.getAddress(), p_rece.getPort());
                    boolean alreadyHasServer = false;
                    for(Chat.Utils.Socket srv: ServersList)
                    {
                        if(srv.equals(newServer))
                        {
                            alreadyHasServer = true;
                            break;
                        }
                    }
                    if(!alreadyHasServer) ServersList.add(newServer);
                }
            }
            catch(SocketTimeoutException e)
            {
                break;
            }
        }
        Socket.setSoTimeout(30000);                                                                                     //reset del timeout

        //Se non sono stati trovati server solleva un eccezione
        if(ServersList.size() == 0) throw new NoServersFoundException("NoServersFound");

        //Altrimenti si "connette" al primo che ha trovato
        ServerIp = ServersList.get(0).getServerAddress();
        ServerPort = ServersList.get(0).getServerPort();

    }
    
////////////////////////////////////////////////////////////////////////////////
    //Funzioni utili
    public byte[] insertByteArray(byte[] array, byte[] arrayToAdd, int start)
    {   //Inserisce un array di byte dentro un altro array
        for(int i = 0; i < arrayToAdd.length; i++)
        {
            array[start + i] = arrayToAdd[i];
        }
        
        return array;
    }
    
    public byte[] buildMessage(byte OpCode, byte[] message)
    {   //Calcola e inserisce la lunghezza e l'OpCode del messaggio
        byte[] msgLength = ByteBuffer.allocate(2).putShort((short)message.length).array();
        byte[] srv_msg = new byte[message.length + msgLength.length + 1];
        srv_msg[0] = OpCode;
        srv_msg[1] = msgLength[0];
        srv_msg[2] = msgLength[1];
        
        srv_msg = insertByteArray(srv_msg, message, 3);
        
        return srv_msg;
    }
    
    public ArrayList<String> extractUsersFromMessage(byte[] receivedBuffer) 
    {   //Ricava una lista di stringhe da un array di byte
        ArrayList<String> userList = new ArrayList();
        String user = "";
        
        for(int i = 3; i < receivedBuffer.length; i++)
        {
            if(user.equals("") && receivedBuffer[i] == 0)
            {
                break;
            }
            if(receivedBuffer[i] != 0)
            {
                user += (char) receivedBuffer[i];
            }
            else
            {
                userList.add(user);
                user = "";
            }
        }
        
        if(user.length() > 0) userList.add(user);
        
        return userList;
    }

    //Trova tutti gli indirizzi di broadcast disponibili
    ArrayList<InetAddress> listAllBroadcastAddresses() throws SocketException {
        ArrayList<InetAddress> broadcastList = new ArrayList<>();
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();

            if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                continue;
            }

            networkInterface.getInterfaceAddresses().stream()
                    .map(InterfaceAddress::getBroadcast)
                    .filter(Objects::nonNull)
                    .forEach(broadcastList::add);
        }
        return broadcastList;
    }
////////////////////////////////////////////////////////////////////////////////
    //Possibile implementazione del protocollo TCP
    //Datagram send and receive
    public void sendDatagramPacket(byte[] builtMessage)
    {   //Manda un pacchetto
        DatagramPacket p_send;
        byte[] sendBuf = builtMessage;
        p_send = new DatagramPacket(sendBuf, sendBuf.length, ServerIp, ServerPort);
        try
        {
            Socket.send(p_send);
        }
        catch(IOException e) { }
    }
    
    public byte[] receiveDatagramPacket(int timeout)
    {   //Riceve un pacchetto con attesa limitata
        DatagramPacket p_rece;
        byte[] recBuf = new byte[ReceiveBufferSize];
        p_rece = new DatagramPacket(recBuf, recBuf.length);
        try
        {
            Socket.setSoTimeout(timeout);
            Socket.receive(p_rece);
            Socket.setSoTimeout(30000);
        }
        catch(IOException e)
        {
            recBuf[0] = 3;
        }
        return recBuf;
    }
    
    public byte[] receiveDatagramPacket()
    {   //Attende indeterminatamente un pacchetto
        DatagramPacket p_rece;
        byte[] recBuf = new byte[ReceiveBufferSize];
        p_rece = new DatagramPacket(recBuf, recBuf.length);
        try
        {
            Socket.receive(p_rece);
        }
        catch(IOException e) { }
        return recBuf;
    }
    
////////////////////////////////////////////////////////////////////////////////
    //Client to server functions
    public int SRV_Login(String username)
    {
        /*
         * 0 = login avvenuto con successo
         * 1 = login non avvenuto con successo
         * 2 = username già in uso
         * 3 = timeout scaduto
         * 4 = server pieno
         */
        sendDatagramPacket(buildMessage((byte)11, username.getBytes()));
        
        byte[] receivedBuffer = receiveDatagramPacket(Timeout);

        if(receivedBuffer[0] == 0)
        {
            Username = username;
            isLoggedIn = true;
            postLogin();
        }

        return receivedBuffer[0];
    }

    public void postLogin()
    {
        //Fai qualcosa dopo il login
    }
    
    public boolean SRV_Logout()
    {
        sendDatagramPacket(buildMessage((byte)12, new byte[0]));
        
        byte[] receivedBuffer = receiveDatagramPacket(Timeout);

        isLoggedIn = false;

        if(receivedBuffer[0] == 0) 
        {
            return true;
        }
        
        return false;
    }
    
    public String SRV_Help()
    {
        sendDatagramPacket(buildMessage((byte)40, new byte[0]));
        

        byte[] receivedBuffer = receiveDatagramPacket(Timeout);

        if(receivedBuffer[0] == 41) 
        {
            return new String(receivedBuffer, 3, receivedBuffer.length -3);
        }

        return "Impossibile ricevere la lista dei comandi";
    }
    
    public String SRV_PublicMessage(String message)
    {
        byte[] msg = message.getBytes();
        sendDatagramPacket(buildMessage((byte)20, msg));

        byte[] receivedBuffer = receiveDatagramPacket(Timeout);

        if(receivedBuffer[0] == 21) 
        {
            return new String(receivedBuffer, 3, receivedBuffer.length -3);
        }

        return "";
    }
    
    public boolean SRV_PrivateMessage(String user, String message)
    {
        byte[] byteUser = user.getBytes();
        byte[] byteMessage = message.getBytes();
        byte[] prvt_msg = new byte[byteUser.length + byteMessage.length + 1];

        prvt_msg = insertByteArray(prvt_msg, byteUser, 0);
        //prvt_msg[byteUser.length] = 0;
        prvt_msg = insertByteArray(prvt_msg, byteMessage, byteUser.length + 1);

        sendDatagramPacket(buildMessage((byte)22, prvt_msg));
        
        byte[] receivedBuffer = receiveDatagramPacket(Timeout);

        if(receivedBuffer[0] == 0) return true;
        return false;
    }
    
    public ArrayList<String> SRV_RequestUserList()
    {
        sendDatagramPacket(buildMessage((byte)42, new byte[0]));
        byte[] receivedBuffer = receiveDatagramPacket(Timeout);
        
        if(receivedBuffer[0] == 43) 
        {
            return extractUsersFromMessage(receivedBuffer);
        }
        else
        {
            ArrayList<String> error = new ArrayList();
            error.add("Impossibile ottenere una lista degli utenti online");
            return error;
        }
    }
    
////////////////////////////////////////////////////////////////////////////////
    //Set
    public InetAddress getServerIp()
    {
        return ServerIp;
    }

    public int getServerPort()
    {
        return ServerPort;
    }

    public DatagramSocket getSocket()
    {
        return Socket;
    }

    public int getReceiveBufferSize()
    {
        return ReceiveBufferSize;
    }

    public String getUsername()
    {
        return Username;
    }
    
    public boolean getIsLogged()
    {
        return isLoggedIn;
    }

    public void setTimeout(int Timeout)
    {
        this.Timeout = Timeout;
    }

    public ArrayList<Chat.Utils.Socket> getServersList()
    {
        return ServersList;
    }

    public String toString()
    {
        return Username;
    }
}