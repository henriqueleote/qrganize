
/*  Coded by Henrique Leote - 2019/2020   */

//Esta classe é responsável por apresentar os dados dos produtos na página principal "HomeFragment"

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
import com.leote.app.Box.OpenBoxFragment;
import com.leote.app.Product.OpenProductFragment;
import com.leote.app.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MyProductAdapter extends RecyclerView.Adapter<MyProductAdapter.MyViewHolder> {

    //Declaração de variáveis
    Context context; //Variavel de contexto do adaptador
    ArrayList<Produtos> produtos; //Lista que contém os dados da classe "Produtos" em forma de array

    //Associação das variáveis da classe "MyProductAdapter" aos valores recebidos da função pedida
    public MyProductAdapter(Context c, ArrayList<Produtos> p) {
        context = c;
        produtos = p;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Associação deste adaptador ao layout "cardview_product"
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.cardview_product, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        holder.nome.setText(produtos.get(position).getNome()); //Definir o nome do produto com o dado que veio da base de dados
        holder.tipo.setText(produtos.get(position).getTipo()); //Definir o tipo do produto com o dado que veio da base de dados
        Picasso.get()
                .load(produtos.get(position)
                        .getLink())
                .centerCrop()                                         //Recortar a imagem ao centro
                .fit()                                                //Colocar a imagem de maneira a caber na ImageView
                .into(holder.imagem); //Definir a imagem do produto com o link que veio da base de dados através da livraria Picasso


        //Quando se mantem o dedo na caixa, pergunta se deseja eliminar o produto
        holder.layout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(context.getResources().getString(R.string.product_delete_confirm) + produtos.get(position).getNome() + " ?")
                        .setPositiveButton(context.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                FragmentClassData.value = produtos.get(position).getProductBoxID();
                                //eliminar o produto da base da dados
                                FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Caixas").child(produtos.get(position).getProductBoxID()).child("Artigos").child(produtos.get(position).getProductID()).removeValue();
                                //Este bloco recarrega a página (Fragment)
                                FragmentManager manager = ((AppCompatActivity) context).getSupportFragmentManager();
                                FragmentTransaction transaction = manager.beginTransaction();
                                transaction.replace(R.id.fragment_container, new OpenBoxFragment());
                                transaction.addToBackStack(null);
                                transaction.commit();
                                Toast.makeText(context, context.getResources().getString(R.string.product_delete_successful), Toast.LENGTH_SHORT).show(); //Mensagem de aviso
                            }
                        })
                        .setNegativeButton(context.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Toast.makeText(context, context.getResources().getString(R.string.no_product_delete), Toast.LENGTH_SHORT).show(); //Mensagem de aviso
                                dialog.cancel();
                            }
                        });
                AlertDialog alertdialog = builder.create();
                alertdialog.show();
                return true;
            }
        });

        //Quando clicado no botão "Detalhes" de uma caixa em especifico
        holder.layout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                //Este bloco abre um novo Fragment onde apresnta os dados referentes à caixa escolhida
                FragmentClassData.value = produtos.get(position).getProductBoxID();
                FragmentClassData.value2 = produtos.get(position).getProductID();
                FragmentManager manager = ((AppCompatActivity) context).getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.replace(R.id.fragment_container, new OpenProductFragment());
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });

    }

    //Este metodo conta o numero de produtos presentes existentes
    @Override
    public int getItemCount() {
        return produtos.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView nome, tipo;
        ImageView imagem;
        CardView layout;

        //Este bloco associa a variavel da classe à variavel do layout
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            nome = itemView.findViewById(R.id.productName);
            tipo = itemView.findViewById(R.id.productTipo);
            imagem = itemView.findViewById(R.id.productImage);
            layout = itemView.findViewById(R.id.layout);
        }
    }
}
