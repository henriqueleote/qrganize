
/*  Coded by Henrique Leote - 2019/2020   */

//Esta classe é responsável pela página de criação de produtos

package com.leote.app.Product;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.leote.app.Adapters.FragmentClassData;
import com.leote.app.Adapters.Produtos;
import com.leote.app.Auth.StartActivity;
import com.leote.app.R;
import com.squareup.picasso.Picasso;

import static android.app.Activity.RESULT_OK;


public class NewProductFragment extends Fragment {

    public static String link;                                                                                                          //Variavel do link
    TextView TextNomeArtigo, TextTipoArtigo, TextCorArtigo, TextQuantidadeArtigo, BoxName, TextDescricaoArtigo,Back;                    //Variaveis de texto
    ImageView ImageFotografiaArtigo;                                                                                                    //Variavel de imagem
    FloatingActionButton button;                                                                                                        //Variavel de botao
    Drawable oldDrawable;                                                                                                               //Variavel que guarda a imagem
    Uri imguri;                                                                                                                         //Variavel com o endereço da imagem
    FirebaseAuth firebaseAuth;                                                                                                          //Variavel de acesso à autenticação da base de dados


    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //Associação desta classe ao layout "fragment_product_create"
        View view = inflater.inflate(R.layout.fragment_product_create, container, false);

        if (isNetworkConnected()) {
            //Inicializar o acesso à autenticação da base de dados
            firebaseAuth = FirebaseAuth.getInstance();

            //Este bloco verifica se há sessão iniciada, senão, abre a ativadade para autenticar
            if (firebaseAuth.getCurrentUser() == null) {
                getActivity().finish(); //terminar a ativadade atual
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);  //Adicionar uma animação quando entre as atividades
                startActivity(new Intent(getActivity(), StartActivity.class));  //Abrir uma nova atividade
            }

            //Inicializar a variavel do armazenamento para ir buscar o nome da caixa
            FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Caixas").child(FragmentClassData.value).child("Dados").child("name").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    BoxName.setText(snapshot.getValue().toString());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    BoxName.setText(getResources().getString(R.string.box_name));
                }
            });

            BoxName = view.findViewById(R.id.boxName);                          //Associação da variavel ao nome da caixa
            TextNomeArtigo = view.findViewById(R.id.NomeArtigo);                //Associação da variavel à caixa de texto do nome
            TextDescricaoArtigo = view.findViewById(R.id.DescricaoArtigo);      //Associação da variavel à caixa de texto da descrição
            TextTipoArtigo = view.findViewById(R.id.TipoArtigo);                //Associação da variavel à caixa de texto do tipo
            TextCorArtigo = view.findViewById(R.id.CorArtigo);                  //Associação da variavel à caixa de texto da cor
            TextQuantidadeArtigo = view.findViewById(R.id.QuantidadeArtigo);    //Associação da variavel à caixa de texto da quantidade
            ImageFotografiaArtigo = view.findViewById(R.id.ImagemArtigo);       //Associação à imagem do layout
            button = view.findViewById(R.id.fab_button);                        //Associação ao botao do layout
            oldDrawable = ImageFotografiaArtigo.getDrawable();                  //Guardar a imagem atual na variavel
            Back = view.findViewById(R.id.back);                        //Associação ao botão de voltar atrás

            //Quando é clicado na imagem, abre uma atividade para escolher uma imagem
            ImageFotografiaArtigo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isNetworkConnected())
                        FileChooser();  //Declaração do metodo de escolha de imagem
                    else
                        InternetMessage();
                }
            });

            //Quando é clicado no para guardar
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isNetworkConnected())
                        Create();
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
        } else
            InternetMessageClose();

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

                imguri = data.getData();    //Ir buscar a imagem escolhida
                Picasso.get()               //Utilizar o metodo Picasso para inserir a imagem
                        .load(imguri)       //Carregar a imagem
                        .centerCrop()       //Recortar a imagem ao centro
                        .fit()              //Colocar a imagem de maneira a caber na ImageView
                        .into(ImageFotografiaArtigo);

                if (isNetworkConnected()) {
                    final ProgressDialog mDialog = new ProgressDialog(getActivity());   //Iniciar a barra de progresso
                    mDialog.setMessage(getResources().getString(R.string.saving_image));       //Inserir uma mensagem
                    mDialog.setCanceledOnTouchOutside(false);                           //Retirar a função de fechar a barra quando se carrega no resto do ecrã
                    mDialog.show();                                                     //Mostrar a barra


                    StorageReference storageReference = FirebaseStorage.getInstance().getReference();   //Inicializar a variavel de armazenamento da base de dados
                    final StorageReference imageRef = storageReference.child("Products/" + imguri.getLastPathSegment());  //Inicializar a referencia do armazenamento onde a imagem será guardada
                    UploadTask uploadTask = imageRef.putFile(imguri);   //Inicializar a variavel para fazer upload da imagem

                    uploadTask
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
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
                                    Toast.makeText(getActivity(), getResources().getString(R.string.error_please_try_again), Toast.LENGTH_SHORT).show(); //Mensagem
                                }
                            });
                } else
                    InternetMessage();
            }
        } else
            InternetMessage();

    }


    private void Create() {

        if (ImageFotografiaArtigo.getDrawable() == oldDrawable) {   //Caso nao insira imagem
            Toast.makeText(getActivity(), getResources().getString(R.string.mandatory_product_to_create), Toast.LENGTH_SHORT).show(); //Mensagem
        } else {    //Caso insira imagem

            if (link.isEmpty() || TextUtils.isEmpty(TextNomeArtigo.getText().toString()) || TextUtils.isEmpty(TextTipoArtigo.getText().toString()) || TextUtils.isEmpty(TextQuantidadeArtigo.getText().toString()) || TextUtils.isEmpty(TextCorArtigo.getText().toString()) || TextUtils.isEmpty(TextDescricaoArtigo.getText().toString())) { //Caso algum campo não tenha texto
                if (link.isEmpty()) { //Caso nao escolha imagem
                    Toast.makeText(getActivity(), getResources().getString(R.string.mandatory_product_to_create), Toast.LENGTH_SHORT).show(); //Mensagem//Mensagem
                }

                if (TextUtils.isEmpty(TextNomeArtigo.getText().toString())) { //Caso nao insira nome
                    TextNomeArtigo.setError(getResources().getString(R.string.enter_product_name));     //Apresentar um erro
                    TextNomeArtigo.requestFocus();    //Apresentar um alerta
                    return;
                }

                if (TextUtils.isEmpty(TextTipoArtigo.getText().toString())) {   //Caso nao insira tipo
                    TextTipoArtigo.setError(getResources().getString(R.string.enter_product_type));     //Apresentar um erro
                    TextTipoArtigo.requestFocus();    //Apresentar um alerta
                    return;
                }

                if (TextUtils.isEmpty(TextQuantidadeArtigo.getText().toString())) {     //Caso nao insira quantidade
                    TextQuantidadeArtigo.setError(getResources().getString(R.string.enter_product_quantity));     //Apresentar um erro
                    TextQuantidadeArtigo.requestFocus();    //Apresentar um alerta
                    return;
                }

                if (TextUtils.isEmpty(TextCorArtigo.getText().toString())) {    //Caso nao insira cor
                    TextCorArtigo.setError(getResources().getString(R.string.enter_product_color));     //Apresentar um erro
                    TextCorArtigo.requestFocus();    //Apresentar um alerta
                    return;
                }


                if (TextUtils.isEmpty(TextDescricaoArtigo.getText().toString())) {     //Caso nao insira descrição
                    TextDescricaoArtigo.setError(getResources().getString(R.string.enter_product_description));     //Apresentar um erro
                    TextDescricaoArtigo.requestFocus();    //Apresentar um alerta
                    return;
                }
            } else {

                if (isNetworkConnected()) {
                    final ProgressDialog mDialog = new ProgressDialog(getActivity());   //Iniciar a barra de progresso
                    mDialog.setMessage(getResources().getString(R.string.saving_image));       //Inserir uma mensagem
                    mDialog.setCanceledOnTouchOutside(false);                           //Retirar a função de fechar a barra quando se carrega no resto do ecrã
                    mDialog.show();                                                     //Mostrar a barra

                    final String key = FirebaseDatabase.getInstance().getReference().child(firebaseAuth.getCurrentUser().getUid()).child("Caixas").push().getKey();   //Associação da variavel ao ID do novo produto

                    Produtos produtos = new Produtos(TextNomeArtigo.getText().toString(), TextTipoArtigo.getText().toString(), TextCorArtigo.getText().toString(), TextQuantidadeArtigo.getText().toString(), link, key, FragmentClassData.value, TextDescricaoArtigo.getText().toString());
                    FirebaseDatabase.getInstance().getReference(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child("Caixas").child(FragmentClassData.value).child("Artigos").child(key) //Metodo para inserir os dados na base de dados
                            .setValue(produtos).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {  //Caso não haja erros
                                mDialog.dismiss();  //Parar a barra
                                getFragmentManager().popBackStack();
                                Toast.makeText(getActivity(), getResources().getString(R.string.product_create_sucessful), Toast.LENGTH_SHORT).show();   //Mensagem
                            } else {    //Caso haja erros
                                Toast.makeText(getActivity(), getResources().getString(R.string.error_please_try_again), Toast.LENGTH_SHORT).show();   //Mensagem
                            }
                        }
                    });
                } else
                    InternetMessage();

            }
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