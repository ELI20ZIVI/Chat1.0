/*
 * Thread che rimane in ascolto in attesa di ricevere messaggi e richiede l'aggiornamento dell'interfaccia grafica al ricevimento
 */

package Chat.Client.GraphicClient;

import Chat.Client.SimpleClient.BaseClientListener;

import java.util.ArrayList;

public class GraphicClientListener extends BaseClientListener
{
    //Obbliga l'uso di un graphic client
    public GraphicClientListener(GraphicClient Client)
    {
        super(Client);
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Funzioni da sovrascrivere per eventuali interfacce grafiche
    //Ogni funzione richiama un metodo dell'interfaccia grafica per aggiungere un messaggio
    @Override
    public void MSG_PublicMessage(String user, String message)
    {
        ((GraphicClient)Client).GUI().displayNewMessage(user + ": " + message);
    }

    @Override
    public void MSG_PrivateMessage(String user, String message)
    {
        ((GraphicClient)Client).GUI().displayNewMessage(user + ": " + message);

    }

    @Override
    public void MSG_Help(String message)
    {
        ((GraphicClient)Client).GUI().displayNewMessage(message);
    }

    @Override
    public void MSG_UserList(byte[] messageBuffer)
    {
        ArrayList<String> UserList = Client.extractUsersFromMessage(messageBuffer);
        String message = "";
        for(String s: UserList)
        {
            message += s + "\n";
        }
        ((GraphicClient)Client).GUI().displayNewMessage(message);
    }

    @Override
    public void MSG_ServerOk()
    {
        ((GraphicClient)Client).GUI().displayNewMessage("SrvOK");
    }

    @Override
    public void MSG_GenericServerError()
    {
        ((GraphicClient)Client).GUI().displayNewMessage("ERROR");
    }

    @Override
    public void MSG_UsernameAlreadyUsed()
    {
        ((GraphicClient)Client).GUI().displayNewMessage("UsrERROR");
    }

    @Override
    public void MSG_ServerNotReachable()
    {
        ((GraphicClient)Client).GUI().displayNewMessage("SrvNotReachable");
    }

    @Override
    public void MSG_ServerIsFull()
    {
        ((GraphicClient)Client).GUI().displayNewMessage("SrvFULL");
    }
}
