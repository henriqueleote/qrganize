package com.leote.app;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.leote.app.Auth.ProfileFragment;
import com.leote.app.Auth.StartActivity;
import com.leote.app.Box.HomeFragment;
import com.leote.app.Box.ScanFragment;

public class MainActivity extends AppCompatActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth firebaseAuth;

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (isNetworkConnected()) {
            BottomNavigationView navigation = findViewById(R.id.nav_view);
            navigation.setOnNavigationItemSelectedListener(this);

            firebaseAuth = FirebaseAuth.getInstance();
            if (firebaseAuth.getCurrentUser() == null) {
                finish();
                startActivity(new Intent(MainActivity.this, StartActivity.class));
            }

            loadFragment(new ProfileFragment());
        } else
            InternetMessageClose();

    }

    private boolean loadFragment(Fragment fragment) {
        if (isNetworkConnected()) {
            if (fragment != null) {

                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();

                return true;
            }
            return false;
        } else
            InternetMessage();
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        Fragment fragment = null;

        switch (item.getItemId()) {
            case R.id.navigation_home:
                if (isNetworkConnected())
                    fragment = new HomeFragment();
                else
                    InternetMessage();
                break;

            case R.id.scan:
                if (isNetworkConnected())
                    fragment = new ScanFragment();
                else
                    InternetMessage();
                break;

            case R.id.navigation_profile:
                if (isNetworkConnected())
                    fragment = new ProfileFragment();
                else
                    InternetMessage();
                break;

        }
        return loadFragment(fragment);
    }

    public void InternetMessageClose() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setMessage(this.getResources().getString(R.string.internet_access_no))
                .setPositiveButton(getResources().getString(R.string.ok), new
                        DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                finishAffinity();
                                System.exit(0);
                            }
                        })
                .setCancelable(false)
                .show();
    }

    public void InternetMessage() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setMessage(this.getResources().getString(R.string.internet_access_no))
                .setPositiveButton(getResources().getString(R.string.ok), null)
                .setCancelable(false)
                .show();
    }
}
