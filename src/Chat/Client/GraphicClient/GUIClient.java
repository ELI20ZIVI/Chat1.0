package Chat.Client.GraphicClient;

import Chat.Client.GraphicClient.GraphicClient;
import Chat.Client_Main;
import com.intellij.uiDesigner.core.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GUIClient extends JFrame{

    //Variabili
    private GraphicClient client;
    private JFrame Chat = new JFrame();
    private JButton INVIAButton = new JButton();
    private JTextField TestoInvio = new JTextField();
    private JTextPane TestoRicevo = new JTextPane();
    private JPanel PannelloInvio;
    private JTextPane PannelloRicevo;
    private JScrollPane Ricezione;
    public String mex="";

    public GUIClient(GraphicClient client){
        this.client=client;
        //JPanel principale
        Chat.setLayout(new GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        Chat.setMinimumSize(new Dimension(750, 550));
        //Jpanel di invio
        PannelloInvio = new JPanel();
        PannelloInvio.setLayout(new GridLayoutManager(2, 4, new Insets(0, 0, 0, 0), -1, -1));
        Chat.add(PannelloInvio, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_SOUTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(-1, 25), null, 0, false));
        //Bottone e barra di invio
        INVIAButton.setText("INVIA");
        PannelloInvio.getRootPane().setDefaultButton( INVIAButton );
        PannelloInvio.add(INVIAButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        PannelloInvio.add(TestoInvio, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        //Spaziatori
        final Spacer spacer1 = new Spacer();
        PannelloInvio.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, new Dimension(10, -1), new Dimension(10, -1), new Dimension(10, -1), 0, false));
        final Spacer spacer2 = new Spacer();
        PannelloInvio.add(spacer2, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(-1, 5), new Dimension(-1, 5), new Dimension(-1, 5), 0, false));
        final Spacer spacer3 = new Spacer();
        PannelloInvio.add(spacer3, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, new Dimension(10, -1), new Dimension(10, -1), new Dimension(10, -1), 0, false));
        final JLabel label1 = new JLabel();
        //Label Iniziale
        label1.setText("CHAT 1.0");
        Chat.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        //Pannello di ricezione
        PannelloRicevo = new JTextPane();
        Ricezione=new JScrollPane(PannelloRicevo);
        Chat.add(Ricezione, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        PannelloRicevo.setBackground(Color.GRAY);
        PannelloRicevo.setEditable(false);
        PannelloRicevo.setText("Inserire nome di login: ");
        //Spazio di ricezione
        TestoRicevo.setMinimumSize(new Dimension(500, 300));
        TestoRicevo.setPreferredSize(new Dimension(500, 300));

        TestoRicevo.setVisible(true);
        TestoRicevo.setEditable(false);
        Chat.setVisible(true);

        INVIAButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mex=TestoInvio.getText();
                TestoInvio.setText("");
                if(client.getIsLogged()) invioMessaggio(mex);
                else {
                    displayNewMessage(mex);
                    mex="<login;" + mex;
                    invioMessaggio(mex);
                }
            }
        });

        Chat.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    public void invioMessaggio(String mex){
        client.controller().doAction(mex);
    }

    public void displayNewMessage(String message){
        PannelloRicevo.setText(PannelloRicevo.getText()+"\n"+message);
    }
}
