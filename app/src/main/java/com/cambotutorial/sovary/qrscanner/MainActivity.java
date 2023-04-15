package com.cambotutorial.sovary.qrscanner;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.cambotutorial.sovary.qrscanner.Objects.Magazine;
import com.cambotutorial.sovary.qrscanner.Objects.PrefConfig;
import com.cambotutorial.sovary.qrscanner.Objects.Product;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class MainActivity extends AppCompatActivity
{



    Button btn_scan, btn_add_QR, btn_QR_codes, btn_Magazine, btn_addNew, btn_reset;
    EditText editTextDate;
    String scanName ="";
    String productId= "";
    String magazineId ="";
    String date="";
    int scanOperation;
    int inDatabaseScan = 0;
    private Toolbar toolbar;
    AlertDialog  loadingDialog;
    Animation scaleUp, scaleDown;
    MediaPlayer clickAlert;
    AnimationSet s;
    private  AlertDialog dialog;
    private FirebaseDatabase firebaseDatabase;
    private EditText editTextAdd;
    private TextView textView_result;
    private ProgressBar progressBar_auto;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseDatabase = FirebaseDatabase.getInstance("https://magazineapp-3bfa5-default-rtdb.europe-west1.firebasedatabase.app/");
        btn_addNew = findViewById(R.id.btn_add_new);
        editTextAdd = findViewById(R.id.editText_Code);
        btn_reset = findViewById(R.id.btn_clear);
        textView_result = findViewById(R.id.textView_result);
        progressBar_auto = findViewById(R.id.progress_auto);

        editTextAdd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (String.valueOf(editTextAdd.getText()).length()!=0)
                {
                    textView_result.setVisibility(View.GONE);
                    progressBar_auto.setVisibility(View.VISIBLE);
                    productId = String.valueOf(editTextAdd.getText());
                    firebaseDatabase.getReference("Products").child(productId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            int size=0;
                            if (snapshot.exists())
                            {
                                size = (int) snapshot.getChildrenCount();
                            }
                            if (size==0)
                            {
                                textView_result.setVisibility(View.VISIBLE);
                                progressBar_auto.setVisibility(View.GONE);
                                textView_result.setText("Brak wyniku w bazie");

                            }
                            else
                            {
                                textView_result.setVisibility(View.VISIBLE);
                                progressBar_auto.setVisibility(View.GONE);
                                Product product1 = snapshot.getValue(Product.class);
                                assert product1 != null;
                                scanName = product1.getName();
                                textView_result.setText(scanName);
                                savingToMAgazine();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                else
                {
                    textView_result.setVisibility(View.GONE);
                    progressBar_auto.setVisibility(View.GONE);
                }
            }
        });



        btn_addNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (String.valueOf(editTextAdd.getText()).length()!=0)
                {
                    productId = String.valueOf(editTextAdd.getText());
                    InitObjects();
                    firebaseDatabase.getReference("Products").child(productId).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            int size = (int) snapshot.getChildrenCount();

                            if (size!=0)
                            {
                                Product product = snapshot.getValue(Product.class);
                                assert product != null;
                                scanName = product.getName();

                                loadingDialog.dismiss();
                                savingToMAgazine();
                            }
                            else
                            {
                                loadingDialog.dismiss();
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setTitle("Wynik skanowania");
                                builder.setCancelable(false);
                                builder.setMessage("Brak produktu w bazie danych. \nKod: " +productId);
                                builder.setPositiveButton("Dodaj kod kreskowy", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i)
                                    {
                                        AddingQRDialongNew();
                                        clickAlert.start();
                                        dialogInterface.dismiss();
                                    }
                                });
                                builder.setNegativeButton("Anuluj", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        clickAlert.start();
                                        dialogInterface.dismiss();
                                        Toast.makeText(getApplicationContext(), "Anulowano", Toast.LENGTH_SHORT).show();
                                        editTextAdd.setText("");
                                    }
                                });
                                builder.show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Brak wpisanego Id", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btn_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editTextAdd.setText("");
            }
        });


        btn_scan =findViewById(R.id.btn_scan);
        btn_scan.setOnClickListener(v->
        {

            v.startAnimation(s);
            clickAlert.start();
            if (getConnectionType(getApplicationContext())!=0)
            {
                scanOperation =1;
                scanCode();
            }
            else
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Problem z siecią");
                builder.setMessage("Brak łączności z internetem");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
                {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        clickAlert.start();
                        dialogInterface.dismiss();
                    }
                }).show();
            }

        });

        btn_add_QR =findViewById(R.id.btn_add_QR);
        btn_add_QR.setOnClickListener(v->
        {
            v.startAnimation(s);
            clickAlert.start();
            if (getConnectionType(getApplicationContext())!=0)
            {
                scanOperation =2;
                scanCode();
            }
            else
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Problem z siecią");
                builder.setMessage("Brak łączności z internetem");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        clickAlert.start();
                        dialogInterface.dismiss();
                    }
                }).show();
            }

        });

        btn_Magazine =findViewById(R.id.btn_magazine);
        btn_Magazine.setOnClickListener(v->
        {
            v.startAnimation(s);
            clickAlert.start();
            if (getConnectionType(getApplicationContext())!=0)
            {
                Intent intent = new Intent(MainActivity.this, MagazineActivity.class);
                startActivity(intent);
            }
            else
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Problem z siecią");
                builder.setMessage("Brak łączności z internetem");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        clickAlert.start();
                        dialogInterface.dismiss();
                    }
                }).show();
            }
        });

        btn_QR_codes =findViewById(R.id.btn_QR_codes);
        btn_QR_codes.setOnClickListener(v->
        {
            v.startAnimation(s);
            clickAlert.start();
            if (getConnectionType(getApplicationContext())!=0)
            {
                Intent intent = new Intent(MainActivity.this, QRCodesActivity.class);
                startActivity(intent);
            }
            else
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Problem z siecią");
                builder.setMessage("Brak łączności z internetem");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        clickAlert.start();
                        dialogInterface.dismiss();
                    }
                }).show();
            }
        });

        if (getConnectionType(getApplicationContext())==0)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Problem z siecią");
            builder.setMessage("Brak łączności z internetem");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialogInterface, int i)
                {
                    clickAlert.start();
                    dialogInterface.dismiss();
                }
            }).show();
        }

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        scaleUp = AnimationUtils.loadAnimation(this, R.anim.scale_up);
        scaleDown = AnimationUtils.loadAnimation(this, R.anim.scale_down);
        clickAlert = MediaPlayer.create(MainActivity.this, R.raw.click);
        s = new AnimationSet(false);//false means don't share interpolators
        s.addAnimation(scaleDown);
        s.addAnimation(scaleUp);




    }

    private void scanCode()
    {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Przycisk podgłośnienia uruchamia lampę błyskową.");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLaucher.launch(options);
    }

    ActivityResultLauncher<ScanOptions> barLaucher = registerForActivityResult(new ScanContract(), result->
    {

        if(result.getContents() !=null)
        {

            productId = result.getContents();
            if (scanOperation==1)
            {
                InitObjects();
                firebaseDatabase.getReference("Products").child(productId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int size = (int) snapshot.getChildrenCount();

                        if (size!=0)
                        {
                            Product product = snapshot.getValue(Product.class);
                            assert product != null;
                            scanName = product.getName();

                            loadingDialog.dismiss();
                            savingToMAgazine();
                        }
                        else
                        {
                            loadingDialog.dismiss();
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle("Wynik skanowania");
                            builder.setCancelable(false);
                            builder.setMessage("Brak produktu w bazie danych. \nKod: " +productId);
                            builder.setPositiveButton("Dodaj kod kreskowy", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i)
                                {
                                    AddingQRDialongNew();
                                    clickAlert.start();
                                    dialogInterface.dismiss();
                                }
                            });
                            builder.setNegativeButton("Anuluj", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    clickAlert.start();
                                    dialogInterface.dismiss();
                                    Toast.makeText(getApplicationContext(), "Anulowano", Toast.LENGTH_SHORT).show();
                                }
                            });
                            builder.show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }
            else if (scanOperation ==2)
            {
                InitObjects();
                firebaseDatabase.getReference("Products").child(productId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int size = (int) snapshot.getChildrenCount();

                        if (size!=0)
                        {
                            Product product = snapshot.getValue(Product.class);
                            assert product != null;
                            scanName = product.getName();

                            loadingDialog.dismiss();
                            UpdateQRDialog();

                        }
                        else
                        {
                            loadingDialog.dismiss();
                            AddingQRDialongNew();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }











        }
    });


    @SuppressLint("SetTextI18n")
    private void savingToMAgazine() {
        AlertDialog.Builder alert = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AppTheme));
        LayoutInflater inflater = getLayoutInflater();
        alert.setTitle("Dodawanie");
        alert.setCancelable(false);
        View view = inflater.inflate(R.layout.alert_scaning, findViewById(R.id.notification_main_column));
        editTextDate = view.findViewById(R.id.btn_date1);
        Button buttonAdd = view.findViewById(R.id.btn_done);
        Button buttonCancel = view.findViewById(R.id.btn_reset);
        TextView textView = view.findViewById(R.id.editext_message);
        textView.setText("Nazwa: " + scanName + "\n" +"Kod: " + productId);
        editTextDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String current = String.valueOf(charSequence);
                if ((i==1&&charSequence.length()==2) || (i==4&&charSequence.length()==5))
                {
                    current = current+"-";
                    editTextDate.setText(current);
                    editTextDate.setSelection(current.length());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });






      buttonAdd.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              clickAlert.start();

              if (String.valueOf(editTextDate.getText()).length()==8 && StringUtils.countMatches(String.valueOf(editTextDate.getText()), "-") == 2 )
              {
                  InitObjects();
                  generateMagazineId();

                  String fullDate = String.valueOf(editTextDate.getText());
                  String  day = StringUtils.substringBefore(fullDate, "-");
                  String noDay = StringUtils.substringAfter(fullDate, "-");
                  String mouth = StringUtils.substringBefore(noDay, "-");
                  String year = "20"+StringUtils.substringAfterLast(fullDate, "-");

                  date = year+"-"+mouth+"-"+day;


                  Magazine addingMagazine = new Magazine(magazineId, productId, scanName, date);
                  firebaseDatabase.getReference("Magazine").child(magazineId).setValue(addingMagazine).addOnSuccessListener(new OnSuccessListener<Void>() {
                      @Override
                      public void onSuccess(Void unused) {


                          Toast.makeText(getApplicationContext(), "Pomyślnie dodano do magazynu", Toast.LENGTH_SHORT).show();
                          loadingDialog.dismiss();
                          date="";
                          editTextAdd.setText("");
                      }
                  });
                  dialog.dismiss();
              }
              else
              {
                  Toast.makeText(getApplicationContext(), "Musisz wybrać datę", Toast.LENGTH_LONG).show();
              }
          }
      });

      buttonCancel.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {
              clickAlert.start();
              dialog.dismiss();
              date="";
              Toast.makeText(getApplicationContext(), "Anulowano", Toast.LENGTH_SHORT).show();
              editTextAdd.setText("");
          }
      });

        alert.setView(view);
        dialog = alert.create();
        dialog.getWindow().setBackgroundDrawableResource(R.color.background_color);
        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void generateMagazineId() {
        Date date = new Date();
        String currentDate = String.valueOf(date.getTime());
        int random_int1 = (int)Math.floor(Math.random() * (99 + 1) + 0);
        String one;
        String two;
        String three;
        if (random_int1 < 10)
        {
            one = "0"+random_int1;
        }
        else
        {
            one = String.valueOf(random_int1);

        }

        int random_int2 = (int)Math.floor(Math.random() * (99 + 1) + 0);
        if (random_int2 < 10)
        {
            two = "0"+random_int2;
        }
        else
        {
            two = String.valueOf(random_int2);

        }
        int random_int3 = (int)Math.floor(Math.random() * (99 + 1) + 0);
        if (random_int3 < 10)
        {
            three = "0"+random_int3;
        }
        else
        {
            three = String.valueOf(random_int3);

        }
        magazineId = currentDate + "-" + one + "-"+   two + "-" + three;
    }

    private void UpdateQRDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Wynik skanowania");
        builder.setMessage("Nazwa: " + scanName + "\n" +"Kod: " + productId  +"\n\nProdukt znajduje się już w bazie, możesz nadpisać jego nazwę wprowadzając nową.");
        EditText editText = new EditText(getApplicationContext());
        editText.setText(scanName);
        builder.setCancelable(false);
        builder.setView(editText);
        builder.setPositiveButton("Nadpisz", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                clickAlert.start();
                if (editText.length()!=0)
                {
                    InitObjects();
                    scanName = editText.getText().toString();
                    Product addingQR = new Product(productId, scanName);
                    firebaseDatabase.getReference("Products").child(productId).setValue(addingQR).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(getApplicationContext(), "Pomyślnie nadpisano nazwę", Toast.LENGTH_SHORT).show();
                            loadingDialog.dismiss();
                        }
                    });

                    dialogInterface.dismiss();
                }
                else
                {
                    UpdateQRDialog();
                    Toast.makeText(getApplicationContext(), "Nazwa produktu nie może być pusta", Toast.LENGTH_LONG).show();
                }
            }
        });
        builder.setNegativeButton("Anuluj", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                clickAlert.start();
                dialogInterface.dismiss();
                Toast.makeText(getApplicationContext(), "Anulowano", Toast.LENGTH_SHORT).show();

            }
        });
        builder.show();
    }
    private void AddingQRDialongNew()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        EditText editText = new EditText(getApplicationContext());
        editText.setHint("Wprowadź nazwę produktu");
        builder.setView(editText);
        builder.setTitle("Wynik skanowania");
        builder.setCancelable(false);
        builder.setMessage("Brak produktu w bazie danych. \nKod: " +productId);
        builder.setPositiveButton("Dodaj", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                clickAlert.start();

                if (editText.length()!=0)
                {
                    InitObjects();
                    scanName = editText.getText().toString();
                    Product addingQR = new Product(productId, scanName);
                    firebaseDatabase.getReference("Products").child(productId).setValue(addingQR).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(getApplicationContext(), "Pomyślnie dodano kod produktu", Toast.LENGTH_SHORT).show();
                            loadingDialog.dismiss();
                        }
                    });

                    dialogInterface.dismiss();
                    editTextAdd.setText("");

                }
                else
                {
                    AddingQRDialongNew();
                    Toast.makeText(getApplicationContext(), "Nazwa produktu nie może być pusta", Toast.LENGTH_LONG).show();
                }

            }
        });
        builder.setNegativeButton("Anuluj", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                clickAlert.start();
                dialogInterface.dismiss();
                editTextAdd.setText("");
                Toast.makeText(getApplicationContext(), "Anulowano", Toast.LENGTH_SHORT).show();

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
        loadingDialog.setButton(Dialog.BUTTON_POSITIVE, "Odśwież aplikacje", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                recreate();
                Toast.makeText(getApplicationContext(), "Odswieżono", Toast.LENGTH_SHORT).show();
            }
        });
        loadingDialog.setMessage("Trwa łączenie z bazą, jeśli trwa zbyt długo, upewnij się, że masz łączność internetem.");
        loadingDialog.show();
    }


}