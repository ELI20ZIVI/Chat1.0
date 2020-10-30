package Chat.Client.BaseClient;

public class NoServersFoundException extends Exception
{
    public NoServersFoundException(String message)
    {
        super(message);
    }
}
