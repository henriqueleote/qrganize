
/*  Coded by Henrique Leote - 2019/2020   */

//Esta classe é responsável pela página do perfil do utilizador

package com.leote.app.Auth;

//Livrarias de Funções necessárias

import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.github.chrisbanes.photoview.PhotoView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.leote.app.AboutFragment;
import com.leote.app.R;
import com.squareup.picasso.Picasso;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;


public class ProfileFragment extends Fragment {

    //Declaração de variaveis
    public static String ImageLink;                                     //Varivel que guarda o link da imagem de perfil
    TextView TextEmail, TextName, BoxCount, year, optionBtn;            //Variavel de texto
    ImageView ImageAva, Image;                                          //Variavel de Imagem
    private FirebaseAuth firebaseAuth;                                  //Variavel de autenticação da base de dados
    private SwipeRefreshLayout mSwipeRefreshLayout;                     //Variavel de layout

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //Associação desta classe ao layout "fragment_profile"
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        if (isNetworkConnected()) {
            //Inicializar o acesso à autenticação da base de dados
            firebaseAuth = FirebaseAuth.getInstance();


            //Este bloco verifica se há sessão iniciada, senão, abre a ativadade para autenticar
            if (firebaseAuth.getCurrentUser() == null) {
                getActivity().finish();
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                startActivity(new Intent(getActivity(), StartActivity.class));
            }


            TextEmail = view.findViewById(R.id.profile_email);                      //Variaveis de texto
            TextName = view.findViewById(R.id.profile_name);                        //Variaveis de texto
            ImageAva = view.findViewById(R.id.profile_image);                       //Imagem
            Image = view.findViewById(R.id.image);                                  //Imagem
            mSwipeRefreshLayout = view.findViewById(R.id.swiperefresh_items);       //Layout
            BoxCount = view.findViewById(R.id.boxCount);                            //Variaveis de texto
            year = view.findViewById(R.id.year);                                    //Variaveis de texto
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

            Data();     //Declaração do motodo dos Dados do Perfil
            Count();    //Declaração do motodo de contar o numero de caixas


            //Este bloco faz com que haja um "Refresh" na pagina quando ocorre um gesto descendente no ecrã
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if (isNetworkConnected()) {
                        Data();     //Declaração do motodo dos Dados do Perfil
                        Count();    //Declaração do motodo de contar o numero de caixas
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

            //Quando é clicado na imagem de perfil do utilizador
            ImageAva.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isNetworkConnected()) {
                        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
                        View mView = getLayoutInflater().inflate(R.layout.dialog_custom_layout, null);   //inicializar view do qr
                        PhotoView photoView = mView.findViewById(R.id.imageView);                              //associar a classe ao design
                        Picasso.get()                                                 //Utilizar o metodo Picasso para inserir a imagem
                                .load(ImageLink)                                           //Carregar a imagem
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

            Image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    About();
                }
            });
        } else {
            InternetMessageClose();
        }

        //Devolver dados para o ecrã
        return view;

    }

    //Este metodo faz com que o menu de opções apresenta os dados
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater menuInflater = getActivity().getMenuInflater();
        menuInflater.inflate(R.menu.options_menu, menu);
    }

    //Este metodo faz com que o haja opções no menu e que abram certas
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // Handle item click
        switch (item.getItemId()) {
            case R.id.edit_profile:     //caso se carregue na primeira opção
                EditProfile();
                break;
            case R.id.change_email:     //caso se carregue na segunda opção
                ChangeEmail();
                break;
            case R.id.change_password:  //caso se carregue na terceira opção
                ChangePassword();
                break;
            case R.id.delete_account:   //caso se carregue na quarta opção
                AccountDelete();
                break;
            case R.id.logout:           //caso se carregue na quinta opção
                Logout();
                break;
            case R.id.about:            //caso se carregue na sexta opção
                About();
                break;
            default:
                break;

        }
        return super.onContextItemSelected(item);
    }

    //Este metodo termina a sessão
    public void About() {
        if (isNetworkConnected()) {
            AboutFragment bottomSheetFragment = new AboutFragment();
            bottomSheetFragment.show(getActivity().getSupportFragmentManager(), bottomSheetFragment.getTag());
        } else {
            InternetMessage();
        }
    }

    //Este metodo termina a sessão
    public void Logout() {
        if (isNetworkConnected()) {
            LogoutFragment bottomSheetFragment = new LogoutFragment();
            bottomSheetFragment.show(getActivity().getSupportFragmentManager(), bottomSheetFragment.getTag());
        } else {
            InternetMessage();
        }
    }

    //Este metodo faz com abra a página para eliminar a conta
    public void AccountDelete() {
        if (isNetworkConnected()) {
            DeleteProfileFragment bottomSheetFragment = new DeleteProfileFragment();
            bottomSheetFragment.show(getActivity().getSupportFragmentManager(), bottomSheetFragment.getTag());
        } else {
            InternetMessage();
        }
    }

    //Este metodo faz com abra a página para editar o perfil
    public void EditProfile() {
        if (isNetworkConnected()) {
            EditProfileFragment bottomSheetFragment = new EditProfileFragment();
            bottomSheetFragment.show(getActivity().getSupportFragmentManager(), bottomSheetFragment.getTag());
        } else {
            InternetMessage();
        }
    }

    //Este metodo faz com abra a página para editar o perfil
    public void ChangeEmail() {
        if (isNetworkConnected()) {
            ChangeEmailFragment bottomSheetFragment = new ChangeEmailFragment();
            bottomSheetFragment.show(getActivity().getSupportFragmentManager(), bottomSheetFragment.getTag());
        } else {
            InternetMessage();
        }
    }

    //Este metodo faz com abra a página para editar o perfil
    public void ChangePassword() {
        if (isNetworkConnected()) {
            ChangePasswordFragment bottomSheetFragment = new ChangePasswordFragment();
            bottomSheetFragment.show(getActivity().getSupportFragmentManager(), bottomSheetFragment.getTag());
        } else {
            InternetMessage();
        }
    }

    //Este metodo faz com que o perfil seja preenchido com os dados da base de dados
    public void Data() {
        if (isNetworkConnected()) {
            TextEmail.setText(firebaseAuth.getCurrentUser().getEmail());            //Apresenta o email na pagina do perfil
            final ProgressDialog mDialog = new ProgressDialog(getActivity());       //Iniciar a barra de progresso
            mDialog.setMessage(getResources().getString(R.string.please_wait));                                   //Inserir uma mensagem
            mDialog.setCanceledOnTouchOutside(false);                               //Retirar a função de fechar a barra quando se carrega no resto do ecrã
            mDialog.show();                                                         //Mostrar a barra

            DatabaseReference uidRef = FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Profile"); //Guardar na variavel o caminho para os dados da utilizador

            ValueEventListener valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    ImageLink = dataSnapshot.child("link").getValue(String.class);            //Guardar o link da imagem do utilizador
                    String name = dataSnapshot.child("name").getValue(String.class);            //Guardar o nome do utilizador
                    String ano = dataSnapshot.child("ano").getValue(String.class);              //Guardar o ano de registo do utilizador
                    year.setText(ano);                                                          //Escrever o ano
                    TextName.setText(name);                                                     //Escrever o nome

                    Picasso.get()                                           //Utilizar o metodo Picasso para inserir a imagem
                            .load(ImageLink)                                //Carregar a imagem
                            .centerCrop()                                   //Recortar a imagem ao centro
                            .fit()                                          //Colocar a imagem de maneira a caber na ImageView
                            .transform(new CropCircleTransformation())      //Editar para ficar redonda
                            .into(ImageAva);                                //Inserir na ImageView

                    mDialog.dismiss();  //Parar a barra de progresso

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    mDialog.dismiss();  //Parar a barra de progresso
                    Toast.makeText(getActivity(), getResources().getString(R.string.error_please_try_again), Toast.LENGTH_SHORT).show(); //Mensagem
                }
            };
            uidRef.addListenerForSingleValueEvent(valueEventListener); //Guardar as alterações
        } else {
            InternetMessage();
        }

    }

    //Este metodo conta o número de caixas na base de dados
    public void Count() {
        if (isNetworkConnected()) {
            DatabaseReference boxRef = FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Caixas");    //Guardar na variavel o caminho para os dados das caixas
            ValueEventListener valueEventListener = (new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {    //Caso existam caixas
                        String numberAsString = String.valueOf(dataSnapshot.getChildrenCount());    //Associar string ao numero de caixas
                        BoxCount.setText(numberAsString);                                           //Definir na caixa de texto
                    } else {  //Caso não haja caixas
                        BoxCount.setText("0");                                                      //Definir na caixa de texto
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }


            });
            boxRef.addListenerForSingleValueEvent(valueEventListener); //Guardar as alterações
        } else {
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
