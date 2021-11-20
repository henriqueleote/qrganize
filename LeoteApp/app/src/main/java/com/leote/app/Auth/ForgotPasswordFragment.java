
/*  Coded by Henrique Leote - 2019/2020   */

//Esta classe é responsável por recuperar a conta do utilizador

package com.leote.app.Auth;

//Livrarias de Funções necessárias

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.leote.app.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ForgotPasswordFragment extends BottomSheetDialogFragment {

    //Declaração de variaveis
    private Button sendButton;                 //Botão de terminar
    private EditText editTextEmail;            //Caixa de texto
    private FirebaseAuth firebaseAuth;         //Variavel de autenticação da base de dados

    //Este bloco de código verifica se o e-mail é valido
    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //Associação desta classe ao layout "forgot_password_sheet"
        View view = inflater.inflate(R.layout.forgot_password_sheet, container, false);

        if (isNetworkConnected()) {
            //Inicializar o acesso à autenticação da base de dados
            firebaseAuth = FirebaseAuth.getInstance();

            sendButton = view.findViewById(R.id.buttonResetEmail);      //Associação do botão de enviar ao layout
            editTextEmail = view.findViewById(R.id.editTextEmail);      //Associação da variavel à caixa de texto do layout

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

            //Quando é clicado no botão de enviar o email
            sendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isNetworkConnected()) {
                        if (TextUtils.isEmpty(editTextEmail.getText().toString())) { //Caso não haja texto
                            editTextEmail.setError(getResources().getString(R.string.enter_your_mail));  //Apresentar um erro
                            editTextEmail.requestFocus(); //Apresentar um alerta
                            return;
                        }
                        if (isEmailValid(editTextEmail.getText().toString()) == true) { //Caso o email esteja correto
                            firebaseAuth.sendPasswordResetEmail(editTextEmail.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) { //Caso nao haja erros
                                        getDialog().cancel();
                                        Toast.makeText(getActivity(), getResources().getString(R.string.check_email), Toast.LENGTH_SHORT).show(); //Mensagem
                                    } else {    //Caso haja erros
                                        Toast.makeText(getActivity(), getResources().getString(R.string.no_found_email), Toast.LENGTH_SHORT).show(); //Mensagem
                                    }
                                }
                            });
                        } else {
                            editTextEmail.setError(getResources().getString(R.string.email_not_valid));  //Apresentar um erro
                            editTextEmail.requestFocus(); //Apresentar um alerta
                            return;
                        }
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