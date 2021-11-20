
/*  Coded by Henrique Leote - 2019/2020   */

//Esta classe é responsável pela página de modificação de Produtos

package com.leote.app.Product;

//Livrarias de Funções necessárias

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
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
import com.leote.app.Adapters.FragmentClassData;
import com.leote.app.Adapters.Produtos;
import com.leote.app.Auth.StartActivity;
import com.leote.app.R;
import com.squareup.picasso.Picasso;

import static android.app.Activity.RESULT_OK;

public class EditProductFragment extends BottomSheetDialogFragment {

    //Declaração de variaveis
    public static String link;                                  //Variavel que guarda o link
    public static Drawable oldDrawable;                         //Variavel que guarda a imagem
    EditText Nome, Tipo, Quantidade, Cor, Descricao;            //Caixas de texto
    Button Save;                                                //Botão de Guardar
    FirebaseAuth firebaseAuth;                                  //Variavel de acesso à autenticação da base de dados
    private ImageView img;                                      //Imagem de layout
    private Uri imguri;                                         //Variavel com o endereço da imagem

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(final Dialog dialog, int style) {
        super.setupDialog(dialog, style);

        //Associação desta classe ao layout "edit_product_sheet"
        View view = View.inflate(getContext(), R.layout.edit_product_sheet, null);

        if (isNetworkConnected()) {
            //Inicializar o acesso à autenticação da base de dados
            firebaseAuth = FirebaseAuth.getInstance();

            //Este bloco verifica se há sessão iniciada, senão, abre a ativadade para autenticar
            if (firebaseAuth.getCurrentUser() == null) {
                getActivity().finish(); //terminar a ativadade atual
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);  //Adicionar uma animação quando entre as atividades
                startActivity(new Intent(getActivity(), StartActivity.class));  //Abrir uma nova atividade
            }

            Nome = view.findViewById(R.id.editProductName);                 //Associação da variavel à caixa de texto do nome
            Tipo = view.findViewById(R.id.editProductTipo);                 //Associação da variavel à caixa de texto do tipo
            Quantidade = view.findViewById(R.id.editProductQuantidade);     //Associação da variavel à caixa de texto da quantidade
            Cor = view.findViewById(R.id.editProductCor);                   //Associação da variavel à caixa de texto da cor
            Descricao = view.findViewById(R.id.editProductDescricao);       //Associação da variavel à caixa de texto da descricao
            Save = view.findViewById(R.id.buttonSave);                      //Associação da variavel ao botão de guardar

            img = view.findViewById(R.id.mImageView);   //Associar variavel de imagem à imagem do layout

            oldDrawable = img.getDrawable();    //Guardar a imagem na variavel

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

            Data(); //Metodo dos Dados

            Save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isNetworkConnected())
                        saveProductInformation();
                    else
                        InternetMessage();
                }
            });

        } else
            InternetMessageClose();

        //Abrir a bottomsheet sem problemas de design
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                BottomSheetDialog d = (BottomSheetDialog) dialog;

                FrameLayout bottomSheet = d.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        //Mostrar dados
        dialog.setContentView(view);
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
                        .into(img);

                final ProgressDialog mDialog = new ProgressDialog(getActivity());   //Iniciar a barra de progresso
                mDialog.setMessage(getResources().getString(R.string.saving_image));       //Inserir uma mensagem
                mDialog.setCanceledOnTouchOutside(false);                           //Retirar a função de fechar a barra quando se carrega no resto do ecrã
                mDialog.show();                                                     //Mostrar a barra


                StorageReference storageReference = FirebaseStorage.getInstance().getReference();   //Inicializar a variavel de armazenamento da base de dados
                final StorageReference imageRef = storageReference.child("Products/" + imguri.getLastPathSegment());  //Inicializar a referencia do armazenamento onde a imagem será guardada
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
                                Toast.makeText(getActivity(), getResources().getString(R.string.error_please_try_again), Toast.LENGTH_SHORT).show(); //Mensagem
                            }
                        });
            }
        } else
            InternetMessage();

    }

    //Metodo para apresentar os dados nos campos
    public void Data() {
        if (isNetworkConnected()) {
            DatabaseReference uidRef = FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Caixas").child(FragmentClassData.value
            ).child("Artigos").child(FragmentClassData.value2);   //Guardar na variavel o caminho para os dados do produto
            ValueEventListener valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    link = dataSnapshot.child("link").getValue(String.class);                               //Guardar o link da imagem do utilizador
                    Nome.setText(dataSnapshot.child("nome").getValue(String.class));                   //Guardar o nome do produto
                    Tipo.setText(dataSnapshot.child("tipo").getValue(String.class));                   //Guardar o tipo do produto
                    Quantidade.setText(dataSnapshot.child("quantidade").getValue(String.class));       //Guardar a quantidade do produto
                    Cor.setText(dataSnapshot.child("cor").getValue(String.class));                     //Guardar a cor do produto
                    Descricao.setText(dataSnapshot.child("descricao").getValue(String.class));         //Guardar a descricao do produto

                    Picasso.get()                                           //Utilizar o metodo Picasso para inserir a imagem
                            .load(link)                                     //Carregar a imagem
                            .centerCrop()                                   //Recortar a imagem ao centro
                            .fit()                                          //Colocar a imagem de maneira a caber na ImageView
                            .into(img);                                     //Inserir na ImageView

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    getDialog().cancel();
                    Toast.makeText(getActivity(), getResources().getString(R.string.error_please_try_again), Toast.LENGTH_SHORT).show();
                }
            };
            uidRef.addListenerForSingleValueEvent(valueEventListener);
        } else
            InternetMessage();
    }

    //Metodo para guardar os dados na base de dados
    private void saveProductInformation() {
        if (TextUtils.isEmpty(Nome.getText().toString()) || TextUtils.isEmpty(Tipo.getText().toString()) || TextUtils.isEmpty(Quantidade.getText().toString()) || TextUtils.isEmpty(Cor.getText().toString()) || link.isEmpty() || TextUtils.isEmpty(Descricao.getText().toString())) {    //Caso algum campo não tenha texto
            if (link.isEmpty()) {   //Caso não haja um link
                Toast.makeText(getActivity(), getResources().getString(R.string.mandatory_product_to_create), Toast.LENGTH_SHORT).show(); //Mensagem
            }

            if (TextUtils.isEmpty(Nome.getText().toString())) { //Caso o campo do nome não tenha texto
                Nome.setError(getResources().getString(R.string.enter_product_name));  //Apresentar um erro
                Nome.requestFocus();    //Apresentar um alerta
                return;
            }

            if (TextUtils.isEmpty(Tipo.getText().toString())) { //Caso o campo do tipo não tenha texto
                Tipo.setError(getResources().getString(R.string.enter_product_type));  //Apresentar um erro
                Tipo.requestFocus();    //Apresentar um alerta
                return;
            }

            if (TextUtils.isEmpty(Quantidade.getText().toString())) {   //Caso o campo da quantidade não tenha texto
                Quantidade.setError(getResources().getString(R.string.enter_product_quantity));  //Apresentar um erro
                Quantidade.requestFocus();    //Apresentar um alerta
                return;
            }

            if (TextUtils.isEmpty(Cor.getText().toString())) {  //Caso o campo da cor não tenha texto
                Cor.setError(getResources().getString(R.string.enter_product_color));  //Apresentar um erro
                Cor.requestFocus();    //Apresentar um alerta
                return;
            }

            if (TextUtils.isEmpty(Descricao.getText().toString())) {  //Caso o campo da descricao não tenha texto
                Descricao.setError(getResources().getString(R.string.enter_product_description));  //Apresentar um erro
                Descricao.requestFocus();    //Apresentar um alerta
                return;
            }

        } else {
            if (isNetworkConnected()) {
                Produtos productName = new Produtos(Nome.getText().toString(), Tipo.getText().toString(), Cor.getText().toString(), Quantidade.getText().toString(), link, FragmentClassData.value2, FragmentClassData.value, Descricao.getText().toString());
                FirebaseDatabase.getInstance().getReference(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child("Caixas").child(FragmentClassData.value).child("Artigos").child(FragmentClassData.value2) //Metodo para inserir os dados na base de dados
                        .setValue(productName).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {  //Caso não haja erros
                            getDialog().cancel();
                            Toast.makeText(getActivity(), getResources().getString(R.string.data_success_changed_refresh), Toast.LENGTH_SHORT).show();   //Mensagem
                        } else {    //Caso haja erros
                            Toast.makeText(getActivity(), getResources().getString(R.string.error_please_try_again), Toast.LENGTH_SHORT).show();   //Mensagem
                        }
                    }
                });
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
