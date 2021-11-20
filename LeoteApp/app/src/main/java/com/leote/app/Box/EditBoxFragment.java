
/*  Coded by Henrique Leote - 2019/2020   */

//Esta classe é responsável pela página de modificação de caixas

package com.leote.app.Box;

//Livrarias de Funções necessárias

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.leote.app.Adapters.Box;
import com.leote.app.Adapters.FragmentClassData;
import com.leote.app.Auth.StartActivity;
import com.leote.app.MainActivity;
import com.leote.app.R;

public class EditBoxFragment extends BottomSheetDialogFragment {

    //Declaração de variaveis
    public static String link;                                  //Variavel que guarda o link
    EditText Nome, Tipo, Local;                                 //Caixas de texto
    Button Save;                                                //Botão de Guardar
    FirebaseAuth firebaseAuth;                                  //Variavel de acesso à autenticação da base de dados
    DatabaseReference databaseReference;                        //Variavel de acesso às dados da base de dados


    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //Associação desta classe ao layout "edit_box_sheet"
        View view = inflater.inflate(R.layout.edit_box_sheet, container, false);

        if (isNetworkConnected()) {
            //Inicializar o acesso à autenticação da base de dados
            firebaseAuth = FirebaseAuth.getInstance();

            //Este bloco verifica se há sessão iniciada, senão, abre a ativadade para autenticar
            if (firebaseAuth.getCurrentUser() == null) {
                getActivity().finish(); //terminar a ativadade atual
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);  //Adicionar uma animação quando entre as atividades
                startActivity(new Intent(getActivity(), StartActivity.class));  //Abrir uma nova atividade
            }

            Nome = view.findViewById(R.id.editBoxName);             //Associação da variavel à caixa de texto do nome
            Tipo = view.findViewById(R.id.editBoxTipo);             //Associação da variavel à caixa de texto do tipo
            Local = view.findViewById(R.id.editBoxLocal);           //Associação da variavel à caixa de texto do local
            Save = view.findViewById(R.id.buttonSave);              //Associação da variavel ao botão de guardar

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


            //Inicializar a variavel do armazenamento
            databaseReference = FirebaseDatabase.getInstance().getReference();

            Data(); //Metodo dos Dados

            //Quando é clicado no botão de guardar
            Save.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isNetworkConnected())
                        saveBoxInformation();  //metodo de guardar os dados na base de dados
                    else
                        InternetMessage();
                }
            });

        } else
            InternetMessageClose();

        return view;

    }


    //Metodo para apresentar os dados nos campos
    public void Data() {
        if (isNetworkConnected()) {
            DatabaseReference uidRef = FirebaseDatabase.getInstance().getReference().child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Caixas").child(FragmentClassData.value).child("Dados");  //Guardar na variavel o caminho para os dados da caixa
            ValueEventListener valueEventListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    FragmentClassData.value = dataSnapshot.child("boxID").getValue(String.class);  //Definir caixa de texto com o valor
                    link = dataSnapshot.child("link").getValue(String.class);                      //Definir caixa de texto com o valor
                    Nome.setText(dataSnapshot.child("name").getValue(String.class));                //Definir caixa de texto com o valor
                    Local.setText(dataSnapshot.child("local").getValue(String.class));              //Definir caixa de texto com o valor
                    Tipo.setText(dataSnapshot.child("tipo").getValue(String.class));                //Definir caixa de texto com o valor
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
    private void saveBoxInformation() {
        if (TextUtils.isEmpty(Nome.getText().toString()) || TextUtils.isEmpty(Local.getText().toString()) || TextUtils.isEmpty(Tipo.getText().toString())) { //Caso algum campo não tenha texto
            if (TextUtils.isEmpty(Nome.getText().toString())) {  //Caso o campo do nome não tenha texto
                Nome.setError(getResources().getString(R.string.enter_box_name));    //Apresentar um erro
                Nome.requestFocus();    //Apresentar um alerta
                return;
            }

            if (TextUtils.isEmpty(Tipo.getText().toString())) {  //Caso o campo do tipo não tenha texto
                Tipo.setError(getResources().getString(R.string.enter_box_type));    //Apresentar um erro
                Tipo.requestFocus();    //Apresentar um alerta
                return;
            }

            if (TextUtils.isEmpty(Local.getText().toString())) {  //Caso o campo do local não tenha texto
                Local.setError(getResources().getString(R.string.enter_box_location));    //Apresentar um erro
                Local.requestFocus();    //Apresentar um alerta
                return;
            }

        } else {
            if (isNetworkConnected()) {
                Box boxName = new Box(Nome.getText().toString(), Tipo.getText().toString(), Local.getText().toString(), link, FragmentClassData.value); //Guardar os dados na classe "Box"
                FirebaseDatabase.getInstance().getReference(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child("Caixas").child(FragmentClassData.value).child("Dados") //Metodo para inserir os dados na base de dados
                        .setValue(boxName).addOnCompleteListener(new OnCompleteListener<Void>() {
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
