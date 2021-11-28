/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author Utente
 */
public class InviaRiceviThread extends Thread {

    private DatagramSocket peer;
    boolean ifConnesso;
    ChatFrame frameGrafico;
    String nicknameMittente;
    String nicknameDestinatario;
    String ipDestinatario;
    String ipDestinatarioConnesso;

    public InviaRiceviThread() throws SocketException {
        ifConnesso = false;
        peer = new DatagramSocket(2003);
        this.frameGrafico = frameGrafico;
        nicknameDestinatario = "unknown";

    }

    public void setIpDestinatario(String ipDestinatario) {
        this.ipDestinatario = ipDestinatario;
    }

    public void setNicknameMittente(String nickname) {
        this.nicknameMittente = nickname;
    }

    String ricevi() throws IOException {
        byte[] bufferRisposta = new byte[1500];
        DatagramPacket pacchettoRisposta = new DatagramPacket(bufferRisposta, bufferRisposta.length);
        if (ifConnesso) {
            ipDestinatario = pacchettoRisposta.getAddress().toString();
        } else {
            ipDestinatarioConnesso = pacchettoRisposta.getAddress().toString();
        }
        peer.receive(pacchettoRisposta);
        return new String(bufferRisposta);
    }

    synchronized void invia(String messaggio) throws UnknownHostException, IOException {
        byte[] di = messaggio.getBytes();
        DatagramPacket pacchettoInvia = new DatagramPacket(di, di.length);
        InetAddress A = InetAddress.getByName(ipDestinatario);
        pacchettoInvia.setAddress(A);
        pacchettoInvia.setPort(2003);
        peer.send(pacchettoInvia);
    }

    @Override
    public void run() {
        String messRicevuto = "";
        while (true) {
            try {
                //trovo l'ip del destinatario
                messRicevuto = ricevi();
                System.out.println(messRicevuto);
                String[] vettElementi = messRicevuto.split(";");
                //controllo richiesta di connessione se non esistente
                if (vettElementi[0].equals("c") && ifConnesso == false) {
                    //richiesta di connessione
                    int scelta = JOptionPane.showConfirmDialog(frameGrafico, vettElementi[1] + "vuole connettersi", "Richiesta connessione...", JOptionPane.YES_NO_OPTION);
                    if (scelta == 0) {
                        nicknameDestinatario = vettElementi[1];
                        invia("y;" + nicknameMittente);
                        frameGrafico.jLabel2.setText("Connesso a:" + nicknameDestinatario + " ip: " + ipDestinatario);
                        ifConnesso = true;
                    } else {
                        invia("n;");
                    }
                }

                if (vettElementi[0].equals("m") && ifConnesso == true && ipDestinatarioConnesso == ipDestinatario) {
                    //visualizzo nel frame grafico il messaggio 
                    frameGrafico.jTextArea1.append("<"+nicknameDestinatario+"> " + vettElementi[1]);

                } else {
                    //ricevo un messaggio senza una connessione/con destinatario differente,invio n; 

                    invia("n;");
                }

                if (vettElementi[0].equals("e") && ifConnesso == true && ipDestinatarioConnesso == ipDestinatario) {
                    ifConnesso = false;
                } else {
                    //ricevo una chiusura connessione  senza una connessione/con destinatario differente
                    invia("n;");
                }
                if (vettElementi[0] == "y") {
                    //per confermare la connessione
                    invia("y;");
                } else if (vettElementi[0] == "n") {
                    //per negare la connessione
                    invia("n;");
                }

            } catch (IOException ex) {
                Logger.getLogger(InviaRiceviThread.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }
}
