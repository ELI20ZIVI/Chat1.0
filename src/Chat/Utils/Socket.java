/*
 * Classe rappresentante un host
 */

package Chat.Utils;

import java.net.InetAddress;

public class Socket
{
    protected InetAddress ServerAddress;
    protected int ServerPort;

    public Socket(InetAddress ServerAddress, int ServerPort)
    {
        this.ServerAddress = ServerAddress;
        this.ServerPort = ServerPort;
    }

    public InetAddress getServerAddress()
    {
        return ServerAddress;
    }

    public int getServerPort()
    {
        return ServerPort;
    }

    public boolean equals(Socket otherClass)
    {
        if(!ServerAddress.equals(otherClass.getServerAddress())) return false;
        if(!(ServerPort == otherClass.getServerPort())) return false;
        return true;
    }

    @Override
    public String toString() {
        return "ServerInfo{" +
                "ServerAddress=" + ServerAddress +
                ", ServerPort=" + ServerPort +
                '}';
    }
}
