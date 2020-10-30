package Chat;

import Chat.Client.BaseClient.NoServersFoundException;
import Chat.Client.GraphicClient.GraphicClient;

import java.io.IOException;

public class Client_Main {
    //Client
    public static void main(String[] args) throws NoServersFoundException, IOException {
        //SimpleClient client = new SimpleClient("3.130.135.149", 2000);                                                //Berlingieri
        //SimpleClient client = new SimpleClient("172.16.3.50", 2000);                                                  //Server di classe
        //SimpleClient client = new SimpleClient("192.168.1.6", 2000);                                                  /Ziviani
        //SimpleClient client = new SimpleClient();                                                                     //Automatico(Broadcast)
        //client.run();                                                                                                 //Esegue il client

        GraphicClient client = new GraphicClient();                                                                     //Grafico automatico(Broadcast)
    }
}