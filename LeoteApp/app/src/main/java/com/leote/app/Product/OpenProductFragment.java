
/*  Coded by Henrique Leote - 2019/2020   */

//Esta classe é responsável pela página de abertura do produto escolhido

package com.leote.app.Product;

//Livrarias de Funções necessárias

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.github.chrisbanes.photoview.PhotoView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.leote.app.Adapters.FragmentClassData;
import com.leote.app.Auth.StartActivity;
import com.leote.app.Box.DeleteBoxFragment;
import com.leote.app.Box.HomeFragment;
import com.leote.app.Box.OpenBoxFragment;
import com.leote.app.R;
import com.squareup.picasso.Picasso;


public class OpenProductFragment extends Fragment {

    public static String link;                                          //Variavel que guarda o link da imagem
    TextView Nome, Tipo, Quantidade, Cor, Back, Descricao, optionBtn;    //Variaveis de texto
    ImageView Image;                                                    //Variavel de imagem
    FirebaseAuth firebaseAuth;                                          //Variavel de acesso à autenticação da base de dados
    SwipeRefreshLayout mSwipeRefreshLayout;                             //Variavel que guarda o layout de swipe


    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //Associação desta classe ao layout "fragment_open_product"
        View view = inflater.inflate(R.layout.fragment_open_product, container, false);

        if (isNetworkConnected()) {
            //Inicializar o acesso à autenticação da base de dados
            firebaseAuth = FirebaseAuth.getInstance();

            //Este bloco verifica se há sessão iniciada, senão, abre a ativadade para autenticar
            if (firebaseAuth.getCurrentUser() == null) {
                getActivity().finish(); //terminar a ativadade atual
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);  //Adicionar uma animação quando entre as atividades
                startActivity(new Intent(getActivity(), StartActivity.class));  //Abrir uma nova atividade
            }

            Data();     //Metodo dos dados


            mSwipeRefreshLayout = view.findViewById(R.id.swiperefresh_items);      //Associação da variavel ao layout de design
            Nome = view.findViewById(R.id.productName);                 //Associação da variavel à caixa de texto do nome
            Tipo = view.findViewById(R.id.productTipo);                 //Associação da variavel à caixa de texto do tipo
            Descricao = view.findViewById(R.id.productDescricao);       //Associação da variavel à caixa de texto da descricao
            Quantidade = view.findViewById(R.id.productQuantidade);     //Associação da variavel à caixa de texto da quantidade
            Cor = view.findViewById(R.id.productCor);                   //Associação da variavel à caixa de texto da cor
            Image = view.findViewById(R.id.productImage);               //Associação da variavel à imagem do produto
            Back = view.findViewById(R.id.back);                        //Associação ao botão de voltar atrás
            optionBtn = view.findViewById(R.id.options);                            //Associar a variavel do menu ao menu do layout
            registerForContextMenu(optionBtn);                                      //Associar o menu de contexto ao menu de opções

            //Quando clicado, abre um menú de opções
            optionBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isNetworkConnected())
                        getActivity().openContextMenu(v);
                    else
                        InternetMessage();
                }
            });

            //Este bloco faz com que haja um "Refresh" na pagina quando ocorre um gesto descendente no ecrã
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if (isNetworkConnected())
                        Data(); //Metodo dos dados
                    else
                        InternetMessage();
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (mSwipeRefreshLayout.isRefreshing()) {
                                mSwipeRefreshLayout.setRefreshing(false);
                            }
                        }
                    }, 1000);
                }
            });


            //Quando se carrega, abre a página anterior
            Back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isNetworkConnected()) {
                        OpenBoxFragment boxFragment= new OpenBoxFragment();
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, boxFragment, "Fragment")
                                .addToBackStack(null)
                                .commit();
                    } else
                        InternetMessage();
                }
            });

            //Quando se carrega na imagem do código QR, abre a imagem em full screen
            Image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isNetworkConnected()) {
                        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
                        View mView = getLayoutInflater().inflate(R.layout.dialog_custom_layout, null);   //inicializar view do qr
                        PhotoView photoView = mView.findViewById(R.id.imageView);                              //associar a classe ao design
                        Picasso.get()                                                 //Utilizar o metodo Picasso para inserir a imagem
                                .load(link)                                           //Carregar a imagem
                                .centerCrop()                                         //Recortar a imagem ao centro
                                .fit()                                                //Colocar a imagem de maneira a caber na ImageView
                                .into(photoView);                                     //Inserir na ImageView

                        mBuilder.setView(mView);                                      //definir apresentarção da view na pagina
                        AlertDialog mDialog = mBuilder.create();                      //criar view
                        mDialog.show();
                    } else
                        InternetMessage();
                    //mostrar view
                }
            });
        } else
            InternetMessageClose();

        return view;
    }

    //Este metodo faz com que o menu de opções apresenta os dados
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater menuInflater = getActivity().getMenuInflater();
        menuInflater.inflate(R.menu.product_menu, menu);
    }

    //Este metodo faz com que o haja opções no menu e que abram certas
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // Handle item click
        switch (item.getItemId()) {
            case R.id.edit_product:     //caso se carregue na primeira opção
                EditProduct();
                break;
            case R.id.delete_product:   //caso se carregue na segunda opção
                DeleteProduct();
                break;
            default:
                break;

        }
        return super.onContextItemSelected(item);
    }

    public void EditProduct() {
        if (isNetworkConnected()) {
            EditProductFragment bottomSheetFragment = new EditProductFragment();
            bottomSheetFragment.show(getActivity().getSupportFragmentManager(), bottomSheetFragment.getTag());
        } else
            InternetMessage();
    }

    public void DeleteProduct() {
        if (isNetworkConnected()) {
            DeleteProductFragment bottomSheetFragment = new DeleteProductFragment();
            bottomSheetFragment.show(getActivity().getSupportFragmentManager(), bottomSheetFragment.getTag());
        } else
            InternetMessage();
    }


    public void Data() {
        if (isNetworkConnected()) {
            DatabaseReference uidRef = FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Caixas").child(FragmentClassData.value).child("Artigos").child(FragmentClassData.value2); //Caminho para a pasta dos dados
            ValueEventListener valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    link = dataSnapshot.child("link").getValue(String.class);                                                                             //Guardar o link da imagem do produto
                    Nome.setText(dataSnapshot.child("nome").getValue(String.class));                                                                      //Escrever o nome do produto
                    Tipo.setText(dataSnapshot.child("tipo").getValue(String.class));                                                                      //Escrever o tipo do produto
                    Quantidade.setText(dataSnapshot.child("quantidade").getValue(String.class) + " " + getResources().getString(R.string.products));      //Escrever a quantidade do produto
                    Cor.setText(dataSnapshot.child("cor").getValue(String.class));                                                                        //Escrever a cor do produto
                    Descricao.setText(dataSnapshot.child("descricao").getValue(String.class));                                                            //Escrever a descrição do produto


                    Picasso.get()                                           //Utilizar o metodo Picasso para inserir a imagem
                            .load(link)                                     //Carregar a imagem
                            .centerCrop()                                   //Recortar a imagem ao centro
                            .fit()                                          //Colocar a imagem de maneira a caber na ImageView
                            .into(Image);                                   //Inserir na ImageView
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.error_please_try_again), Toast.LENGTH_SHORT).show(); //Mensagem
                }
            };
            uidRef.addListenerForSingleValueEvent(valueEventListener);
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
