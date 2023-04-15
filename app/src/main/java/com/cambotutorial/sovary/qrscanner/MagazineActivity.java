package com.cambotutorial.sovary.qrscanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.cambotutorial.sovary.qrscanner.Objects.CardCaptionedImageAdapterQR;
import com.cambotutorial.sovary.qrscanner.Objects.CardCaptionedImageMagazine;
import com.cambotutorial.sovary.qrscanner.Objects.Magazine;
import com.cambotutorial.sovary.qrscanner.Objects.PrefConfig;
import com.cambotutorial.sovary.qrscanner.Objects.Product;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MagazineActivity extends AppCompatActivity implements ValueEventListener {

    private SearchView searchView;
    private ProgressBar progress;
    private String id;
    private String name;
    private String magazineId;
    private String date;
    private List<Magazine> magazineList;
    private RecyclerView recyclerView;
    private FirebaseDatabase firebaseDatabase;
    private TextView loadingText;
    private TextView emptyList;
    private Toolbar toolbar;
    private int searchingType;
    private Button searchingButton;
    AnimationSet s;
    Animation scaleUp, scaleDown;
    private CalendarView calendarView;
    private CardCaptionedImageMagazine adapter;
    private String searchingDate="";
    private Button buttonDate1, buttonDate2;
    private  AlertDialog dialog;

    AlertDialog loadingDialog;
    MediaPlayer clickAlert;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_magazine);
        PrefConfig.saveTotalInPref(getApplicationContext(), 1);
        searchView = findViewById(R.id.searchView);
        searchingButton = findViewById(R.id.search_button);
        scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up);
        scaleDown = AnimationUtils.loadAnimation(this, R.anim.scale_down);
        s = new AnimationSet(false);//false means don't share interpolators
        s.addAnimation(scaleDown);
        s.addAnimation(scaleUp);
        searchingType = PrefConfig.loadTotalFromPref(getApplicationContext());
        if (searchingType==1)
        {
            searchView.setVisibility(View.VISIBLE);
        }
        else
        {
            searchingButton.setVisibility(View.VISIBLE);
        }
        magazineList = new ArrayList<>();

        progress = findViewById(R.id.progress_Magazine);
        progress.setMax(1000);
        progress.setVisibility(View.VISIBLE);
        loadingText = findViewById(R.id.loading_text);
        loadingText.setVisibility(View.VISIBLE);
        emptyList = findViewById(R.id.emptyTv);
        ObjectAnimator.ofInt(progress, "progress", 1000).setDuration(2000).start();

        firebaseDatabase = FirebaseDatabase.getInstance("https://magazineapp-3bfa5-default-rtdb.europe-west1.firebasedatabase.app/");
        recyclerView = findViewById(R.id.rv_Magazine);


        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MagazineActivity.super.onBackPressed();
                clickAlert.start();
            }
        });
        clickAlert = MediaPlayer.create(MagazineActivity.this, R.raw.click);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true; }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        searchingType = PrefConfig.loadTotalFromPref(getApplicationContext());
        if (id == R.id.m_lo) {
            if (progress.getVisibility() == View.VISIBLE)
            {
                Toast.makeText(getApplicationContext(), "Sprawdź połaczenie z siecią", Toast.LENGTH_SHORT).show();

            }
            else if (progress.getVisibility() == View.GONE && searchingType==2){
                PrefConfig.saveTotalInPref(getApplicationContext(), 1);
                Toast.makeText(MagazineActivity.this, "Wyszukiwanie po nazwie", Toast.LENGTH_SHORT).show();
                searchView.setVisibility(View.VISIBLE);
                searchingButton.setVisibility(View.GONE);
                clickAlert.start();
                adapter.getFilter().filter("");
            }
            else if (progress.getVisibility() == View.GONE && searchingType==1){
                Toast.makeText(getApplicationContext(), "Już wybrano", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        else if (id == R.id.m_assets) {
            if (progress.getVisibility() == View.VISIBLE)
            {
                Toast.makeText(getApplicationContext(), "Sprawdź połaczenie z siecią", Toast.LENGTH_SHORT).show();

            }
            else if (progress.getVisibility() == View.GONE && searchingType==1){
                PrefConfig.saveTotalInPref(getApplicationContext(), 2);
                Toast.makeText(MagazineActivity.this, "Wyszukiwanie po dacie", Toast.LENGTH_SHORT).show();
                searchView.setVisibility(View.GONE);
                searchingButton.setVisibility(View.VISIBLE);
                adapter.getFilter().filter("");
                clickAlert.start();
            }
            else if (progress.getVisibility() == View.GONE && searchingType==2){
                Toast.makeText(getApplicationContext(), "Już wybrano", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
       return super.onOptionsItemSelected(item); }

    @Override
    protected void onPause() {
        super.onPause();
        firebaseDatabase.getReference("Magazine").orderByChild("date").removeEventListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        firebaseDatabase.getReference("Magazine").orderByChild("date").addValueEventListener(this);

    }

    private void searchingInMagazineNew()
    {

        AlertDialog.Builder alert = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AppTheme));
        LayoutInflater inflater = getLayoutInflater();
        alert.setCancelable(true);
        alert.setTitle("Wyszukiwanie");
        alert.setCancelable(false);
        View view = inflater.inflate(R.layout.alert_magazine, findViewById(R.id.notification_main_column));

        buttonDate1 = view.findViewById(R.id.btn_date1);
        buttonDate2 = view.findViewById(R.id.btn_date2);
        Button buttonSubmit = view.findViewById(R.id.btn_done);
        Button buttonReset = view.findViewById(R.id.btn_reset);



        buttonDate1.setOnClickListener(view62 -> selectDate1());
        buttonDate2.setOnClickListener(view6 -> selectDate2());



        buttonSubmit.setOnClickListener(view63 -> {

            Submit();

        });

        buttonReset.setOnClickListener(view63 -> {

            dialog.dismiss();
            searchingDate="";
            adapter.getFilter().filter(searchingDate);
        });

        alert.setView(view);
        dialog = alert.create();
        dialog.getWindow().setBackgroundDrawableResource(R.color.background_color);
        dialog.show();
    }

    private void Submit() {
        if (buttonDate1.getText().toString().equals("Data poczatkowa"))
        {
            Toast.makeText(getApplicationContext(),"Wybierz datę." , Toast.LENGTH_SHORT).show();
        }
        else if (!buttonDate1.getText().toString().equals("Data poczatkowa") && !buttonDate2.getText().toString().equals("Data koncowa"))
        {
            searchingDate = buttonDate1.getText()+"&&"+buttonDate2.getText();
            adapter.getFilter().filter(searchingDate);
            dialog.dismiss();
            searchingDate="";
        }
        else if (!buttonDate1.getText().toString().equals("Data poczatkowa") && buttonDate2.getText().toString().equals("Data koncowa"))
        {
            searchingDate = buttonDate1.getText()+"&&"+buttonDate1.getText();
            adapter.getFilter().filter(searchingDate);
            dialog.dismiss();
            searchingDate="";
        }
    }

    private void selectDate2() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                String dayString;
                String mouthString;
                month = month+1;


                if (day<10)
                {
                    dayString="0"+day;
                }
                else
                {
                    dayString = String.valueOf(day);
                }
                if (month<10)
                {
                    mouthString="0"+month;
                }
                else
                {
                    mouthString = String.valueOf(month);
                }
                buttonDate2.setText(year + "-" + (mouthString) + "-" + dayString);
            }
        }, year, month, day);datePickerDialog.show();
    }

    private void selectDate1() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog1 = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year1, int month1, int day1) {
                String dayString1;
                String mouthString1;
                    month1 = month1+1;
                if (day1<10)
                {
                    dayString1="0"+day1;
                }
                else
                {
                    dayString1 = String.valueOf(day1);
                }
                if (month1<10)
                {
                    mouthString1="0"+month1;
                }
                else
                {
                    mouthString1 = String.valueOf(month1);
                }
                buttonDate1.setText(year1 + "-" + (mouthString1) + "-" +  dayString1);
            }
        }, year, month, day);datePickerDialog1.show();
    }


    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {
        int size = (int) snapshot.getChildrenCount();
        magazineList.clear();
        if (size!=0)
        {



            for (DataSnapshot postSnapshot : snapshot.getChildren()){
                Magazine magazine = postSnapshot.getValue(Magazine.class);
                assert magazine != null;
                magazineList.add(new Magazine(magazine.getId(), magazine.getQR(), magazine.getName(), magazine.getDate()));
            }

            progress.setVisibility(View.GONE);
            loadingText.setVisibility(View.GONE);
            emptyList.setVisibility(View.GONE);

            adapter = new CardCaptionedImageMagazine(magazineList);
            recyclerView.setAdapter(adapter);

            LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
            recyclerView.setLayoutManager(layoutManager);

            searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    adapter.getFilter().filter(s);

                    return false;
                }
            });



            searchingButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    view.startAnimation(s);
                    clickAlert.start();
                    searchingInMagazineNew();
                }
            });


            adapter.setListener(new CardCaptionedImageMagazine.Listener() {
                @Override
                public void onClick(int position) {
                    Magazine clickedMagazine = new Magazine();
                    clickedMagazine = magazineList.get(position);
                    id = clickedMagazine.getQR();
                    name = clickedMagazine.getName();
                    magazineId = clickedMagazine.getId();
                    date = clickedMagazine.getDate();
                    setAlertDialog(magazineId, id, name, date);
                }
            });

        }
        else
        {
            progress.setVisibility(View.GONE);
            loadingText.setVisibility(View.GONE);
            emptyList.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {

    }

    private void setAlertDialog(String IdMagazine, String IdQRText, String NameText, String dateText)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(MagazineActivity.this);
        builder.setTitle("Usuwanie pozycji");
        builder.setMessage("Czy na pewno chcesz usunąć z magazynu: \n\nNazwa: "+ NameText +"\nKod:" + IdQRText + "\nData: " +dateText);
        builder.setPositiveButton("Usuń", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                clickAlert.start();
                if (getConnectionType(getApplicationContext())!=0)
                {
                    InitObjects();
                    firebaseDatabase.getReference("Magazine").child(IdMagazine).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(getApplicationContext(), "Pomyślnie usunięto produkt", Toast.LENGTH_SHORT).show();
                            loadingDialog.dismiss();
                        }
                    });
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Brak łączności z siecią", Toast.LENGTH_SHORT).show();
                    setAlertDialog(IdMagazine, IdQRText, NameText, dateText );
                }
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton("Anuluj", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                clickAlert.start();
            }
        });
        builder.show();
    }


    private static int getConnectionType(Context context)
    {
        int result =0;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info == null)
        {
            return 0;
        }
        else if (info.getType() == ConnectivityManager.TYPE_WIFI)
        {
            return 1;
        }
        else if (info.getType() == ConnectivityManager.TYPE_MOBILE)
        {
            return 2;
        }
        else if (info.getType() == ConnectivityManager.TYPE_VPN)
        {
            return 3;
        }
        return  result;
    }

    private void InitObjects()
    {
        loadingDialog = new AlertDialog.Builder(this).create();
        ProgressBar progressBar = new ProgressBar(getApplicationContext());
        loadingDialog.setView(progressBar);
        loadingDialog.setCancelable(false);
        loadingDialog.setTitle("Łączenie");
        loadingDialog.setMessage("Trwa łączenie z bazą, jeśli trwa zbyt długo, upewnij się, że masz łączność internetem.");
        loadingDialog.setButton(Dialog.BUTTON_POSITIVE, "Odśwież aplikacje", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                recreate();
                Toast.makeText(getApplicationContext(), "Odswieżono", Toast.LENGTH_SHORT).show();
            }
        });
        loadingDialog.show();
    }
}