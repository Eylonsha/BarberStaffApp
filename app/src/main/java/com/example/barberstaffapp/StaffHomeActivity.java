package com.example.barberstaffapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.barberstaffapp.Adapter.MyTimeSlotAdapter;
import com.example.barberstaffapp.Common.Common;
import com.example.barberstaffapp.Common.SpacesItemDecoration;
import com.example.barberstaffapp.Interface.ITimeSlotLoadListener;
import com.example.barberstaffapp.Model.TimeSlot;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.HorizontalCalendarView;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

public class StaffHomeActivity extends AppCompatActivity implements ITimeSlotLoadListener {

    @BindView(R.id.activity_main)
    DrawerLayout drawerLayout;
    @BindView(R.id.navigation_view)
    NavigationView navigationView;
    @BindView(R.id.recycler_time_slot)
    RecyclerView recycler_time_slot;
    @BindView(R.id.calendarView)
    HorizontalCalendarView calendarView;

    ActionBarDrawerToggle actionBarDrawerToggle;


    DocumentReference barberDoc;
    ITimeSlotLoadListener iTimeSlotLoadListener;
    android.app.AlertDialog alertDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_home);

        ButterKnife.bind(this);

        init();

        initView();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                R.string.open, R.string.close);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.menu_exit)
                    logOut();

                return true;
            }
        });


        //coping from first app

        alertDialog = new SpotsDialog.Builder().setCancelable(false).setContext(this)
                .build();

        Calendar date = Calendar.getInstance();
        date.add(Calendar.DATE, 0);
        loadAvailableTimeSlotBarber(Common.currentBarber.getBarberId(), Common.simpleDateFormat.format(date.getTime()));

        recycler_time_slot.setHasFixedSize(true);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        recycler_time_slot.setLayoutManager(layoutManager);
        recycler_time_slot.addItemDecoration(new SpacesItemDecoration(8));


        Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.DATE, 0);
        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.DATE, 2);

        HorizontalCalendar horizontalCalendar = new HorizontalCalendar.Builder(this, R.id.calendarView)
                .range(startDate, endDate).datesNumberOnScreen(1)
                .mode(HorizontalCalendar
                        .Mode.DAYS)
                .defaultSelectedDate(startDate).configure().end().build();

        horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Calendar date, int position) {
                if (Common.bookingDate.getTimeInMillis() != date.getTimeInMillis()) {
                    Common.bookingDate = date;
                    loadAvailableTimeSlotBarber(Common.currentBarber.getBarberId(), Common.simpleDateFormat.format(date.getTime()));


                }
            }
        });
    }

    private void logOut() {
        Paper.init(this);
        Paper.book().delete(Common.SALON_KEY);
        Paper.book().delete(Common.BARBER_KEY);
        Paper.book().delete(Common.LOGGED_KEY);
        Paper.book().delete(Common.STATE_KEY);

        new AlertDialog.Builder(this).setMessage("Are you sure you want to exit ?").setCancelable(false)
                .setPositiveButton("YES", (dialog, which) -> {

                    Intent intent = new Intent(StaffHomeActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();


                }).setNegativeButton("CANCEL", (dialogInterface, which) -> dialogInterface.dismiss()).show();

    }

    private void loadAvailableTimeSlotBarber(String barberId, String bookDate) {
        alertDialog.show();
        barberDoc = FirebaseFirestore.getInstance()
                .collection("AllSalon")
                .document(Common.state_name)
                .collection("Branch")
                .document(Common.selectSalon.getSalonId())
                .collection("Barbers")
                .document(barberId);


        barberDoc.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot.exists()) {
                    CollectionReference date = FirebaseFirestore.getInstance()
                            .collection("AllSalon")
                            .document(Common.state_name)
                            .collection("Branch")
                            .document(Common.selectSalon.getSalonId())
                            .collection("Barbers")
                            .document(barberId)
                            .collection(bookDate);

                    date.get().addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            QuerySnapshot querySnapshot = task1.getResult();
                            if (querySnapshot.isEmpty())
                                iTimeSlotLoadListener.onTimeSlotLoadEmpty();
                            else {
                                List<TimeSlot> timeSlots = new ArrayList<>();
                                for (QueryDocumentSnapshot document : task1.getResult())
                                    timeSlots.add(document.toObject(TimeSlot.class));
                                iTimeSlotLoadListener.onTimeSlotLoadSuccess(timeSlots);
                            }
                        }
                    }).addOnFailureListener(e -> iTimeSlotLoadListener.onTimeSlotLoadFailed(e.getMessage()));

                }

            }
        });

    }

    private void init() {

        iTimeSlotLoadListener = this;
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this).setMessage("Are you sure you want to exit ?").setCancelable(false)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(StaffHomeActivity.this, "Fake function exit", Toast.LENGTH_SHORT).show();
                    }
                }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
            }
        }).show();
    }

    @Override
    public void onTimeSlotLoadSuccess(List<TimeSlot> timeSlotList) {
        MyTimeSlotAdapter adapter = new MyTimeSlotAdapter(this, timeSlotList);
        recycler_time_slot.setAdapter(adapter);
        alertDialog.dismiss();
    }

    @Override
    public void onTimeSlotLoadFailed(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        alertDialog.dismiss();
    }

    @Override
    public void onTimeSlotLoadEmpty() {
        MyTimeSlotAdapter adapter = new MyTimeSlotAdapter(this);
        recycler_time_slot.setAdapter(adapter);
        alertDialog.dismiss();
    }
}