
/*  Coded by Henrique Leote - 2019/2020   */

package com.leote.app.Adapters;

//Esta classe é responsável por guardar todos os dados dos utilizador

//Declaração das váriaveis da classe "UserInformation"
public class UserInformation {
    public String name;
    public String link;
    public String ano;

    public UserInformation() {
    }

    //Associação das variáveis da classe "UserInformation" aos valores recebidos da função pedida
    public UserInformation(String name, String link, String ano) {
        this.name = name;
        this.link = link;
        this.ano = ano;
    }
}
