/*
 * In base a una Stringa decide quale azione il client deve svolgere
 */

package Chat.Client.BaseClient;

public class BaseActionController
{
    protected BaseClient Client;
    
    public BaseActionController(BaseClient Client)
    {
        this.Client = Client;
    }

    //in base alla stringa ricevuta svolge una determinata azione
    public void doAction(String text)
    {
        if(text.length() > 0)
        {
            if(text.charAt(0) == '<')
            {
                executeCommand(text);
            }
            else
            {
                Client.SRV_PublicMessage(text);
            }
        }
    }
    
    //Esempi comandi:
    //<help
    //<privatemsg|User|Message
    public void executeCommand(String commandLine)
    {
        String command = commandLine.split(";")[0];
        command = command.toLowerCase();
        
        switch (command)
        {
            case "<login":
                try
                {
                    Client.SRV_Login(commandLine.split(";")[1]);
                }
                catch(IndexOutOfBoundsException e) { }
            break;
            
            case "<logout":
                Client.SRV_Logout();
                break;
                
            case "<help":
                Client.SRV_Help();
                break;
                
            case "<msg":
                try
                {
                    String[] data = commandLine.split(";");
                    Client.SRV_PrivateMessage(data[1], data[2]);
                }
                catch(IndexOutOfBoundsException e) { }
                break;
                
            case "<userlist":
                Client.SRV_RequestUserList();
                break;
        }
    }
}
