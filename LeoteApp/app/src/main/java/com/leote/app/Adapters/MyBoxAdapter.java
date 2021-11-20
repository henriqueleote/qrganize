
/*  Coded by Henrique Leote - 2019/2020   */

//Esta classe é responsável por apresentar os dados das caixas na página principal "HomeFragment"

package com.leote.app.Adapters;

//Livrarias de Funções necessárias

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.leote.app.Box.HomeFragment;
import com.leote.app.Box.OpenBoxFragment;
import com.leote.app.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MyBoxAdapter extends RecyclerView.Adapter<MyBoxAdapter.MyViewHolder> {

    //Declaração de variáveis
    Context context; //Variavel de contexto do adaptador
    ArrayList<Box> box; //Lista que contém os dados da classe "Box" em forma de array

    //Associação das variáveis da classe "MyBoxAdapter" aos valores recebidos da função pedida
    public MyBoxAdapter(Context c, ArrayList<Box> b) {
        context = c;
        box = b;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Associação deste adaptador ao layout "cardview_box"
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.cardview_box, parent, false));
    }


    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {

        holder.nome.setText(box.get(position).getName()); //Definir o nome da caixa com o dado que veio da base de dados
        holder.local.setText(box.get(position).getLocal()); //Definir o local da caixa com o dado que veio da base de dados
        Picasso.get()
                .load(box.get(position)
                        .getLink())
                .into(holder.imagem); //Definir o qr da caixa com o link que veio da base de dados através da livraria Picasso


        //Quando clicado no botão "Detalhes" de uma caixa em especifico
        holder.layout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                //Este bloco abre um novo Fragment onde apresnta os dados referentes à caixa escolhida
                FragmentClassData.value = box.get(position).getboxID();
                FragmentManager manager = ((AppCompatActivity) context).getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.fragment_container, new OpenBoxFragment());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

        //Quando se mantem o dedo na caixa, pergunta se deseja eliminar a caixa
        holder.layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(context.getResources().getString(R.string.box_delete_confirm) + box.get(position).getName() + " ?")
                        .setPositiveButton(context.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Caixas").child(box.get(position).getboxID()).removeValue(); //eliminar a caixa da base da dados
                                //Este bloco recarrega a página (Fragment)
                                FragmentManager manager = ((AppCompatActivity) context).getSupportFragmentManager();
                                FragmentTransaction transaction = manager.beginTransaction();
                                transaction.replace(R.id.fragment_container, new HomeFragment());
                                transaction.addToBackStack(null);
                                transaction.commit();
                                Toast.makeText(context, context.getResources().getString(R.string.box_delete_successful), Toast.LENGTH_SHORT).show(); //Mensagem de aviso
                            }
                        })
                        .setNegativeButton(context.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Toast.makeText(context, context.getResources().getString(R.string.no_box_delete), Toast.LENGTH_SHORT).show(); //Mensagem de aviso
                                dialog.cancel();
                            }
                        });
                AlertDialog alertdialog = builder.create();
                alertdialog.show();
                return true;
            }
        });


    }

    //Este metodo conta o numero de caixas presentes existentes
    @Override
    public int getItemCount() {
        return box.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView nome, local;
        ImageView imagem;
        CardView layout;

        //Este bloco associa a variavel da classe à variavel do layout
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            nome = itemView.findViewById(R.id._boxName);
            imagem = itemView.findViewById(R.id._boxImage);
            local = itemView.findViewById(R.id._boxLocal);
            layout = itemView.findViewById(R.id.layout);
        }
    }

}
