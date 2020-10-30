package Chat.Server;

import Chat.Utils.Socket;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class BaseServer
{
    //Variables
    protected DatagramSocket Socket;
    protected DatagramPacket ReceivedPacket;

    protected HashMap<String, Chat.Utils.Socket> UsersList;
    protected int Port;
    protected int ReceiveBufferSize;

    //Settaggi
    public final int MIN_USERNAME_LENGTH = 6;
    public final int MAX_USERNAME_LENGTH = 15;
    protected int MaxLoggedClients;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Constructors
    public BaseServer()
    {
        Port = 2000;
        ReceiveBufferSize = 512;
        UsersList = new HashMap<>();

        try
        {
            Socket = new DatagramSocket(Port);
        } catch (IOException e) { }

        MaxLoggedClients = 50;
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Datagram send and receive
    public void sendDatagramPacket(byte[] builtMessage, InetAddress clientAddress, int clientPort)
    {
        DatagramPacket p_send;
        byte[] sendBuf = builtMessage;
        p_send = new DatagramPacket(sendBuf, sendBuf.length, clientAddress, clientPort);
        try
        {
            Socket.send(p_send);
        }
        catch(IOException e) { }
    }

    public DatagramPacket receiveDatagramPacket(int timeout)
    {
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
        return p_rece;
    }

    public DatagramPacket receiveDatagramPacket()
    {
        DatagramPacket p_rece;
        byte[] recBuf = new byte[ReceiveBufferSize];
        p_rece = new DatagramPacket(recBuf, recBuf.length);
        try
        {
            Socket.receive(p_rece);
        }
        catch(IOException e) { }
        return p_rece;
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Useful functions
    public byte[] insertByteArray(byte[] array, byte[] arrayToAdd, int start)
    {   //Inserisce un array dentro un altro array(byte)
        for(int i = 0; i < arrayToAdd.length; i++)
        {
            array[start + i] = arrayToAdd[i];
        }

        return array;
    }

    public byte[] insertByteArray(byte[] array, byte[] arrayToAdd, int start, int length, int offset)
    {   //Inserisce una parte di un array dentro un'altro array(byte)
        if (length <= start) return new byte[0];

        for(int i = 0; i < length - start; i++)
        {
            array[start + i] = arrayToAdd[i + offset];
        }

        return array;
    }

    public byte[] buildMessage(byte OpCode, byte[] message)
    {
        byte[] msgLength = ByteBuffer.allocate(2).putShort((short)message.length).array();
        byte[] srv_msg = new byte[message.length + msgLength.length + 1];
        srv_msg[0] = OpCode;
        srv_msg[1] = msgLength[0];
        srv_msg[2] = msgLength[1];

        srv_msg = insertByteArray(srv_msg, message, 3);

        return srv_msg;
    }

    public byte[] buildMessage(int OpCode, byte[] message) { return buildMessage((byte) OpCode, message); }

    //Ritorna una chiave della mappa corrispondente al valore
    public String getUsersListKeyByValue(Chat.Utils.Socket value)
    {
        for(Map.Entry<String, Chat.Utils.Socket> selectedUser: UsersList.entrySet())
        {
            if(selectedUser.getValue().equals(value))
            {
                return selectedUser.getKey();
            }
        }

        return "";
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Others
    public int CLIENT_Login(int messageLength)
    {
        /*
         * 0 = login avvenuto con successo
         * 1 = login non avvenuto con successo
         * 2 = username già in uso
         * 3 = x
         * 4 = server pieno
         */
        DatagramPacket loginRequest = ReceivedPacket;

        byte[] receivedBuffer = loginRequest.getData();
        String username = new String(receivedBuffer, 3, messageLength);

        if(username.indexOf(' ') != -1 || username.length() < MIN_USERNAME_LENGTH || username.length() > MAX_USERNAME_LENGTH)
        {
            System.out.println("Invalid username");
            sendDatagramPacket(buildMessage(1, new byte[0]), loginRequest.getAddress(), loginRequest.getPort());
            return 1;
        }
        if(UsersList.containsKey(username)){
            System.out.println("Username already used");
            sendDatagramPacket(buildMessage(2, new byte[0]), loginRequest.getAddress(), loginRequest.getPort());
            return 2;
        }

        if(UsersList.size() >= MaxLoggedClients)
        {
            System.out.println("Username already used");
            sendDatagramPacket(buildMessage(4, new byte[0]), loginRequest.getAddress(), loginRequest.getPort());
            return 4;
        }

        Chat.Utils.Socket newUserInfo = new Socket(loginRequest.getAddress(), loginRequest.getPort());
        //Se l'utente è già registrato cambia l'username
        String key = getUsersListKeyByValue(newUserInfo);
        if(key.length() > 0)
        {
            UsersList.remove(key);
        }

        UsersList.put(username, newUserInfo);

        System.out.println("Added new user: " + username);
        sendDatagramPacket(buildMessage(0, new byte[0]), loginRequest.getAddress(), loginRequest.getPort());
        return 0;
    }

    public boolean CLIENT_Logout()
    {
        DatagramPacket logoutRequest = ReceivedPacket;

        Chat.Utils.Socket pendingUser = new Socket(logoutRequest.getAddress(), logoutRequest.getPort());
        String key = getUsersListKeyByValue(pendingUser);
        if(key.length() > 0)
        {
            System.out.println("Removed user: " + key);
            UsersList.remove(key);
            sendDatagramPacket(buildMessage(0, new byte[0]), logoutRequest.getAddress(), logoutRequest.getPort());
            return true;
        }
        return false;
    }

    public void CLIENT_PublicMessage()                                                                                  //Possibile implementazione di un multicast
    {
        DatagramPacket publicMessagePacket = ReceivedPacket;

        Chat.Utils.Socket senderSocket = new Socket(publicMessagePacket.getAddress(), publicMessagePacket.getPort());
        String senderUser = getUsersListKeyByValue(senderSocket);
        int messageLength = ((publicMessagePacket.getData()[1] & 0xff) << 8) | (publicMessagePacket.getData()[2] & 0xff);
        byte[] senderMessage = new byte[messageLength];
        senderMessage = insertByteArray(senderMessage, publicMessagePacket.getData(), 0, messageLength, 3);

        byte[] messageToSend = new byte[senderUser.length() + messageLength + 1];
        messageToSend = insertByteArray(messageToSend, senderUser.getBytes(), 0);
        //messageToSend[senderUser.length()] = 0;                                                                       //Il valore è già a 0
        messageToSend = insertByteArray(messageToSend, senderMessage, senderUser.length() + 1);

        for(String user: UsersList.keySet())
        {
            sendDatagramPacket(buildMessage( 21, messageToSend), UsersList.get(user).getServerAddress(), UsersList.get(user).getServerPort());
        }

        System.out.println("Messaggio pubblico inoltrato");
    }

    public void CLIENT_PrivateMessage()
    {
        DatagramPacket privateMessagePacket = ReceivedPacket;
        byte[] prvt_message = new byte[0];

        Chat.Utils.Socket senderSocket = new Socket(privateMessagePacket.getAddress(), privateMessagePacket.getPort());
        String senderUser = getUsersListKeyByValue(senderSocket);
        int messageLength = ((privateMessagePacket.getData()[1] & 0xff) << 8) | (privateMessagePacket.getData()[2] & 0xff);
        byte[] senderMessage = new byte[messageLength];
        senderMessage = insertByteArray(senderMessage, privateMessagePacket.getData(), 0, messageLength, 3);

        String receiverUser = "";
        for(int i = 0; i < messageLength; i++)
        {
            if(senderMessage[i] != 0)
            {
                receiverUser += (char) senderMessage[i];
            }
            else
            {
                prvt_message = new byte[messageLength - receiverUser.length() - 1];
                prvt_message = insertByteArray(prvt_message, senderMessage, 0, prvt_message.length, receiverUser.length() + 1);
                break;
            }
        }

        if(UsersList.containsKey(receiverUser))
        {
            byte[] messageToSend = new byte[senderUser.length() + prvt_message.length + 1];
            messageToSend = insertByteArray(messageToSend, senderUser.getBytes(), 0);
            messageToSend = insertByteArray(messageToSend, prvt_message, senderUser.length() + 1);

            sendDatagramPacket(buildMessage(23, messageToSend), UsersList.get(receiverUser).getServerAddress(), UsersList.get(receiverUser).getServerPort());
            //Invio di conferma invio messaggio privato
            sendDatagramPacket(buildMessage(0, new byte[0]), senderSocket.getServerAddress(), senderSocket.getServerPort());

            System.out.println("Messaggio privato inviato a: " + receiverUser + " da " + senderUser);
        }
        else
        {
            //Invio errore di spedizione del messaggio privato
            sendDatagramPacket(buildMessage(1, new byte[0]), senderSocket.getServerAddress(), senderSocket.getServerPort());

            System.out.println("Messaggio privato di: " + senderUser + " non inviato");
        }
    }

    public void CLIENT_SendInfo()
    {
        DatagramPacket infoRequestPacket = ReceivedPacket;

        String info = "ChatServer5BI_MZ-0.1.Alpha";
        sendDatagramPacket(buildMessage(41, info.getBytes()), infoRequestPacket.getAddress(), infoRequestPacket.getPort());

        System.out.println("Invio info");
    }

    public void CLIENT_SendUserList()
    {
        DatagramPacket usersListRequestPacket = ReceivedPacket;

        int messageSize = 0;
        for (String user : UsersList.keySet())
        {
            messageSize += user.length() + 1;
        }

        byte[] byteUserList = new byte[messageSize];

        int i = 0;
        for (String user: UsersList.keySet())
        {
            byteUserList = insertByteArray(byteUserList, user.getBytes(), i);
            i += user.length() + 1;
        }

        sendDatagramPacket(buildMessage(43, byteUserList), usersListRequestPacket.getAddress(), usersListRequestPacket.getPort());

        System.out.println("Inviata lista degli utenti");
    }

    public void CLIENT_RespondAsServer()
    {
        DatagramPacket whoIsServerPacket = ReceivedPacket;

        sendDatagramPacket(buildMessage(61, new byte[0]), whoIsServerPacket.getAddress(), whoIsServerPacket.getPort());

        System.out.println("Risposta a ricerca di server da parte di un client");
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Set e get
    public DatagramSocket getSocket()
    {
        return Socket;
    }

    public DatagramPacket getReceivedPacket()
    {
        return ReceivedPacket;
    }

    public HashMap<String, Chat.Utils.Socket> getUsersList()
    {
        return UsersList;
    }

    public int getPort()
    {
        return Port;
    }

    public int getReceiveBufferSize()
    {
        return ReceiveBufferSize;
    }

    public void setMaxLoggedClients(int MaxLoggedClients)
    {
        if(MaxLoggedClients >= 4) this.MaxLoggedClients = MaxLoggedClients;
    }
}