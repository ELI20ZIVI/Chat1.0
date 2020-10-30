/*
 * Aggiunge a BaseClient:
 * - Un'interfacciaGrafica
 * - La possibilità di inserire un input (BaseActionController)
 * - La ricezione automatica dei messaggi non previsti(GraphicClientListener)
 */

package Chat.Client.GraphicClient;

import Chat.Client.BaseClient.BaseClient;
import Chat.Client.BaseClient.BaseActionController;
import Chat.Client.BaseClient.NoServersFoundException;
import Chat.Client.SimpleClient.BaseClientListener;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

public class GraphicClient extends BaseClient
{
    protected GUIClient ClientGUI;
    protected BaseActionController Controller;
    protected GraphicClientListener GraphicListener;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Constructors
    public GraphicClient(String ServerIp, int ServerPort) throws UnknownHostException, SocketException
    {
        super(ServerIp, ServerPort);
        Controller = new BaseActionController(this);
        ClientGUI = new GUIClient(this);
    }

    public GraphicClient(String ServerIp, int ServerPort, int ReceiveBufferSize)
    {
        super(ServerIp, ServerPort, ReceiveBufferSize);
        Controller = new BaseActionController(this);
        ClientGUI = new GUIClient(this);
    }

    public GraphicClient() throws NoServersFoundException, IOException
    {
        super();
        Controller = new BaseActionController(this);
        ClientGUI = new GUIClient(this);
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Others
    @Override
    public void postLogin()
    {
        GraphicListener = new GraphicClientListener(this);
        GraphicListener.listen();
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Set e get
    public GUIClient GUI()
    {
        return ClientGUI;
    }

    public BaseClientListener listener()
    {
        return GraphicListener;
    }

    public BaseActionController controller()
    {
        return Controller;
    }
}
