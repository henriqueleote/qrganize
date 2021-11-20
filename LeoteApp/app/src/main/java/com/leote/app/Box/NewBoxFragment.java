
/*  Coded by Henrique Leote - 2019/2020   */

//Esta classe é responsável pela página para criar a caixa

package com.leote.app.Box;

//Livrarias de Funções necessárias

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.leote.app.Adapters.Box;
import com.leote.app.Auth.StartActivity;
import com.leote.app.R;

import java.io.ByteArrayOutputStream;


public class NewBoxFragment extends Fragment {


    EditText nameEditText, localEditText, tipoEditText;         //Caixas de texto
    ImageView qrImage;                                          //Imagem de layout
    DatabaseReference databaseReference;                        //Variavel de acesso às dados da base de dados
    FirebaseAuth firebaseAuth;                                  //Variavel de autenticação da base de dados
    FloatingActionButton fab_main;                              //Variavel do botão de adicionar guardar
    TextView Back;                                              //Variavel de Texto


    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //Associação desta classe ao layout "fragment_box_create"
        View view = inflater.inflate(R.layout.fragment_box_create, container, false);

        if (isNetworkConnected()) {

        } else
            InternetMessageClose();

        //Inicializar o acesso à autenticação da base de dados
        firebaseAuth = FirebaseAuth.getInstance();

        //Este bloco verifica se há sessão iniciada, senão, abre a ativadade para autenticar
        if (firebaseAuth.getCurrentUser() == null) {
            getActivity().finish(); //terminar a ativadade atual
            getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);  //Adicionar uma animação quando entre as atividades
            startActivity(new Intent(getActivity(), StartActivity.class));  //Abrir uma nova atividade
        }

        fab_main = view.findViewById(R.id.fab);                                 //Associação do botão ao botão de layout
        databaseReference = FirebaseDatabase.getInstance().getReference();      //Inicializar a variavel do armazenamento
        nameEditText = view.findViewById(R.id.name);                            //Associação da variavel à caixa de texto do nome
        localEditText = view.findViewById(R.id.local);                          //Associação da variavel à caixa de texto do local
        tipoEditText = view.findViewById(R.id.tipo);                            //Associação da variavel à caixa de texto do tipo
        qrImage = view.findViewById(R.id.image);                                //Associar variavel de imagem à imagem do layout
        Back = view.findViewById(R.id.back);                        //Associação ao botão de voltar atrás


        //Quando clicado no botão, guarda as alterações
        fab_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isNetworkConnected())
                    SaveData();
                else
                    InternetMessage();
            }
        });

        //Quando se carrega, abre a página anterior
        Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isNetworkConnected()) {
                    getFragmentManager().popBackStack();
                } else
                    InternetMessage();
            }
        });

        return view;
    }

    public void SaveData() {
        final String name = nameEditText.getText().toString();              //Associar variavel ao texto da página
        final String tipo = tipoEditText.getText().toString();              //Associar variavel ao texto da página
        final String local = localEditText.getText().toString();            //Associar variavel ao texto da página

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(local) || TextUtils.isEmpty(tipo)) { //Caso algum campo não tenha texto
            if (TextUtils.isEmpty(name)) {  //Caso o campo do nome não tenha texto
                nameEditText.setError(getResources().getString(R.string.enter_box_name));    //Apresentar um erro
                nameEditText.requestFocus();    //Apresentar um alerta
                return;
            }

            if (TextUtils.isEmpty(tipo)) {  //Caso o campo do tipo não tenha texto
                tipoEditText.setError(getResources().getString(R.string.enter_box_type));    //Apresentar um erro
                tipoEditText.requestFocus();    //Apresentar um alerta
                return;
            }

            if (TextUtils.isEmpty(local)) {  //Caso o campo do local não tenha texto
                localEditText.setError(getResources().getString(R.string.enter_box_location));    //Apresentar um erro
                localEditText.requestFocus();    //Apresentar um alerta
                return;
            }

        } else {
            if (isNetworkConnected()) {
                final ProgressDialog mDialog = new ProgressDialog(getActivity());   //Iniciar a barra de progresso
                mDialog.setMessage(getResources().getString(R.string.please_wait));                         //Inserir uma mensagem
                mDialog.setCanceledOnTouchOutside(false);                           //Retirar a função de fechar a barra quando se carrega no resto do ecrã
                mDialog.show();                                                     //Mostrar a barra

                final String key = databaseReference.child(firebaseAuth.getCurrentUser().getUid()).child("Caixas").push().getKey();   //Associação da variavel ao ID da caixa

                MultiFormatWriter multiFormatWriter = new MultiFormatWriter();  //Classe que traduz texto para codigo de barras
                try {
                    BitMatrix bitMatrix = multiFormatWriter.encode(key, BarcodeFormat.QR_CODE, 400, 400);
                    BarcodeEncoder barcodeEncoder = new BarcodeEncoder();       //Classe que codifica o código
                    Bitmap imagem = barcodeEncoder.createBitmap(bitMatrix); //Codificar o código

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();   //Classe para exportar a imagem para jpeg
                    imagem.compress(Bitmap.CompressFormat.JPEG, 100, baos); //Exportar

                    final StorageReference imageRef = FirebaseStorage.getInstance().getReference().child("QRCodes/" + baos.toByteArray() + "." + "png");  //Caminho da pasta da imagem
                    UploadTask uploadTask = imageRef.putBytes(baos.toByteArray());  //upload da imagem para a base de dados
                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    Box boxName = new Box(name, tipo, local, uri.toString(), key);
                                    FirebaseDatabase.getInstance().getReference(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                            .child("Caixas").child(key).child("Dados") //Metodo para inserir os dados na base de dados
                                            .setValue(boxName).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {  //Caso não haja erros
                                                mDialog.dismiss();  //Parar a barra
                                                getFragmentManager().popBackStack();
                                                Toast.makeText(getActivity(), getResources().getString(R.string.box_create_successful), Toast.LENGTH_SHORT).show();  //Mensagem
                                            } else {    //Caso haja erros
                                                Toast.makeText(getActivity(), getResources().getString(R.string.error_please_try_again), Toast.LENGTH_SHORT).show();   //Mensagem
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            Toast.makeText(getActivity(), getResources().getString(R.string.error_please_try_again), Toast.LENGTH_SHORT).show();
                        }
                    });

                } catch (WriterException e) {
                    e.printStackTrace();
                }
            } else
                InternetMessage();
        }

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
