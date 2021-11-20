
/*  Coded by Henrique Leote - 2019/2020   */

//Esta classe é responsável por alterar a palavra passe de uma conta

package com.leote.app.Auth;

//Livrarias de Funções necessárias

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
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.leote.app.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ChangeEmailFragment extends BottomSheetDialogFragment {

    //Declaração de variaveis
    EditText editTextNewEmail, editTextPassword;     //Variaveis de texto
    Button buttonSave;                              //Variaveis de Botão
    FirebaseAuth firebaseAuth;                      //Variavel de autenticação da base de dados


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

        //Associação desta classe ao layout "change_email_sheet"
        View view = inflater.inflate(R.layout.change_email_sheet, container, false);

        if (isNetworkConnected()) {
            //Inicializar o acesso à autenticação da base de dados
            firebaseAuth = FirebaseAuth.getInstance();

            //Este bloco verifica se há sessão iniciada, senão, abre a ativadade para autenticar
            if (firebaseAuth.getCurrentUser() == null) {
                getActivity().finish();
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                startActivity(new Intent(getActivity(), StartActivity.class));
            }

            editTextNewEmail = view.findViewById(R.id.editTextEmail);            //Associar variavel à caixa de texto do layout
            editTextPassword = view.findViewById(R.id.editTextPassword);            //Associar variavel à caixa de texto do layout
            buttonSave = view.findViewById(R.id.buttonSave);                        //Associar variavel ao botão de alterar

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

            //Quando clicado no botao de alterar
            buttonSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isNetworkConnected())
                        ChangeEmail();   //Metodo para alterar a palavra passe
                    else
                        InternetMessage();
                }
            });

        } else {
            InternetMessageClose();
        }

        return view;
    }


    //Metodo que altera o email
    public void ChangeEmail() {
        if (TextUtils.isEmpty(editTextPassword.getText().toString())) {  //Caso não haja texto no campo da password
            editTextPassword.setError(getResources().getString(R.string.enter_your_password));  //Apresentar um erro
            editTextPassword.requestFocus(); //Apresentar um alerta
            return;
        }

        if (TextUtils.isEmpty(editTextNewEmail.getText().toString())) {  //Caso não haja texto no campo da password
            editTextNewEmail.setError(getResources().getString(R.string.enter_your_mail));  //Apresentar um erro
            editTextNewEmail.requestFocus(); //Apresentar um alerta
            return;
        }

        if (isEmailValid(editTextNewEmail.getText().toString()) == true) {
            //Caso o email esteja correto
            if (isNetworkConnected()) {
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();  //Guardar os dados do utilizador na variavel
                user.reauthenticate(EmailAuthProvider.getCredential(user.getEmail(), editTextPassword.getText().toString()))    //fazer a autenticação com o e-mail e password
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {  //caso não haja erros
                                    user.updateEmail(editTextNewEmail.getText().toString()) //altera o e-mail para o pretendido
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {  //caso não haja erros
                                                        getDialog().cancel();
                                                        Toast.makeText(getActivity(), getResources().getString(R.string.email_changed), Toast.LENGTH_SHORT).show();   //Mensagem
                                                    } else {    //caso haja erros
                                                        editTextNewEmail.setError(getResources().getString(R.string.email_use_please_try));  //Apresentar um erro
                                                        editTextNewEmail.requestFocus(); //Apresentar um alerta
                                                        return;
                                                    }
                                                }
                                            });
                                } else {    //caso haja erros
                                    editTextPassword.setError(getResources().getString(R.string.wrong_password));  //Apresentar um erro
                                    editTextPassword.requestFocus(); //Apresentar um alerta
                                    return;
                                }
                            }
                        });
            } else {
                InternetMessage();
            }
        } else {
            editTextNewEmail.setError(getResources().getString(R.string.email_not_valid));  //Apresentar um erro
            editTextNewEmail.requestFocus(); //Apresentar um alerta
            return;
        }


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
