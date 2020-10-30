/*
 * Aggiunge:
 * - Scrittura dei messaggi da riga di comando (BaseActionController)
 * - Ricezione di messaggi non previsti (BaseClientListener)
 * - Esecuzione del programma fino a un eventuale logout
 */

package Chat.Client.SimpleClient;

import Chat.Client.BaseClient.BaseActionController;
import Chat.Client.BaseClient.BaseClient;
import Chat.Client.BaseClient.NoServersFoundException;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Scanner;

public class SimpleClient extends BaseClient
{
    protected BaseActionController Controller;
    protected BaseClientListener Listener;

    protected Scanner JInput;

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Constructors
    public SimpleClient(String ServerIp, int ServerPort) throws UnknownHostException, SocketException
    {
        super(ServerIp, ServerPort);
        Controller = new BaseActionController(this);
        JInput = new Scanner(System.in);
    }

    public SimpleClient(String ServerIp, int ServerPort, int ReceiveBufferSize)
    {
        super(ServerIp, ServerPort, ReceiveBufferSize);
        Controller = new BaseActionController(this);
        JInput = new Scanner(System.in);
    }

    public SimpleClient() throws NoServersFoundException, IOException
    {
        super();
        Controller = new BaseActionController(this);
        JInput = new Scanner(System.in);
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void run()
    {
        //Multithreading
        Thread thread = Thread.currentThread();
        thread.setName("MainClient");
        thread.setPriority(10);

        while(SRV_Login(JInput.nextLine()) != 0) { }

        //Scrittura messaggio verso server
        while(isLoggedIn)
        {
            System.out.print("\u001B[34m" + "~" + "\u001B[0m");
            Controller.doAction(JInput.nextLine());
        }
    }

    public int SRV_Login(String username)
    {
        switch (super.SRV_Login(username)) {
            case 0:
                System.out.println("\u001B[33m" + "Login avvenuto con successo!" + "\u001B[0m");
                postLogin();
                return 0;

            case 1:
                System.out.print("\u001B[33m" + "Nome utente non accettato; Login:" + "\u001B[0m");
                return 1;

            case 2:
                System.out.print("\u001B[33m" + "Nome utente non valido; Login:" + "\u001B[0m");
                return 2;

            case 3:
                System.out.print("\u001B[33m" + "Server non raggiungibile" + "\u001B[0m");
                return 3;

            case 4:
                System.out.print("\u001B[33m" + "Server pieno, ritenta tra qualche istante..." + "\u001B[0m");
                return 4;
        }
        return -1;
    }

    @Override
    public void postLogin()
    {
        Listener = new BaseClientListener(this);
        Listener.listen();
    }

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Set e get
    public BaseClientListener listener()
    {
        return Listener;
    }

    public BaseActionController controller()
    {
        return Controller;
    }
}
