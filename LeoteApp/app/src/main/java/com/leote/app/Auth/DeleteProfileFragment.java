
/*  Coded by Henrique Leote - 2019/2020   */

//Esta classe é responsável por apresentar os dados das caixas na página principal "HomeFragment"

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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.leote.app.R;

public class DeleteProfileFragment extends BottomSheetDialogFragment {

    //Declaração de variaveis
    public FirebaseAuth firebaseAuth;           //Variavel de autenticação da base de dados
    public DatabaseReference databaseReference; //Variavel de acesso às dados da base de dados
    public FirebaseUser user;                   //Variavel do utilizador da base de dados
    private Button Yes, No;                     //Botões de Sim e Não
    private EditText editTextPassword;          //Caixa d etexto


    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //Associação desta classe ao layout "delete_profile_sheet"
        View view = inflater.inflate(R.layout.delete_profile_sheet, container, false);

        if (isNetworkConnected()) {
            //Inicializar o acesso à autenticação da base de dados
            firebaseAuth = FirebaseAuth.getInstance();

            //Inicializar o acesso aos dados da base de dados
            databaseReference = FirebaseDatabase.getInstance().getReference();

            Yes = view.findViewById(R.id.Yes);  //Associar variavel "Sim" ao botao do layout
            No = view.findViewById(R.id.No);    //Associar variavel "Não" ao botao do layout

            //Este bloco verifica se há sessão iniciada, senão, abre a ativadade para autenticar
            if (firebaseAuth.getCurrentUser() == null) {
                getActivity().finish();
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                startActivity(new Intent(getActivity(), StartActivity.class));
            }

            editTextPassword = view.findViewById(R.id.editTextPassword);            //Associar variavel à caixa de texto do layout

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

            //Quando é clicado no botão de "Não"
            No.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isNetworkConnected()) {
                        getDialog().cancel();
                        Toast.makeText(getActivity(), getResources().getString(R.string.no_account_delete), Toast.LENGTH_SHORT).show(); //Mensagem
                    } else
                        InternetMessage();
                }
            });

            //Quando é clicado no botão de "Sim"
            Yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (TextUtils.isEmpty(editTextPassword.getText().toString())) {  //Caso não haja texto no campo da password
                        editTextPassword.setError(getResources().getString(R.string.enter_your_password));  //Apresentar um erro
                        editTextPassword.requestFocus(); //Apresentar um alerta
                        return;
                    }

                    if (isNetworkConnected()) {
                        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();  //Guardar os dados do utilizador na variavel
                        user.reauthenticate(EmailAuthProvider.getCredential(user.getEmail(), editTextPassword.getText().toString()))    //fazer a autenticação com o e-mail e password
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {  //caso não haja erros
                                            user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) { //Caso seja eliminada com sucesso
                                                        Toast.makeText(getActivity(), getResources().getString(R.string.account_delete_successful), Toast.LENGTH_SHORT).show(); //Mensagem
                                                        getActivity().finish();
                                                        startActivity(new Intent(getActivity(), StartActivity.class)); //Terminar atividade
                                                    } else { //Caso não seja eliminada com sucesso
                                                        Toast.makeText(getActivity(), getResources().getString(R.string.account_delete_error), Toast.LENGTH_SHORT).show(); //Mensagem
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
                    } else
                        InternetMessage();
                }
            });

        } else {
            InternetMessageClose();
        }

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
