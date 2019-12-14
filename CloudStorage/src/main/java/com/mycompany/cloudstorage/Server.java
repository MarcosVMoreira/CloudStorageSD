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


public class Server extends ReceiverAdapter {

    //lib var
    private static JChannel channel;

    final List<String> state = new LinkedList<String>();

    //my var
    private static String homePath = "C:\\Users\\Marcos\\Desktop\\ServerFiles\\";

    View testeView;

    public static void main(String[] args) throws Exception {
        new Server().start();
        homePath = homePath.concat(channel.address() + "\\");

        File folder = new File(homePath);

        if (!folder.exists()) {
            //se o usuário nao tem diretório, eu crio
            if (folder.mkdir()) {
                System.out.println("Created directory for new server.");

            } else {
                System.out.println("Failed to create directory for new server (maybe ServerFukes folder doesnt exist yet).");
            }
        }

        checkOnlineServers();

    }

    private void start() throws Exception {

        try {
            channel = new JChannel()
                    .connect("BropDox")
                    .setReceiver(this)
                    .getState(null, 10000);
        } catch (Exception e) {
            System.out.println("Erro pra startar: " + e.getMessage());
        }
    }

    public static void checkOnlineServers() throws Exception {

        Message message = new Message(null, "server");

        channel.send(message);
    }

    public void viewAccepted(View newView) {

    }

    public void sendServerFilesToRecentLoggedUser(Address user) throws IOException, Exception {

        LinkedList<byte[]> fileList = new LinkedList<>();
        byte[] fileContent;

        File folder = new File(homePath + user);

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

    public void receive(Message mensagemRecebida) {

        if (mensagemRecebida.getObject() instanceof Arquivo) {
            Arquivo arquivoRecebido = mensagemRecebida.getObject();

            File folder = new File(homePath + arquivoRecebido.getDonoArquivo());

            if (!folder.exists()) {
                //se o usuário nao tem diretório, eu crio
                if (folder.mkdir()) {
                    System.out.println("Created directory for new user.");
                } else {
                    System.out.println("Failed to create directory for new user (maybe UserFiles folder doesnt exist yet).");
                }
            }

            try {
                Utils.converterByteArquivo(arquivoRecebido, homePath + arquivoRecebido.getDonoArquivo());

            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {

            String mensagemString = mensagemRecebida.getObject();

            if (mensagemString.startsWith("delete")) {

                File folder = new File(homePath + mensagemString.split(" ")[2]);

                if (folder.exists()) {
                    File f = new File(homePath + mensagemString.split(" ")[2] + "\\" + mensagemString.split(" ")[1]);
                    f.delete();
                }

            } else if (!mensagemString.equals("server")) {
                // primeiro login do usuário e, portanto, devo checkar se ele ja tem pasta no servidor. Se tem, envio os arquivos da pasta pra ele
                File folder = new File(homePath + mensagemString);

                if (folder.exists()) {
                    //se o usuário nao tem diretório, eu crio

                    for (File fileIt : folder.listFiles()) {

                        try {
                            Arquivo arquivo = new Arquivo(Utils.converterArquivoByte(fileIt), fileIt.getName(), mensagemString);
                         
                            Message message = new Message(mensagemRecebida.getSrc(), arquivo);
                            channel.send(message);
                        } catch (IOException ex) {
                            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (Exception ex) {
                            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }

                } else {
                    if (folder.mkdir()) {
                        System.out.println("Created directory for new user.");

                    } else {
                        System.out.println("Failed to create directory for new user (maybe UserFiles folder doesnt exist yet).");
                    }
                }

                Message message = new Message(mensagemRecebida.getSrc(), "start");
                try {
                    channel.send(message);
                } catch (Exception ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }

            } else {
                File folder = new File(homePath);

                if (folder.exists()) {
                    //se o usuário nao tem diretório, eu crio

                    for (File fileIt : folder.listFiles()) {

                        if (fileIt.isDirectory()) {

                            File userFolder = new File(fileIt.getPath());

                            for (File secondIt : userFolder.listFiles()) {

                                try {
                                    Arquivo arquivo = new Arquivo(Utils.converterArquivoByte(secondIt), secondIt.getName(), fileIt.getName());
          
                                    Message message = new Message(mensagemRecebida.getSrc(), arquivo);
                                    channel.send(message);
                                } catch (IOException ex) {
                                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                                } catch (Exception ex) {
                                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        }

                    }

                }
            }

        }

    }

    public void getState(OutputStream output) throws Exception {
        synchronized (state) {
            Util.objectToStream(state, new DataOutputStream(output));
        }
    }

    @SuppressWarnings("unchecked")
    public void setState(InputStream input) throws Exception {

    }

}
