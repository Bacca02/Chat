/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat;

import java.awt.Color;
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
    private boolean ifConnesso;
    private ChatFrame frameGrafico;
    private String nicknameMittente;
    private String nicknameDestinatario;
    private String ipDestinatario;
    private String ipDestinatarioConnesso;

    public InviaRiceviThread(ChatFrame frameGrafico) throws SocketException {
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
    public void setipDestinatario(String ipDestinatario) {
        this.ipDestinatario = ipDestinatario;
    }

    public String getNicknameDestinatario() {
        return nicknameDestinatario;
    }
    
    String ricevi() throws IOException {
        byte[] bufferRisposta = new byte[1500];
        DatagramPacket pacchettoRisposta = new DatagramPacket(bufferRisposta, bufferRisposta.length);
        peer.receive(pacchettoRisposta);
            ipDestinatario = pacchettoRisposta.getAddress().toString().substring(1);                        
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
                System.out.println(vettElementi[0]);
                //controllo richiesta di connessione se non esistente
                if (vettElementi[0].equals("c") && ifConnesso == false) {
                    //richiesta di connessione
                    int scelta = JOptionPane.showConfirmDialog(frameGrafico, vettElementi[1] + " vuole connettersi", "Richiesta connessione...", JOptionPane.YES_NO_OPTION);
                    if (scelta == 0) {
                        nicknameDestinatario = vettElementi[1];  
                        System.out.println(vettElementi[1]);
                        invia("y;" + nicknameMittente);
                        frameGrafico.jLabel2.setText(nicknameMittente);
                        frameGrafico.jLabel1.setText(nicknameDestinatario + " ip: " + ipDestinatario);
                        ifConnesso = true;
                        frameGrafico.jButton1.setBackground(Color.red);
                        frameGrafico.jButton1.setText("Chiudi");
                        frameGrafico.setAccesso(!frameGrafico.isAccesso());
                    } else {
                        invia("n;");
                    }
                }

                else if (vettElementi[0].equals("m") && ifConnesso == true) {
                    //visualizzo nel frame grafico il messaggio 
                    frameGrafico.jTextArea1.append("<" + nicknameDestinatario + "> " + vettElementi[1]+"\n");

                }
                    //ricevo un messaggio senza una connessione/con destinatario differente,invio n; 
                   
                

                else if (vettElementi[0].equals("e") && ifConnesso == true) {
                    frameGrafico.jTextArea1.setText("");
                    frameGrafico.jLabel1.setText("Non connesso");
                    ifConnesso = false;
                    frameGrafico.jButton1.setBackground(Color.green);
                    frameGrafico.jButton1.setText("Apri");
                    frameGrafico.setAccesso(!frameGrafico.isAccesso());
                }
                    //ricevo una chiusura connessione  senza una connessione/con destinatario differente
                
                else if (vettElementi[0].equals("y") && !ifConnesso ) {
                    //per confermare la connessione               
                    invia("y;");
                    nicknameDestinatario=vettElementi[1];
                    frameGrafico.jLabel1.setText(nicknameDestinatario+ " ip: " + ipDestinatario);
                    ifConnesso = true;
                }

            } catch (IOException ex) {
                Logger.getLogger(InviaRiceviThread.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }
}
