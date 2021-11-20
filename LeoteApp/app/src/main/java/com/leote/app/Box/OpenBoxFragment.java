
/*  Coded by Henrique Leote - 2019/2020   */

//Esta classe é responsável pela página para abrir a caixa escolhida

package com.leote.app.Box;

//Livrarias de Funções necessárias

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.leote.app.Adapters.FragmentClassData;
import com.leote.app.Adapters.MyProductAdapter;
import com.leote.app.Adapters.Produtos;
import com.leote.app.Auth.StartActivity;
import com.leote.app.Product.NewProductFragment;
import com.leote.app.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import pub.devrel.easypermissions.EasyPermissions;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE;


public class OpenBoxFragment extends Fragment {

    public static String link;                                                //Variavel que guarda o link da imagem
    public static String[] perms = {  //Criar lista de strings com as permissoes
            Manifest.permission.READ_EXTERNAL_STORAGE,
            WRITE_EXTERNAL_STORAGE
    };
    RecyclerView recyclerView;                                                //Variavel que guarda o layout
    ArrayList<Produtos> list;                                                 //Variavel que guarda os dados do array
    MyProductAdapter adapter;                                                 //Variavel que guarda o acesso ao adaptador dos Produtos
    FloatingActionButton fab;                                                 //Variavel do botão de adicionar caixa
    SwipeRefreshLayout mSwipeRefreshLayout;                                   //Variavel que guarda o layout de swipe
    FirebaseAuth firebaseAuth;                                                //Variavel de autenticação da base de dados
    TextView Nome, Local, Tipo, invisible_create, Back, Number, optionBtn;     //Variaveis de texto
    ImageView Image;                                                          //Variavel da imagem do codigo

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //Associação desta classe ao layout "fragment_open_box"
        View view = inflater.inflate(R.layout.fragment_open_box, container, false);


        if (isNetworkConnected()) {
            //Inicializar o acesso à autenticação da base de dados
            firebaseAuth = FirebaseAuth.getInstance();

            //Este bloco verifica se há sessão iniciada, senão, abre a ativadade para autenticar
            if (firebaseAuth.getCurrentUser() == null) {
                getActivity().finish(); //terminar a ativadade atual
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);  //Adicionar uma animação quando entre as atividades
                startActivity(new Intent(getActivity(), StartActivity.class));  //Abrir uma nova atividade
            }


            TopData();  //Metodo dos dados do topo
            Data();     //Metodo dos dados dos produtos


            mSwipeRefreshLayout = view.findViewById(R.id.swiperefresh_items);      //Associação da variavel ao layout de design


            //Este bloco faz com que haja um "Refresh" na pagina quando ocorre um gesto descendente no ecrã
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if (isNetworkConnected()) {
                        list.clear();   //Limpar os dados atuais
                        adapter = new MyProductAdapter(getActivity(), list);
                        recyclerView.setAdapter(adapter);   //Definir adaptador
                        TopData();
                        Data(); //Metodo dos dados
                    } else
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


            recyclerView = view.findViewById(R.id.myRecyclerProduct);  //Associação do layout do design
            recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2)); //Limitação para 2 itens
            invisible_create = view.findViewById(R.id.invisible_create);    //Associação da caixa de texto
            list = new ArrayList<Produtos>();               //Associar lista do array aos dados das caixas
            Nome = view.findViewById(R.id.boxName);         //Associação da variavel à caixa de texto do nome
            Local = view.findViewById(R.id.boxLocal);       //Associação da variavel à caixa de texto do local
            Tipo = view.findViewById(R.id.boxTipo);         //Associação da variavel à caixa de texto do tipo
            Number = view.findViewById(R.id.productNumber); //Associação da variavel à caixa de texto do numero de produtos
            Image = view.findViewById(R.id.qrImage);        //Associação da imagem do codigo qr
            fab = view.findViewById(R.id.fab);              //Associação do botão ao botão de layout
            Back = view.findViewById(R.id.back);            //Associação ao botão de voltar atrás
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

            //Quando se carrega, abre a página anterior
            Back.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isNetworkConnected()) {
                        HomeFragment homeFragment= new HomeFragment();
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, homeFragment, "Fragment")
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
                        mDialog.show();                                               //mostrar view
                    } else
                        InternetMessage();

                }
            });


            //Quando clicado no botão, abre uma nova página para criar um produto
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isNetworkConnected()) {
                        NewProductFragment fragment = new NewProductFragment();
                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        transaction.replace(R.id.fragment_container, fragment); // give your fragment container id in first parameter
                        transaction.addToBackStack(null);  // if written, this transaction will be added to backstack
                        transaction.commit();
                    } else
                        InternetMessage();

                    if (Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.LOLLIPOP) {  //Caso seja compativel
                        if (!EasyPermissions.hasPermissions(getActivity(), perms)) { //caso nao haja permissão, pede
                            EasyPermissions.requestPermissions(getActivity(), getResources().getString(R.string.needed_permissions), REQUEST_CODE, perms);
                        }
                    }

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
        menuInflater.inflate(R.menu.box_menu, menu);
    }

    //Este metodo faz com que o haja opções no menu e que abram certas
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // Handle item click
        switch (item.getItemId()) {
            case R.id.edit_box:     //caso se carregue na primeira opção
                EditBox();
                break;
            case R.id.save_gallery:     //caso se carregue na primeira opção
                Save();
                break;
            case R.id.delete_box:   //caso se carregue na quarta opção
                DeleteBox();
                break;
            default:
                break;

        }
        return super.onContextItemSelected(item);
    }

    public void Save() {

        int currentapiVersion = android.os.Build.VERSION.SDK_INT;   //verificar a versão do sistema
        if (currentapiVersion >= android.os.Build.VERSION_CODES.M) {    //caso seja compativel
            if (EasyPermissions.hasPermissions(getActivity(), perms)) {    //verifica permissoes
                Bitmap bitmap = ((BitmapDrawable) Image.getDrawable()).getBitmap();
                File myDir = new File(Environment.getExternalStorageDirectory().toString() + "/Pictures/QRganize");
                if (!myDir.exists())
                    myDir.mkdirs();
                String fileName = Nome.getText().toString() + System.currentTimeMillis() + ".png";
                File file = new File(myDir, fileName);
                if (file.exists())
                    file.delete();
                try {
                    FileOutputStream out = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                    Toast.makeText(getActivity(), getResources().getString(R.string.successful_save) + file, Toast.LENGTH_SHORT).show();
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.grant_try_again), Toast.LENGTH_SHORT).show();
                }
            } else {
                EasyPermissions.requestPermissions(getActivity(), getResources().getString(R.string.needed_permissions), REQUEST_CODE, perms);    //pedir permissões
            }
        }

    }

    public void EditBox() {
        if (isNetworkConnected()) {
            EditBoxFragment bottomSheetFragment = new EditBoxFragment();
            bottomSheetFragment.show(getActivity().getSupportFragmentManager(), bottomSheetFragment.getTag());
        } else
            InternetMessage();
    }

    public void DeleteBox() {
        if (isNetworkConnected()) {
            DeleteBoxFragment bottomSheetFragment = new DeleteBoxFragment();
            bottomSheetFragment.show(getActivity().getSupportFragmentManager(), bottomSheetFragment.getTag());
        } else
            InternetMessage();
    }

    public void Data() {
        if (isNetworkConnected()) {
            FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Caixas").child(FragmentClassData.value).child("Artigos").addValueEventListener(new ValueEventListener() { //Caminho para a pasta dos produtos
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getChildrenCount() == 0)
                        Number.setText("0 " + getResources().getString(R.string.products));    //Numero de Produtos
                    else
                        Number.setText(dataSnapshot.getChildrenCount()+ " " + getResources().getString(R.string.products));    //Numero de Produtos

                    if (dataSnapshot.exists()) {  //Caso haja produtos
                        for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                            Produtos p = dataSnapshot1.getValue(Produtos.class);   //Guardar os dados na class "Box"
                            list.add(p);        //Adicionar caixas à lista do array
                            adapter = new MyProductAdapter(getActivity(), list);    //Definir o adaptador com a lista das caixas
                            recyclerView.setAdapter(adapter);   //Definir o layout com o adaptador
                            adapter.notifyDataSetChanged(); //Alterar caso aconteça algo
                        }
                    } else {
                        invisible_create.setVisibility(View.VISIBLE);   //Apresentar texto a dizer para criar caixas
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.error_please_try_again), Toast.LENGTH_SHORT).show();
                }
            });
        } else
            InternetMessage();
        //Apresentar produtos que existem na base de dados

    }

    public void TopData() {
        if (isNetworkConnected()) {
            DatabaseReference uidRef = FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Caixas").child(FragmentClassData.value).child("Dados"); //Caminho para a pasta dos dados
            ValueEventListener valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    link = dataSnapshot.child("link").getValue(String.class);            //Guardar o link da imagem do codigo qr
                    Nome.setText("" + dataSnapshot.child("name").getValue(String.class));       //Escrever o nome da caixa
                    Local.setText("" + dataSnapshot.child("local").getValue(String.class));     //Escrever o local da caixa
                    Tipo.setText("" + dataSnapshot.child("tipo").getValue(String.class));       //Escrever o tipo da caixa

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
