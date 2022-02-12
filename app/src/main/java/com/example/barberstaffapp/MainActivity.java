package com.example.barberstaffapp;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import com.example.barberstaffapp.Adapter.MyStateAdapter;
import com.example.barberstaffapp.Common.Common;
import com.example.barberstaffapp.Common.SpacesItemDecoration;
import com.example.barberstaffapp.Interface.IOnAllStateLoadListener;
import com.example.barberstaffapp.Model.Barber;
import com.example.barberstaffapp.Model.City;
import com.example.barberstaffapp.Model.Salon;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.reflect.TypeToken;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.View;


import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

public class MainActivity extends AppCompatActivity implements IOnAllStateLoadListener {
    @BindView(R.id.recycler_state)
    RecyclerView recycler_state;

    CollectionReference allSalonCollection;

    IOnAllStateLoadListener iOnAllStateLoadListener;
    MyStateAdapter adapter;
    AlertDialog dialog;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Paper.init(this);
        String user = Paper.book().read(Common.LOGGED_KEY);
        if(TextUtils.isEmpty(user))
        {

            ButterKnife.bind(this);

            initView();

            init();

            loadAllStateFromFireStore();
        }else
        {
            Gson gson = new Gson();
            Common.state_name = Paper.book().read(Common.STATE_KEY);
            Common.selectSalon = gson.fromJson(Paper.book().read(Common.SALON_KEY,""),new TypeToken<Salon>(){}.getType());
            Common.currentBarber = gson.fromJson(Paper.book().read(Common.BARBER_KEY,""),new TypeToken<Barber>(){}.getType());

            Intent intent = new Intent(this,StaffHomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();

        }




    }

    private void loadAllStateFromFireStore() {
        dialog.show();
        allSalonCollection.get().addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                iOnAllStateLoadListener.onAllStateLoadListenerFailure(e.getMessage());
            }
        }).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<City> cities = new ArrayList<>();
                    for (DocumentSnapshot citySnapShot : task.getResult()) {

                        City city = citySnapShot.toObject(City.class);
                        cities.add(city);

                    }
                    iOnAllStateLoadListener.onAllStateLoadListenerSuccess(cities);

                }
            }
        });

    }

    private void init() {
        allSalonCollection = FirebaseFirestore.getInstance().collection("AllSalon");
        iOnAllStateLoadListener = this;
        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();


    }

    private void initView() {
        recycler_state.setHasFixedSize(true);
        recycler_state.setLayoutManager(new GridLayoutManager(this, 2));
        recycler_state.addItemDecoration(new SpacesItemDecoration(8));


    }

    @Override
    public void onAllStateLoadListenerSuccess(List<City> cityList) {
        adapter = new MyStateAdapter(this, cityList);
        recycler_state.setAdapter(adapter);
        dialog.dismiss();
    }

    @Override
    public void onAllStateLoadListenerFailure(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        dialog.dismiss();
    }
}