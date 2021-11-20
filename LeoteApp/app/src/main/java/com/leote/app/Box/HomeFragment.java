
/*  Coded by Henrique Leote - 2019/2020   */

//Esta classe é responsável pela página para mostrar as caixas

package com.leote.app.Box;

//Livrarias de Funções necessárias

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.leote.app.Adapters.Box;
import com.leote.app.Adapters.MyBoxAdapter;
import com.leote.app.Auth.StartActivity;
import com.leote.app.R;

import java.util.ArrayList;


public class HomeFragment extends Fragment {

    //Declaração de variaveis
    RecyclerView recyclerView;                                  //Variavel que guarda o layout
    ArrayList<Box> list;                                        //Variavel que guarda os dados do array
    MyBoxAdapter adapter;                                       //Variavel que guarda o acesso ao adaptador das Caixas
    FloatingActionButton fab_main;                              //Variavel do botão de adicionar caixa
    SwipeRefreshLayout mSwipeRefreshLayout;                     //Variavel que guarda o layout de swipe
    TextView invisible_create, TopText;                         //Variaveis de texto
    FirebaseAuth firebaseAuth;                                  //Variavel de autenticação da base de dados


    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //Associação desta classe ao layout "fragment_home"
        View view = inflater.inflate(R.layout.fragment_home, container, false);

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

            mSwipeRefreshLayout = view.findViewById(R.id.swiperefresh_items);   //Associação da variavel ao layout de design


            //Este bloco faz com que haja um "Refresh" na pagina quando ocorre um gesto descendente no ecrã
            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if (isNetworkConnected()) {
                        list.clear();   //Limpar os dados atuais
                        adapter = new MyBoxAdapter(getActivity(), list);
                        recyclerView.setAdapter(adapter);   //Definir adaptador
                        Data(); //Metodo dos dados
                    } else {
                        InternetMessage();
                    }

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

            recyclerView = view.findViewById(R.id.myRecycler);  //Associação do layout do design
            recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2)); //Limitação para 2 itens
            invisible_create = view.findViewById(R.id.invisible_create);    //Associação da caixa de texto
            list = new ArrayList<Box>();    //Associar lista do array aos dados das caixas
            fab_main = view.findViewById(R.id.fab);         //Associação do botão ao botão de layout
            TopText = view.findViewById(R.id.text);

            TopText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    list.clear();   //Limpar os dados atuais
                    adapter = new MyBoxAdapter(getActivity(), list);
                    recyclerView.setAdapter(adapter);   //Definir adaptador
                    Data(); //Metodo dos dados
                }
            });

            //Quando clicado no botão, abre uma nova página para criar um caixa
            fab_main.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isNetworkConnected()) {
                        NewBoxFragment fragment = new NewBoxFragment();
                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                        transaction.replace(R.id.fragment_container, fragment);
                        transaction.addToBackStack(null);
                        transaction.commit();
                    } else {
                        InternetMessage();
                    }
                }
            });

        } else {
            InternetMessageClose();
        }

        return view;
    }

    //Metodo que contem os dados das caixas
    public void Data() {
        if (isNetworkConnected()) {
            //Apresentar caixas que existem na base de dados
            FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Caixas").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {  //Caso haja caixas
                        for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                            Box b = dataSnapshot1.child("Dados").getValue(Box.class);   //Guardar os dados na class "Box"
                            list.add(b);        //Adicionar caixas à lista do array
                            adapter = new MyBoxAdapter(getActivity(), list);    //Definir o adaptador com a lista das caixas
                            recyclerView.setAdapter(adapter);   //Definir o layout com o adaptador
                            adapter.notifyDataSetChanged(); //Alterar caso aconteça algo
                        }
                    } else {
                        invisible_create.setVisibility(View.VISIBLE);   //Apresentar texto a dizer para criar caixas
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.error_please_try_again), Toast.LENGTH_SHORT).show();   //Mensagem
                }
            });
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