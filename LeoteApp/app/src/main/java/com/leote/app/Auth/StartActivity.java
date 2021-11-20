
/*  Coded by Henrique Leote - 2019/2020   */

//Esta classe é responsável pela página inicial da aplicação

package com.leote.app.Auth;

//Livrarias de Funções necessárias

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.leote.app.MainActivity;
import com.leote.app.R;

import pub.devrel.easypermissions.EasyPermissions;

import static com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE;

public class StartActivity extends AppCompatActivity {

    //Declaração de variaveis
    private Button login_btn, register_btn;             //Botão de Login e Registo
    private FirebaseAuth firebaseAuth;                  //Variavel de autenticação da base de dados
    private TextView textViewForgotPassword;            //Variavel de texto


    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Associação desta classe ao layout "activity_start"
        setContentView(R.layout.activity_start);

        if (isNetworkConnected()) {
            //Inicializar o acesso à autenticação da base de dados
            firebaseAuth = FirebaseAuth.getInstance();

            //Este bloco verifica se há sessão iniciada, senão, abre a ativadade para autenticar
            if (firebaseAuth.getCurrentUser() != null) {
                finish();
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                startActivity(new Intent(StartActivity.this, MainActivity.class));
            }

            textViewForgotPassword = findViewById(R.id.textViewForgotPassword);             //Associação da variavel à caixa de texto do nome
            login_btn = findViewById(R.id.login);                                           //Associação da variavel ao botao de login
            register_btn = findViewById(R.id.register);                                     //Associação da variavel ao botao de registo

            //Quando é clicado no botão de iniciar sessão, abre a atividade de inicio de sessão
            login_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isNetworkConnected()) {
                        LoginFragment bottomSheetFragment = new LoginFragment();
                        bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
                    } else
                        InternetMessage();
                }
            });


            //Quando é clicado no botão de registar, abre a atividade de registo
            register_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isNetworkConnected()) {
                        RegisterFragment bottomSheetFragment = new RegisterFragment();
                        bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
                    } else
                        InternetMessage();

                }
            });

            //Quando é clicado no texto da palavra-passe, abre a atividade de recuperar a palavra-passe
            textViewForgotPassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isNetworkConnected()) {
                        ForgotPasswordFragment bottomSheetFragment = new ForgotPasswordFragment();
                        bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
                    } else
                        InternetMessage();
                }
            });

            if (Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.LOLLIPOP) {  //Caso seja compativel
                String[] perms = {  //Criar lista de strings com as permissoes
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA,
                };
                if (!EasyPermissions.hasPermissions(this, perms)) { //caso nao haja permissão, pede
                    EasyPermissions.requestPermissions(this, getResources().getString(R.string.needed_permissions), REQUEST_CODE, perms);
                }
            }


        } else
            InternetMessageClose();

    }

    //Metodo para pedir autorizações
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //Abrir janela para pedir autorizações
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.requestPermissions(this, getResources().getString(R.string.all_permission_needed), requestCode, permissions);
    }

    public void InternetMessageClose() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setMessage(this.getResources().getString(R.string.internet_access_no))
                .setPositiveButton(getResources().getString(R.string.ok), new
                        DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                finishAffinity();
                                System.exit(0);
                            }
                        })
                .setCancelable(false)
                .show();
    }

    public void InternetMessage() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setMessage(this.getResources().getString(R.string.internet_access_no))
                .setPositiveButton(getResources().getString(R.string.ok), null)
                .setCancelable(false)
                .show();
    }

}
