
/*  Coded by Henrique Leote - 2019/2020   */

package com.leote.app.Adapters;

//Esta classe é responsável por guardar todos os dados referentes às caixas

//Declaração das váriaveis da classe "Box"
public class Box {
    String name;
    String tipo;
    String local;
    String link;
    String boxID;


    public Box() {
    }

    //Associação das variáveis da classe "Box" aos valores recebidos da função pedida
    public Box(String name, String tipo, String local, String link, String boxID) {
        this.name = name;
        this.tipo = tipo;
        this.local = local;
        this.link = link;
        this.boxID = boxID;
    }

    //Buscar nome
    public String getName() {
        return name;
    }

    //Definir nome
    public void setName(String name) {
        this.name = name;
    }

    //Buscar tipo
    public String getTipo() {
        return tipo;
    }

    //Definir tipo
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    //Buscar local
    public String getLocal() {
        return local;
    }

    //Definir local
    public void setLocal(String local) {
        this.local = local;
    }

    //Buscar link da imagem
    public String getLink() {
        return link;
    }

    //Definir link da imagem
    public void setLink(String link) {
        this.link = link;
    }

    //Buscar ID da caixa pedida
    public String getboxID() {
        return boxID;
    }

}
