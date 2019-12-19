package com.example.travelblog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private Toolbar mainToolbar;
    private  FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;

    private String current_user_id;

    private FloatingActionButton addPostButton;

    private BottomNavigationView bottomNavigationView;

    private HomeFragment homeFragment;
    private AccountFragment accountFragment;

    SharedPref pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        pref = new SharedPref(this);
        if(pref.loadNightMode() == true){
            setTheme(R.style.DarkTheme);
        }else{
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        mainToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);

        getSupportActionBar().setTitle("Travel Blog");

        if (mAuth.getCurrentUser() != null) {

            bottomNavigationView = findViewById(R.id.mainBottomNav);

            homeFragment = new HomeFragment();
            accountFragment = new AccountFragment();

            replaceFragment(homeFragment);

            bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                    switch (menuItem.getItemId()) {
                        case R.id.bottom_home :
                            replaceFragment(homeFragment);
                            return true;

                        case R.id.bottom_account :
                            replaceFragment(accountFragment);
                            return true;
                    }
                    return false;
                }
            });

            addPostButton = findViewById(R.id.add_post_button);

            addPostButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent addIntent = new Intent(MainActivity.this, NewPostActivity.class);
                    startActivity(addIntent);
                }
            });
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            sendToLogin();
        } else {
            current_user_id = mAuth.getCurrentUser().getUid();
            firebaseFirestore.collection("Users").document(current_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {

                        if (!task.getResult().exists()) {

                            Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
                            startActivity(setupIntent);
                            finish();
                        }
                    } else {
                        String error = task.getException().getMessage();
                        Toast.makeText(MainActivity.this,"Error : "+ error, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        
        switch (item.getItemId()){
            case R.id.action_logout:
                logOut();
                return true;

            case R.id.action_account:
                sendToSetup();
                return true;

            case R.id.action_setting:
                sendToSetting();
                return true;

            default:
                return false;
        }
        
    }

    private void logOut() {
        mAuth.signOut();
        sendToLogin();
    }

    private void sendToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void sendToSetup() {
        Intent intent = new Intent(this, SetupActivity.class);
        startActivity(intent);
    }

    private void sendToSetting() {
        Intent intent = new Intent(this, SettingActivity.class);
        startActivity(intent);
    }

    private void replaceFragment(Fragment fragment) {

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
    }
}
