
/*  Coded by Henrique Leote - 2019/2020   */

//Esta classe é responsável pela autenticação do utilizador

package com.leote.app.Auth;

//Livrarias de Funções necessárias

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.leote.app.MainActivity;
import com.leote.app.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class LoginFragment extends BottomSheetDialogFragment {

    //Declaração de variaveis
    private Button buttonSignin;                            //Botão de Login
    private EditText editTextEmail, editTextPassword;       //Caixas de texto
    private ProgressDialog progressDialog;                  //Barra de Progresso
    private FirebaseAuth firebaseAuth;                      //Variavel de autenticação da base de dados

    //Este metodo verifica se o e-mail é valido
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

        //Associação desta classe ao layout "login_sheet"
        View view = inflater.inflate(R.layout.login_sheet, container, false);

        if (isNetworkConnected()) {
            final CheckBox toggle = view.findViewById(R.id.toggBtn);        //Associação da variavel à caixa de toggle
            //Caso a toggle seja clicada
            toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {    //Caso a toggle esteja checked
                        editTextPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance()); //Mostra a palavra-passe
                        toggle.setText(getResources().getString(R.string.hide_password));
                    } else {            //Caso a toggle esteja non-checked
                        editTextPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());   //Esconder a palavra-passe
                        toggle.setText(getResources().getString(R.string.show_password));
                    }
                }
            });

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

            //Inicializar o acesso à autenticação da base de dados
            firebaseAuth = FirebaseAuth.getInstance();

            //Este bloco verifica se há sessão iniciada, senão, abre a ativadade para autenticar
            if (firebaseAuth.getCurrentUser() != null) {
                //Ativação do perfil
                getActivity().finish();
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                startActivity(new Intent(getActivity().getApplicationContext(), MainActivity.class));
            }

            progressDialog = new ProgressDialog(getActivity());             //Iniciar a barra de progresso
            buttonSignin = view.findViewById(R.id.buttonSignin);            //Associação da variavel ao botao de login
            editTextEmail = view.findViewById(R.id.editTextEmail);          //Associação da variavel à caixa de texto do e-mail
            editTextPassword = view.findViewById(R.id.editTextPassword);    //Associação da variavel à caixa de texto da password

            //Quando clicado no botao de login
            buttonSignin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isNetworkConnected())
                        userLogin(); //Declaração do metodo de login do utilizador
                    else
                        InternetMessage();
                }
            });
        } else
            InternetMessageClose();

        return view;
    }

    //Metodo de login do utilizador
    private void userLogin() {
        if (isNetworkConnected()) {
            if (TextUtils.isEmpty(editTextEmail.getText().toString())) {  //Caso não haja texto no campo do e-mail
                editTextEmail.setError(getResources().getString(R.string.enter_your_mail));  //Apresentar um erro
                editTextEmail.requestFocus(); //Apresentar um alerta
                return;
            }
            if (TextUtils.isEmpty(editTextPassword.getText().toString())) {  //Caso não haja texto no campo da password
                editTextPassword.setError(getResources().getString(R.string.enter_your_password));  //Apresentar um erro
                editTextPassword.requestFocus(); //Apresentar um alerta
                return;
            }
            if (isEmailValid(editTextEmail.getText().toString()) == true) {   //Caso o email esteja correto
                progressDialog.setMessage(getResources().getString(R.string.logging_in)); //Definir mensagem para a barra de progresso
                progressDialog.show();  //Mostrar barra de progresso
                firebaseAuth.signInWithEmailAndPassword(editTextEmail.getText().toString(), editTextPassword.getText().toString()) //Autenticação
                        .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressDialog.dismiss();
                                if (task.isSuccessful()) {   //Caso nao haja erros
                                    getActivity().finish(); //Terminar a ativadade
                                    startActivity(new Intent(getActivity().getApplicationContext(), MainActivity.class)); //Iniciar a ativadade principal
                                } else {     //Caso haja erros
                                    Toast.makeText(getActivity(), getResources().getString(R.string.check_credentials), Toast.LENGTH_SHORT).show(); //Mensagem
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