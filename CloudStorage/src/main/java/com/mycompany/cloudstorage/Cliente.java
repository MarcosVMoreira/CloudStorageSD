package com.mycompany.cloudstorage;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.util.Util;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Marcos
 */
public class Cliente extends ReceiverAdapter  {

    //lib var
    JChannel channel;
    String user_name = System.getProperty("user.name", "n/a");
    final List<String> state = new LinkedList<String>();

    //my var
    private final String desktopUserPath = "C:\\Users\\Marcos\\Desktop\\UserFiles\\";

    View testeView;

    public static void main(String[] args) throws Exception {
        new Cliente().start();
    }

    private void start() throws Exception {
        channel = new JChannel();
        channel.setReceiver(this);
        channel.connect("BropDox");
        channel.getState(null, 10000);
        eventLoop();
        channel.close();
    }

    public void viewAccepted(View newView) {

        testeView = newView;

        System.out.println("** view: " + newView);

        System.out.println("members online right now: " + newView.getMembers());

        System.out.println("User " + newView.getMembers().get(newView.getMembers().size() - 1) + " just logged in.");

        try {

            sendUserFilesToServer(newView.getMembers().get(newView.getMembers().size() - 1));

            /*aqui eu detecto quando outro usuario entra na rede ou sai. Neste momento, devo enviar
            pro cara que acabou de entrar o diretório dele que está no server, caso exista*/
 /*se entrar um servidor na rede, tenho que espelhar meus dir atuais pra ele */
        } catch (Exception ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void sendUserFilesToServer(Address user) throws IOException, Exception {

        LinkedList<byte[]> fileList = new LinkedList<>();
        byte[] fileContent;

        File folder = new File(desktopUserPath + user);

        if (!folder.exists()) {
            //se o usuário nao tem diretório, eu crio
            if (folder.mkdir()) {
                System.out.println("Created directory for new user.");
            } else {
                System.out.println("Failed to create directory for new user (maybe UserFiles folder doesnt exist yet).");
            }
        } else {
            //se o usuário ja tem um diretório, tenho que mandar os arquivos pra ele
            for (File fileIt : folder.listFiles()) {
                fileContent = Files.readAllBytes(fileIt.toPath());
                fileList.add(fileContent);
            }
        }

        Message message = new Message(null, fileList);
        
        channel.send(message);

    }

    public void receive(Message msg) {
        String line = msg.getSrc() + ": " + msg.getObject();
        System.out.println(line);
        synchronized (state) {
            state.add(line);
        }

        /* aqui detecto quando o usuario enviar um arquivo novo pro server. Na verdade, ele irá enviar arquivos através do
        Java WatchService que vai ficar rodando na maquina dele.
        aqui devo reconhecer se virá um server conectando ou um usuario conectando e tomar a medida necessária */
    }

    public void getState(OutputStream output) throws Exception {
        synchronized (state) {
            Util.objectToStream(state, new DataOutputStream(output));
        }
    }

    @SuppressWarnings("unchecked")
    public void setState(InputStream input) throws Exception {
        List<String> list = (List<String>) Util.objectFromStream(new DataInputStream(input));
        synchronized (state) {
            state.clear();
            state.addAll(list);
        }
        System.out.println("received state (" + list.size() + " messages in chat history):");
        for (String str : list) {
            System.out.println(str);
        }
    }

    private void eventLoop() {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            try {
                System.out.print("> ");
                System.out.flush();
                String line = in.readLine().toLowerCase();
                sendUserFilesToServer(testeView.getMembers().get(testeView.getMembers().size() - 1));
                if (line.startsWith("quit") || line.startsWith("exit")) {
                    break;
                }
                line = "[" + user_name + "] " + line;

                Message msg = new Message();

                channel.send(msg);
            } catch (Exception e) {
            }
        }
    }

}
