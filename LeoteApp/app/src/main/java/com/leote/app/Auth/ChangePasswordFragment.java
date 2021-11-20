
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


public class ChangePasswordFragment extends BottomSheetDialogFragment {

    //Declaração de variaveis
    EditText editTextPassword, editTextNewPassword, editTextNewPassword2;     //Variaveis de texto
    Button buttonSave;                                                      //Variaveis de Botão
    CheckBox toggle;                                                        //Variavel de Checkbox
    FirebaseAuth firebaseAuth;                                              //Variavel de autenticação da base de dados


    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //Associação desta classe ao layout "change_password_sheet"
        View view = inflater.inflate(R.layout.change_password_sheet, container, false);

        if (isNetworkConnected()) {
            //Inicializar o acesso à autenticação da base de dados
            firebaseAuth = FirebaseAuth.getInstance();

            //Este bloco verifica se há sessão iniciada, senão, abre a ativadade para autenticar
            if (firebaseAuth.getCurrentUser() == null) {
                getActivity().finish();
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                startActivity(new Intent(getActivity(), StartActivity.class));
            }

            editTextPassword = view.findViewById(R.id.editTextPassword);            //Associar variavel à caixa de texto do layout
            editTextNewPassword = view.findViewById(R.id.editTextNewPassword);          //Associar variavel à caixa de texto do layout
            editTextNewPassword2 = view.findViewById(R.id.editTextNewPassword2);          //Associar variavel à caixa de texto do layout
            toggle = view.findViewById(R.id.toggBtn);                               //Associar variavel à caixa de toggle do layout
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

            //Caso a toggle seja clicada
            toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {    //Caso a toggle esteja checked
                        editTextPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());  //Mostra a palavra-passe
                        editTextNewPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance()); //Mostra a palavra-passe
                        editTextNewPassword2.setTransformationMethod(HideReturnsTransformationMethod.getInstance()); //Mostra a palavra-passe
                        toggle.setText(getResources().getString(R.string.hide_password));
                    } else {            //Caso a toggle esteja non-checked
                        editTextPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());     //Esconder a palavra-passe
                        editTextNewPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());    //Esconder a palavra-passe
                        editTextNewPassword2.setTransformationMethod(PasswordTransformationMethod.getInstance());    //Esconder a palavra-passe
                        toggle.setText(getResources().getString(R.string.show_password));
                    }
                }
            });


            //Quando clicado no botao de alterar
            buttonSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isNetworkConnected())
                        ChangePassword();   //Metodo para alterar a palavra passe
                    else
                        InternetMessage();
                }
            });
        } else {
            InternetMessageClose();
        }

        return view;
    }


    //Metodo que altera a palavra passe
    public void ChangePassword() {

        if (TextUtils.isEmpty(editTextPassword.getText().toString())) { //Caso não haja texto no campo da palavra-passe
            editTextPassword.setError(getResources().getString(R.string.enter_your_password));  //Apresentar um erro
            editTextPassword.requestFocus(); //Apresentar um alerta
            return;
        }

        if (editTextNewPassword.getText().toString().length() < 8) {  //Caso a palavra-passe nao tenha 8 caracteres
            editTextNewPassword.setError(getResources().getString(R.string.eight_password));  //Apresentar um erro
            editTextNewPassword.requestFocus(); //Apresentar um alerta
            return;
        }

        if (editTextNewPassword2.getText().toString().length() < 8) {  //Caso a palavra-passe nao tenha 8 caracteres
            editTextNewPassword2.setError(getResources().getString(R.string.eight_password));  //Apresentar um erro
            editTextNewPassword2.requestFocus(); //Apresentar um alerta
            return;
        }

        if (TextUtils.isEmpty(editTextNewPassword.getText().toString())) { //Caso não haja texto no campo da palavra-passe
            editTextNewPassword.setError(getResources().getString(R.string.enter_password));  //Apresentar um erro
            editTextNewPassword.requestFocus(); //Apresentar um alerta
            return;
        }

        if (TextUtils.isEmpty(editTextNewPassword2.getText().toString())) { //Caso não haja texto no campo da palavra-passe
            editTextNewPassword2.setError(getResources().getString(R.string.enter_password));  //Apresentar um erro
            editTextNewPassword2.requestFocus(); //Apresentar um alerta
            return;
        }

        if (editTextNewPassword.getText().toString().equals(editTextNewPassword2.getText().toString())) { //Caso as palavras-passe coincidam
            if (isNetworkConnected()) {
                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();  //Guardar os dados do utilizador na variavel
                user.reauthenticate(EmailAuthProvider.getCredential(user.getEmail(), editTextPassword.getText().toString()))    //fazer a autenticação com o e-mail e password
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    user.updatePassword(editTextNewPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                getDialog().cancel();
                                                Toast.makeText(getActivity(), getResources().getString(R.string.password_changed), Toast.LENGTH_SHORT).show();   //Mensagem
                                            } else {
                                                Toast.makeText(getActivity(), getResources().getString(R.string.error_please_try_again), Toast.LENGTH_SHORT).show();   //Mensagem
                                            }
                                        }
                                    });
                                } else {
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
            Toast.makeText(getActivity(), getResources().getString(R.string.passwords_dont_match), Toast.LENGTH_SHORT).show(); //Mensagem
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