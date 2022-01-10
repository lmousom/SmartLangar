package com.mousom.smartlangar;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        BottomNavigationView navView = findViewById(R.id.bottomNavigationView);

        //Pass the ID's of Different destinations
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.home_frag, R.id.upload_frag, R.id.profileFrag )
                .build();

        //Initialize NavController.
        NavController navController = Navigation.findNavController(HomeActivity.this, R.id.fragment);
        NavigationUI.setupActionBarWithNavController(HomeActivity.this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);



    }



}