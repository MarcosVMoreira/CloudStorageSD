package com.mycompany.cloudstorage;



import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.util.Util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import java.nio.file.WatchKey;
import java.util.List;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jgroups.Address;


/* 
usei inputFileStream pra ler e outputFileStream pra escrever
Eu pego o arquivo que esta na pasta com a classe File, pego os bytes dele,
e crio um objeto de uma class que criei que possui bytes[] como u dos atributos
 ai eu passo esse objeto pela message*/
public class Server extends ReceiverAdapter {

    //lib var
    JChannel channel;
    String user_name = System.getProperty("user.name", "n/a");
    final List<String> state = new LinkedList<String>();

    //my var
    private final String desktopServerPath = "C:\\Users\\Marcos\\Desktop\\ServerFiles\\";

    View testeView;
    
    public static void main(String[] args) throws Exception {
        new Server().start();
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
            
            sendServerFilesToRecentLoggedUser(newView.getMembers().get(newView.getMembers().size() - 1));

            /*aqui eu detecto quando outro usuario entra na rede ou sai. Neste momento, devo enviar
            pro cara que acabou de entrar os arquivos do diretório dele que está no server, caso exista*/
 /*se entrar um servidor na rede, tenho que espelhar meus dir atuais pra ele */
        } catch (Exception ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void sendServerFilesToRecentLoggedUser(Address user) throws IOException, Exception {

        LinkedList<byte[]> fileList = new LinkedList<>();
        byte[] fileContent;

        File folder = new File(desktopServerPath + user);

        if (!folder.exists()) {
            //se o usuário nao tem diretório, eu crio
            if (folder.mkdir()) {
                System.out.println("Created directory for new user.");
            } else {
                System.out.println("Failed to create directory for new user (maybe ServerFiles folder doesnt exist yet).");
            }
        } else {
            //se o usuário ja tem um diretório, tenho que mandar os arquivos pra ele
            for (File fileIt : folder.listFiles()) {
                fileContent = Files.readAllBytes(fileIt.toPath());
                fileList.add(fileContent);
            }
        }

        Message message = new Message(user, fileList);

        channel.send(message);

    }

    public void receive(Message msg) {
        System.out.println("Inside receive method "+channel.getAddress());
        String line = msg.getSrc() + ": " + msg.getObject();
        System.out.println("Linha "+line);
        synchronized (state) {
            state.add(line);
        }
        
        System.out.println("Teste");
        
        File[] fileArray = msg.getObject();
        
                System.out.println("Testando ");

        
        System.out.println("Tamanho do arquivo: "+fileArray.length);
        
        System.out.println("Recebi uma lista de arquivos: ");
        
        for (File fileIt : fileArray) {
            System.out.println("Arquivos: "+fileIt.getName());
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
                sendServerFilesToRecentLoggedUser(testeView.getMembers().get(testeView.getMembers().size() - 1));
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
