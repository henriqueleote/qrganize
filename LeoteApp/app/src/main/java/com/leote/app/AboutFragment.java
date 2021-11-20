
/*  Coded by Henrique Leote - 2019/2020   */

//Esta classe é responsável pela página do perfil do utilizador

package com.leote.app;

//Livrarias de Funções necessárias

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.leote.app.Auth.StartActivity;


public class AboutFragment extends BottomSheetDialogFragment {

    //Declaração de variaveis
    private FirebaseAuth firebaseAuth;            //Variavel de autenticação da base de dados


    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //Associação desta classe ao layout "about_sheet"
        View view = inflater.inflate(R.layout.about_sheet, container, false);

        //Inicializar o acesso à autenticação da base de dados
        firebaseAuth = FirebaseAuth.getInstance();


        //Este bloco verifica se há sessão iniciada, senão, abre a ativadade para autenticar
        if (firebaseAuth.getCurrentUser() == null) {
            getActivity().finish();
            getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            startActivity(new Intent(getActivity(), StartActivity.class));
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

        //Devolver dados para o ecrã
        return view;

    }

    public void InternetMessage() {
        new androidx.appcompat.app.AlertDialog.Builder(getActivity())
                .setMessage(getActivity().getResources().getString(R.string.internet_access_no))
                .setPositiveButton(getResources().getString(R.string.ok), null)
                .setCancelable(false)
                .show();
    }
}
