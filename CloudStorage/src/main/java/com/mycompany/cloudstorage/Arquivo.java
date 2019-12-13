/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.cloudstorage;

import java.io.Serializable;

/**
 *
 * @author Marcos
 */
public class Arquivo implements Serializable {

    private String nomeArquivo;

    private byte[] conteudoArquivo;
    
    private String donoArquivo;

    public Arquivo(byte[] conteudoArquivo, String nomeArquivo, String donoArquivo) {
        this.conteudoArquivo = conteudoArquivo;
        this.nomeArquivo = nomeArquivo;
        this.donoArquivo = donoArquivo;
    }

    /**
     * @return the nomeArquivo
     */
    public String getNomeArquivo() {
        return nomeArquivo;
    }

    /**
     * @param nomeArquivo the nomeArquivo to set
     */
    public void setNomeArquivo(String nomeArquivo) {
        this.nomeArquivo = nomeArquivo;
    }

    /**
     * @return the conteudoArquivo
     */
    public byte[] getConteudoArquivo() {
        return conteudoArquivo;
    }

    /**
     * @param conteudoArquivo the conteudoArquivo to set
     */
    public void setConteudoArquivo(byte[] conteudoArquivo) {
        this.conteudoArquivo = conteudoArquivo;
    }

    /**
     * @return the donoArquivo
     */
    public String getDonoArquivo() {
        return donoArquivo;
    }

    /**
     * @param donoArquivo the donoArquivo to set
     */
    public void setDonoArquivo(String donoArquivo) {
        this.donoArquivo = donoArquivo;
    }

}
