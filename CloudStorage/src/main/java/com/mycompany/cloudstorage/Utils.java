/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.cloudstorage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @author Marcos
 */
public class Utils {

    public static byte[] converterArquivoByte(File arquivo) throws FileNotFoundException, IOException {

        byte[] array = new byte[(int) arquivo.length()];

        FileInputStream fileInput = new FileInputStream(arquivo);
        fileInput.read(array);
        fileInput.close();

        return array;
    }

    public static void converterByteArquivo(Arquivo arquivo, String caminho) throws FileNotFoundException, IOException {

        byte[] arquivoRecebidoBytes = arquivo.getConteudoArquivo();

        FileOutputStream fileOutput = new FileOutputStream(caminho+"\\"+arquivo.getNomeArquivo());
        fileOutput.write(arquivoRecebidoBytes, 0, arquivoRecebidoBytes.length);
        fileOutput.close();

        
    }
}
