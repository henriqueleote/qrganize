
/*  Coded by Henrique Leote - 2019/2020   */

package com.leote.app.Adapters;

//Esta classe é responsável por guardar todos os dados referentes aos produtos

//Declaração das váriaveis da classe "Produtos"
public class Produtos {
    String nome;
    String tipo;
    String cor;
    String quantidade;
    String link;
    String productBoxID;
    String productID;
    String descricao;

    public Produtos() {
    }

    //Associação das variáveis da classe "Produtos" aos valores recebidos da função pedida
    public Produtos(String nome, String tipo, String cor, String quantidade, String link, String productID, String productBoxID, String descricao) {
        this.nome = nome;
        this.tipo = tipo;
        this.cor = cor;
        this.quantidade = quantidade;
        this.link = link;
        this.productID = productID;
        this.productBoxID = productBoxID;
        this.descricao = descricao;

    }

    //Buscar ID da caixa do produto
    public String getProductBoxID() {
        return productBoxID;
    }

    //Definir ID da caixa do produto
    public void setProductBoxID(String productBoxID) {
        this.productBoxID = productBoxID;
    }

    //Buscar ID do produto pedido
    public String getProductID() {
        return productID;
    }

    //Definir ID do produto pedido
    public void setProductID(String productID) {
        this.productID = productID;
    }

    //Buscar ID do produto pedido
    public String getNome() {
        return nome;
    }

    //Definir nome do produto pedido
    public void setNome(String nome) {
        this.nome = nome;
    }

    //Buscar tipo do produto pedido
    public String getTipo() {
        return tipo;
    }

    //Definir tipo do produto pedido
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    //Buscar cor do produto pedido
    public String getCor() {
        return cor;
    }

    //Definir cor do produto pedido
    public void setCor(String cor) {
        this.cor = cor;
    }

    //Buscar quantidade do produto pedido
    public String getQuantidade() {
        return quantidade;
    }

    //Definir quantidade do produto pedido
    public void setQuantidade(String quantidade) {
        this.quantidade = quantidade;
    }

    //Buscar link da imagem do produto pedido
    public String getLink() {
        return link;
    }

    //Definir link da imagem do produto pedido
    public void setLink(String link) {
        this.link = link;
    }

    //Buscar descricao produto pedido
    public String getDescricao() {
        return descricao;
    }

    //Definir descricao do produto pedido
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }
}
