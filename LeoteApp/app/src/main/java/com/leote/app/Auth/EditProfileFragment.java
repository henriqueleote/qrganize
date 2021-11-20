
/*  Coded by Henrique Leote - 2019/2020   */

//Esta classe é responsável por modificar os dados do utilizador

package com.leote.app.Auth;

//Livrarias de Funções necessárias

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.leote.app.Adapters.UserInformation;
import com.leote.app.R;
import com.squareup.picasso.Picasso;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

import static android.app.Activity.RESULT_OK;

public class EditProfileFragment extends BottomSheetDialogFragment {

    //Declaração de variaveis
    public static String year;                              //Variavel que guarda o ano
    public static String link;                              //Variavel que guarda o link
    public static Drawable oldDrawable;                     //Variavel que guarda a imagem
    private ImageView img;                                  //Imagem de layout
    private Uri imguri;                                     //Variavel com o endereço da imagem
    private FirebaseAuth firebaseAuth;                      //Variavel de autenticação da base de dados
    private Button buttonSave;                              //Botão de Guardar
    private EditText editTextName;                          //Caixas de texto


    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //Associação desta classe ao layout "edit_profile_sheet"
        View view = inflater.inflate(R.layout.edit_profile_sheet, container, false);

        if (isNetworkConnected()) {
            Data(); //Declaração do metodo dos Dados

            //Inicializar o acesso à autenticação da base de dados
            firebaseAuth = FirebaseAuth.getInstance();

            //Este bloco verifica se há sessão iniciada, senão, abre a ativadade para autenticar
            if (firebaseAuth.getCurrentUser() == null) {
                getActivity().finish();
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                startActivity(new Intent(getActivity(), StartActivity.class));
            }

            img = view.findViewById(R.id.mImageView);   //Associar variavel de imagem à imagem do layout

            oldDrawable = img.getDrawable();    //Guardar a imagem na variavel

            //Quando é clicado na imagem, abre uma atividade para escolher uma imagem
            img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isNetworkConnected())
                        FileChooser();  //Declaração do metodo de escolha de imagem
                    else
                        InternetMessage();

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

            editTextName = view.findViewById(R.id.editTextName);            //Associação da variavel à caixa de texto do layout
            buttonSave = view.findViewById(R.id.buttonSave);                //Associação do botão de gravar ao layout

            //Quando é clicado o botão de guardar
            buttonSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isNetworkConnected())
                        saveUserInformation();  //Declaração do metodo de guardar os dados do utilizador
                    else
                        InternetMessage();
                }
            });
        } else
            InternetMessageClose();

        //Devolver dados para o ecrã
        return view;

    }

    //Este bloco de código abre uma atividade para o utilizador escolher uma imagem
    private void FileChooser() {
        Intent intent = new Intent();                   //Inicializar a atividade
        intent.setType("image/*");                      //Definir tipo de ficheiro
        intent.setAction(Intent.ACTION_GET_CONTENT);    //Definir o tipo de ação
        startActivityForResult(intent, 1);  //Abrir a atividade
    }


    // A FUNCIONAR
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (isNetworkConnected()) {
            if (requestCode == 1 && resultCode == RESULT_OK && data != null & data.getData() != null) {

                imguri = data.getData();    //Ir buscar a imagem escolhida na atividade das linhas 124-128
                Picasso.get()               //Utilizar o metodo Picasso para inserir a imagem
                        .load(imguri)       //Carregar a imagem
                        .centerCrop()       //Recortar a imagem ao centro
                        .fit()              //Colocar a imagem de maneira a caber na ImageView
                        .transform(new CropCircleTransformation())  //Editar para ficar redonda
                        .into(img);         //Inserir na ImageView


                final ProgressDialog mDialog = new ProgressDialog(getActivity());   //Iniciar a barra de progresso
                mDialog.setMessage(getResources().getString(R.string.saving_image));       //Inserir uma mensagem
                mDialog.setCanceledOnTouchOutside(false);                           //Retirar a função de fechar a barra quando se carrega no resto do ecrã
                mDialog.show();                                                     //Mostrar a barra


                StorageReference storageReference = FirebaseStorage.getInstance().getReference();   //Inicializar a variavel de armazenamento da base de dados
                final StorageReference imageRef = storageReference.child("Images/" + imguri.getLastPathSegment());  //Inicializar a referencia do armazenamento onde a imagem será guardada
                UploadTask uploadTask = imageRef.putFile(imguri);   //Inicializar a variavel para fazer upload da imagem

                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        mDialog.dismiss();  //Parar a barra de progresso
                        Task<Uri> downloadUrl = imageRef.getDownloadUrl(); //Guardar o link da imagem numa variavel
                        downloadUrl.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                link = uri.toString(); //Associar o link a uma variavel
                            }
                        });
                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                mDialog.dismiss();  //Parar a barra de progresso
                                Toast.makeText(getActivity(), "Ocorreu um erro, por favor tente de novo", Toast.LENGTH_SHORT).show(); //Mensagem
                            }
                        });
            }
        } else
            InternetMessage();

    }

    public void Data() {
        if (isNetworkConnected()) {
            DatabaseReference uidRef = FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Profile"); //Guardar na variavel o caminho para os dados da utilizador
            ValueEventListener valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    link = dataSnapshot.child("link").getValue(String.class);                       //Guardar o link da imagem do utilizador
                    year = dataSnapshot.child("ano").getValue(String.class);                        //Guardar o ano
                    editTextName.setText(dataSnapshot.child("name").getValue(String.class));        //Escrever o nome

                    Picasso.get()                                           //Utilizar o metodo Picasso para inserir a imagem
                            .load(link)                                     //Carregar a imagem
                            .centerCrop()                                   //Recortar a imagem ao centro
                            .fit()                                          //Colocar a imagem de maneira a caber na ImageView
                            .transform(new CropCircleTransformation())      //Editar para ficar redonda
                            .into(img);                                     //Inserir na ImageView

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    getDialog().cancel();
                    Toast.makeText(getActivity(), getResources().getString(R.string.error_please_try_again), Toast.LENGTH_SHORT).show(); //Mensagem
                }
            };
            uidRef.addListenerForSingleValueEvent(valueEventListener); //Guardar as alterações
        } else
            InternetMessage();
    }

    //Guardar os valores na base de dados
    private void saveUserInformation() {
        if (isNetworkConnected()) {
            if (TextUtils.isEmpty(editTextName.getText().toString()) || link.isEmpty()) {
                if (link.isEmpty()) {   //Caso não haja um link
                    Toast.makeText(getActivity(), getResources().getString(R.string.mandatory_picture_to_create), Toast.LENGTH_SHORT).show(); //Mensagem
                }
                if (TextUtils.isEmpty(editTextName.getText().toString())) { //Caso não haja texto
                    editTextName.setError(getResources().getString(R.string.enter_your_name));  //Apresentar um erro
                    editTextName.requestFocus(); //Apresentar um alerta
                    return;
                }
            } else {
                UserInformation userInformation = new UserInformation(editTextName.getText().toString(), link, year);                      //Guardar os dados na classe "UserInformation"
                FirebaseDatabase.getInstance().getReference(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child("Profile") //Metodo para inserir os dados na base de dados
                        .setValue(userInformation).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {  //Caso não haja erros
                            getDialog().cancel();
                            Toast.makeText(getActivity(), getResources().getString(R.string.data_success_changed_refresh), Toast.LENGTH_SHORT).show();          //Mensagem
                        } else {    //Caso haja erros
                            Toast.makeText(getActivity(), getResources().getString(R.string.error_please_try_again), Toast.LENGTH_SHORT).show();   //Mensagem
                        }
                    }
                });
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
