
/*  Coded by Henrique Leote - 2019/2020   */

//Esta classe é responsável pela página para abrir a caixa pretendida através do scan do codigo qr

package com.leote.app.Box;

//Livrarias de Funções necessárias

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.Result;
import com.leote.app.Adapters.FragmentClassData;
import com.leote.app.Auth.StartActivity;
import com.leote.app.R;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import pub.devrel.easypermissions.EasyPermissions;

import static com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE;


public class ScanFragment extends Fragment implements ZXingScannerView.ResultHandler {

    public static String[] perms = {  //Criar lista de strings com as permissoes
            Manifest.permission.CAMERA,
    };
    FirebaseAuth firebaseAuth;                          //Variavel de autenticação da base de dados
    ZXingScannerView scannerView;                       //Variavel para o scannerview da camara
    FloatingActionButton flash;                         //Botão para o flash

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //Associação desta classe ao layout "fragment_scan"
        View view = inflater.inflate(R.layout.fragment_scan, container, false);

        if (isNetworkConnected()) {
            scannerView = view.findViewById(R.id.scan);     //Associação do scanner ao layout
            flash = view.findViewById(R.id.fab);            //Associação do botão ao botão de layout
            firebaseAuth = FirebaseAuth.getInstance();      //Inicializar o acesso à autenticação da base de dados

            //Este bloco verifica se há sessão iniciada, senão, abre a ativadade para autenticar
            if (firebaseAuth.getCurrentUser() == null) {
                getActivity().finish(); //terminar a ativadade atual
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);  //Adicionar uma animação quando entre as atividades
                startActivity(new Intent(getActivity(), StartActivity.class));  //Abrir uma nova atividade
            }

            //Quando é clicado o botão do flash
            flash.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (scannerView.getFlash()) {    //caso o flash esteja ligado
                        flash.setImageDrawable(getResources().getDrawable(R.drawable.ic_flash_on_black_24dp));  //alterar icone
                        scannerView.setFlash(false);    //desligar o flash
                    } else {   //caso o flash esteja desligado
                        flash.setImageDrawable(getResources().getDrawable(R.drawable.ic_flash_off_black_24dp)); //alterar icone
                        scannerView.setFlash(true);     //ligar flash

                    }
                }
            });

            if (Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.LOLLIPOP) {  //Caso seja compativel
                if (!EasyPermissions.hasPermissions(getActivity(), perms)) { //caso nao haja permissão, pede
                    EasyPermissions.requestPermissions(getActivity(), getResources().getString(R.string.needed_permissions), REQUEST_CODE, perms);
                }
            }

        } else
            InternetMessageClose();

        return view;
    }

    //Durante a atividade da aplicação
    @SuppressLint("RestrictedApi")
    @Override
    public void onResume() {
        super.onResume();
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;   //verificar a versão do sistema
        if (currentapiVersion >= android.os.Build.VERSION_CODES.M) {    //caso seja compativel
            if (EasyPermissions.hasPermissions(getActivity(), perms)) {    //verifica permissoes
                if (scannerView == null) {   //caso a camara nao esteja iniciada
                    scannerView = new ZXingScannerView(getActivity());  //inicializar view
                    getActivity().setContentView(scannerView);  //definir camara na view
                }
                scannerView.setResultHandler(this); //iniciar função
                scannerView.startCamera();  //iniciar camara
            } else {
                EasyPermissions.requestPermissions(getActivity(), getResources().getString(R.string.needed_permissions), REQUEST_CODE, perms);    //pedir permissões
            }
        }
    }

    //Caso saia a aplicação, desliga o flash
    @Override
    public void onPause() {
        super.onPause();
        scannerView.stopCamera();           //desligar camara
        scannerView.stopCameraPreview();    //parar a view da camara
        scannerView.setFlash(false);        //desligar o flash

    }

    //Caso feche a aplicação, desliga o flash
    @Override
    public void onDestroy() {
        super.onDestroy();
        scannerView.stopCamera();           //desligar camara
        scannerView.stopCameraPreview();    //parar a view da camara
        scannerView.setFlash(false);        //desligar o flash
    }

    @Override
    public void handleResult(final Result result) {
        if (isNetworkConnected()) {
            if (result.toString().isEmpty() || result.toString().contains(".") || result.toString().contains("#") || result.toString().contains("$") || result.toString().contains("{") || result.toString().contains("]")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(getResources().getString(R.string.no_exist_box_try))
                        .setCancelable(false)
                        .setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                FragmentTransaction ft = getFragmentManager().beginTransaction();
                                ft.detach(ScanFragment.this).attach(ScanFragment.this).commit();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            } else {
                DatabaseReference uidRef = FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Caixas").child(result.toString());  //Caminho para a caixa
                ValueEventListener valueEventListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) { //caso a caixa exista, abre a caixa noutra página
                            FragmentClassData.value = result.toString();
                            OpenBoxFragment fragment = new OpenBoxFragment();
                            FragmentTransaction transaction = getFragmentManager().beginTransaction();
                            transaction.replace(R.id.fragment_container, fragment); // give your fragment container id in first parameter
                            transaction.addToBackStack(null);  // if written, this transaction will be added to backstack
                            transaction.commit();

                        } else {  //caso não haja caixa
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setMessage(getResources().getString(R.string.no_exist_box_try))
                                    .setCancelable(false)
                                    .setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            FragmentTransaction ft = getFragmentManager().beginTransaction();
                                            ft.detach(ScanFragment.this).attach(ScanFragment.this).commit();
                                        }
                                    });
                            AlertDialog alert = builder.create();
                            alert.show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(getActivity(), getResources().getString(R.string.error_please_try_again), Toast.LENGTH_SHORT).show(); //Mensagem
                    }
                };
                uidRef.addListenerForSingleValueEvent(valueEventListener);
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
                                getActivity().finishAffinity();
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