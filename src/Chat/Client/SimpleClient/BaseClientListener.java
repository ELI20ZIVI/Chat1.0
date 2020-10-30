/*
 * Thread che rimane in ascolto in attesa di ricevere pacchetti e poi eventualmente ne mostra il contenuto nella corretta forma
 */

package Chat.Client.SimpleClient;

import Chat.Client.BaseClient.BaseClient;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
public class BaseClientListener implements Runnable
{
    protected static BaseClient Client;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //Costruttori
    public BaseClientListener(BaseClient Client)
    {
        this.Client = Client;
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Funzioni principali
    public void listen()
    {
        Thread tf = new Thread(this, "ClientChildThread");
        
        tf.start();
    }
    
    @Override
    public void run()
    {
        int ReceiveBufferSize = Client.getReceiveBufferSize();
        DatagramSocket Socket = Client.getSocket();
        
        DatagramPacket p_rece;

        //Altre variabili
        int split;
        String user;
        String message;

        //Ricevi i pacchetti
        while(Client.getIsLogged())
        {
            try
            {
                byte[] recBuf = new byte[ReceiveBufferSize];
                p_rece = new DatagramPacket(recBuf, recBuf.length);
                Socket.receive(p_rece);
                
                int l =(((recBuf[1] & 0xff) << 8) | (recBuf[2] & 0xff));
                String fullMessage = new String(recBuf, 3, l);

                switch (recBuf[0]) 
                {
                    case 21:
                        split = fullMessage.indexOf((char)(byte)0);
                        user = fullMessage.substring(0, split);
                        message = fullMessage.substring(split + 1);
                        MSG_PublicMessage(user, message);
                        break;
                    case 23:
                        split = fullMessage.indexOf((char)(byte)0);
                        user = fullMessage.substring(0, split);
                        message = fullMessage.substring(split + 1);
                        MSG_PrivateMessage(user, message);
                        break;
                    case 41:
                        MSG_Help(fullMessage);
                        break;
                    case 43:
                        MSG_UserList(recBuf);
                        break;
                    case 0:
                        MSG_ServerOk();
                        break;
                    case 1:
                        MSG_GenericServerError();
                        break;
                    case 2:
                        MSG_UsernameAlreadyUsed();
                        break;
                    case 3:
                        MSG_ServerNotReachable();
                        break;
                    case 4:
                        MSG_ServerIsFull();
                        break;
                    default:
                        System.out.println("\u001B[33m" + "Bad message" + "\u001B[0m");
                }
            }
            catch(IOException e) { }
        }
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Funzioni da sovrascrivere per eventuali interfacce grafiche
    public void MSG_PublicMessage(String user, String message)
    {
        System.out.println("\u001B[35m" + user + "\u001B[0m " + message);
    }

    public void MSG_PrivateMessage(String user, String message)
    {
        System.out.println("\u001B[33m" + "Private message: " + "\u001B[35m" + user + "\u001B[0m " + message);
    }

    public void MSG_Help(String message)
    {
        System.out.println("\u001B[35m" + "Help: " + message + "\u001B[0m ");
    }

    public void MSG_UserList(byte[] messageBuffer)
    {
        System.out.println("\u001B[33m" + "User list: " + Client.extractUsersFromMessage(messageBuffer) + "\u001B[0m");
    }

    public void MSG_ServerOk()
    {
        System.out.println("\u001B[33m" + "Server OK!" + "\u001B[0m");
    }

    public void MSG_GenericServerError()
    {
        System.out.println("\u001B[33m" + "Generic message error" + "\u001B[0m");
    }

    public void MSG_UsernameAlreadyUsed()
    {
        System.out.println("\u001B[33m" + "Username già in uso" + "\u001B[0m");
    }

    public void MSG_ServerNotReachable()
    {
        System.out.println("\u001B[33m" + "Server non raggiungibile" + "\u001B[0m");
    }

    public void MSG_ServerIsFull()
    {
        System.out.println("\u001B[33m" + "Il server è pieno" + "\u001B[0m");
    }
}
