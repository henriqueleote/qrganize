
/*  Coded by Henrique Leote - 2019/2020   */

//Esta classe é responsável pela página de registo do utilizador

package com.leote.app.Auth;

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
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.leote.app.Adapters.UserInformation;
import com.leote.app.MainActivity;
import com.leote.app.R;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.wasabeef.picasso.transformations.CropCircleTransformation;

import static android.app.Activity.RESULT_OK;


public class RegisterFragment extends BottomSheetDialogFragment {

    //Declaração de variaveis
    public static String link;                                                              //Variavel que guarda o link da imagem
    public static Drawable oldDrawable;                                                     //Variavel que guarda a imagem
    private ImageView img;                                                                  //Imagem de layout
    private Uri imguri;                                                                     //Variavel com o endereço da imagem
    private EditText editTextEmail, editTextPassword, editTextPassword2, editTextName;      //Caixas de texto
    private Button buttonSignup;                                                            //Botão de Registo
    private FirebaseAuth firebaseAuth;                                                      //Variavel de autenticação da base de dados
    private TextView Terms;                                                                 //Variavel de texto

    //Este metodo verifica se o e-mail é valido
    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(final Dialog dialog, int style) {
        super.setupDialog(dialog, style);

        //Associação desta classe ao layout "register_sheet"
        View view = View.inflate(getContext(), R.layout.register_sheet, null);

        if (isNetworkConnected()) {
            //Inicializar o acesso à autenticação da base de dados
            firebaseAuth = FirebaseAuth.getInstance();

            //Este bloco verifica se há sessão iniciada, senão, abre a ativadade para autenticar
            if (firebaseAuth.getCurrentUser() != null) {
                getActivity().finish();
                getActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                startActivity(new Intent(getActivity(), MainActivity.class));
            }

            final CheckBox toggle = view.findViewById(R.id.toggBtn);            //Associação da variavel à caixa de toggle
            //Caso a toggle seja clicada
            toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {    //Caso a toggle esteja checked
                        editTextPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance()); //Mostra a palavra-passe
                        editTextPassword2.setTransformationMethod(HideReturnsTransformationMethod.getInstance()); //Mostra a palavra-passe
                        toggle.setText(getResources().getString(R.string.hide_password));
                    } else {            //Caso a toggle esteja non-checked
                        editTextPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());   //Esconder a palavra-passe
                        editTextPassword2.setTransformationMethod(PasswordTransformationMethod.getInstance());   //Esconder a palavra-passe
                        toggle.setText(getResources().getString(R.string.show_password));
                    }
                }
            });

            Terms = view.findViewById(R.id.terms);                          //Associação da variavel ao texto do layout
            editTextName = view.findViewById(R.id.editTextName);            //Associação da variavel à caixa de texto do nome
            editTextEmail = view.findViewById(R.id.editTextEmail);          //Associação da variavel à caixa de texto do e-mail
            editTextPassword = view.findViewById(R.id.editTextPassword);    //Associação da variavel à caixa de texto da password
            editTextPassword2 = view.findViewById(R.id.editTextPassword2);  //Associação da variavel à caixa de texto da confirmação da password
            buttonSignup = view.findViewById(R.id.buttonSignup);            //Associação da variavel com o botão de registo
            img = view.findViewById(R.id.mImageView);                       //Associação variavel de imagem à imagem do layout
            oldDrawable = img.getDrawable();                                //Guardar a imagem atual na variavel

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

            //Quando é clicado no botão de registar
            buttonSignup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isNetworkConnected())
                        registerUser();                                         //Declaração do metodo de registar o utilizador
                    else
                        InternetMessage();
                }
            });

            Terms.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new androidx.appcompat.app.AlertDialog.Builder(getActivity())
                            .setMessage(getActivity().getResources().getString(R.string.terms_message))
                            .setPositiveButton(getResources().getString(R.string.yes), null)
                            .setCancelable(false)
                            .show();
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

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (isNetworkConnected()) {
            if (requestCode == 1 && resultCode == RESULT_OK && data != null & data.getData() != null) {

                imguri = data.getData();    //Ir buscar a imagem escolhida
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

    //Metodo para registar o utilizar
    private void registerUser() {
        if (img.getDrawable() == oldDrawable) {
            Toast.makeText(getActivity(), "A foto de perfil é obrigatória para a criação da conta", Toast.LENGTH_LONG).show();
        } else {

            if (link.isEmpty()) {
                Toast.makeText(getActivity(), "A foto de perfil é obrigatória para a criação da conta", Toast.LENGTH_SHORT).show();
            }
            if (TextUtils.isEmpty(editTextName.getText().toString())) {
                editTextName.setError(getResources().getString(R.string.enter_your_name));  //Apresentar um erro
                editTextName.requestFocus();    //Apresentar um alerta
                return;
            }
            if (TextUtils.isEmpty(editTextEmail.getText().toString())) {
                editTextEmail.setError(getResources().getString(R.string.enter_your_mail));   //Apresentar um erro
                editTextEmail.requestFocus();   //Apresentar um alerta
                return;
            }
            if (TextUtils.isEmpty(editTextPassword.getText().toString())) {
                editTextPassword.setError(getResources().getString(R.string.enter_your_password)); //Apresentar um erro
                editTextPassword.requestFocus();    //Apresentar um alerta
                return;
            }
            if (TextUtils.isEmpty(editTextPassword2.getText().toString())) {
                editTextPassword2.setError(getResources().getString(R.string.password_confirm)); //Apresentar um erro
                editTextPassword2.requestFocus();   //Apresentar um alerta
                return;
            }
            if (editTextPassword.getText().toString().length() < 8) {
                editTextPassword.setError(getResources().getString(R.string.eight_password));   //Apresentar um erro
                editTextPassword.requestFocus();    //Apresentar um alerta
                return;
            }
            if (editTextPassword2.getText().toString().length() < 8) {
                editTextPassword2.setError(getResources().getString(R.string.eight_password));  //Apresentar um erro
                editTextPassword2.requestFocus();   //Apresentar um alerta
                return;
            }

            if (isEmailValid(editTextEmail.getText().toString()) == true) {  //Caso o email esteja correto
                if (editTextPassword.getText().toString().equals(editTextPassword2.getText().toString())) { //Caso as palavras-passe coincidam
                    if (isNetworkConnected()) {
                        final ProgressDialog mDialog = new ProgressDialog(getActivity());       //Iniciar a barra de progresso
                        mDialog.setMessage(getResources().getString(R.string.registering_wait));                             //Inserir uma mensagem
                        mDialog.setCanceledOnTouchOutside(false);                               //Retirar a função de fechar a barra quando se carrega no resto do ecrã
                        mDialog.show();                                                         //Mostrar a barra
                        firebaseAuth.createUserWithEmailAndPassword(editTextEmail.getText().toString(), editTextPassword.getText().toString())    //Função para criar uma conta com o e-mail e password
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) { //Caso a tarefa seja concluída com sucesso
                                            UserInformation user = new UserInformation(editTextName.getText().toString(), link, String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));  //Guardar os dados na classe "UserInformation"
                                            FirebaseDatabase.getInstance().getReference(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                    .child("Profile") //Metodo para inserir os dadosna base de dados
                                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    mDialog.dismiss();   //Parar a barra de progresso
                                                    if (task.isSuccessful()) {  //Caso não haja erros
                                                        getActivity().finish(); //Terminar a atividade
                                                        startActivity(new Intent(getActivity().getApplicationContext(), MainActivity.class));   //Abrir atividade das caixas
                                                    } else {    //Caso haja erros
                                                        Toast.makeText(getActivity(), getResources().getString(R.string.error_please_try_again), Toast.LENGTH_SHORT).show();   //Mensagem
                                                    }
                                                }
                                            });

                                        } else {
                                            mDialog.dismiss();   //Parar a barra de progresso
                                            Toast.makeText(getActivity(), task.getException().getMessage(), Toast.LENGTH_LONG).show();  //Mensagem
                                        }
                                    }
                                });
                    } else
                        InternetMessage();

                } else {  //Caso as palavras-passe não coincidam
                    Toast.makeText(getActivity(), getResources().getString(R.string.passwords_dont_match), Toast.LENGTH_SHORT).show(); //Mensagem
                }

            } else {
                editTextEmail.setError(getResources().getString(R.string.email_not_valid));    //Apresentar um erro
                editTextEmail.requestFocus();   //Apresentar um alerta
                return;
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