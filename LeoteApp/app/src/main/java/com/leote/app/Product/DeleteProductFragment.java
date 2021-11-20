
/*  Coded by Henrique Leote - 2019/2020   */

//Esta classe é responsável por eliminar produtos

package com.leote.app.Product;

//Livrarias de Funções necessárias

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.leote.app.Adapters.FragmentClassData;
import com.leote.app.Auth.StartActivity;
import com.leote.app.Box.HomeFragment;
import com.leote.app.Box.OpenBoxFragment;
import com.leote.app.R;

public class DeleteProductFragment extends BottomSheetDialogFragment {

    //Declaração de variaveis
    public FirebaseAuth firebaseAuth;                //Variavel de autenticação da base de dados
    private Button Yes, No;                          //Botões de Sim e Não


    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //Associação desta classe ao layout "delete_product_sheet"
        View view = inflater.inflate(R.layout.delete_product_sheet, container, false);

        if (isNetworkConnected()) {
            //Inicializar o acesso à autenticação da base de dados
            firebaseAuth = FirebaseAuth.getInstance();


            Yes = view.findViewById(R.id.Yes);  //Associar variavel "Sim" ao botao do layout
            No = view.findViewById(R.id.No);    //Associar variavel "Não" ao botao do layout


            //Este bloco verifica se há sessão iniciada, senão, abre a ativadade para autenticar
            if (firebaseAuth.getCurrentUser() == null) {
                getActivity().finish(); //terminar a ativadade atual
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);  //Adicionar uma animação quando entre as atividades
                startActivity(new Intent(getActivity(), StartActivity.class));  //Abrir uma nova atividade
            }

            ImageView close = view.findViewById(R.id.close);                        //Associar imagem ao layout

            //Quando é carregado no fechar, fecha a página
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isNetworkConnected())
                        getDialog().cancel();
                    else
                        InternetMessage();
                }
            });

            //Quando é clicado no botão de "Não"
            No.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isNetworkConnected()) {
                        getDialog().cancel();
                        Toast.makeText(getActivity(), getResources().getString(R.string.no_product_delete), Toast.LENGTH_SHORT).show();    //Mensagem
                    } else
                        InternetMessage();
                }
            });


            //Quando é clicado no botão de "Sim"
            Yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isNetworkConnected()) {
                        //eliminar o produto da base da dados
                        FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Caixas").child(FragmentClassData.value).child("Artigos").child(FragmentClassData.value2).removeValue();
                        //Abre a página da caixa
                        getDialog().cancel();
                        OpenBoxFragment boxFragment= new OpenBoxFragment();
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, boxFragment, "Fragment")
                                .addToBackStack(null)
                                .commit();
                        Toast.makeText(getActivity(), getResources().getString(R.string.product_delete_successful), Toast.LENGTH_SHORT).show(); //Mensagem de aviso
                    } else
                        InternetMessage();

                }
            });
        } else
            InternetMessageClose();

        return view;
    }

    public void InternetMessageClose() {
        new androidx.appcompat.app.AlertDialog.Builder(getActivity())
                .setMessage(getActivity().getResources().getString(R.string.internet_access_no))
                .setPositiveButton(getResources().getString(R.string.ok), new
                        DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                System.exit(0);
                            }
                        })
                .setCancelable(false)
                .show();
    }

    public void InternetMessage() {
        new androidx.appcompat.app.AlertDialog.Builder(getActivity())
                .setMessage(getActivity().getResources().getString(R.string.internet_access_no))
                .setPositiveButton(getResources().getString(R.string.ok), null)
                .setCancelable(false)
                .show();
    }
}
