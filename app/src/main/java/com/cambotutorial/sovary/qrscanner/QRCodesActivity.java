package com.cambotutorial.sovary.qrscanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.cambotutorial.sovary.qrscanner.Objects.CardCaptionedImageAdapterQR;
import com.cambotutorial.sovary.qrscanner.Objects.Product;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;

public class QRCodesActivity extends AppCompatActivity implements ValueEventListener {

    private SearchView searchView;
    private ProgressBar progress;
    private String id;
    private String name;
    private List<Product> productList;
    private RecyclerView recyclerView;
    private FirebaseDatabase firebaseDatabase;
    private TextView loadingText;
    private TextView emptyList;
    private Toolbar toolbar;

    AlertDialog  loadingDialog;
    MediaPlayer clickAlert;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcodes);


        productList = new ArrayList<>();

        progress = findViewById(R.id.progress_QR);
        progress.setMax(1000);
        progress.setVisibility(View.VISIBLE);
        loadingText = findViewById(R.id.loading_text);
        loadingText.setVisibility(View.VISIBLE);
        emptyList = findViewById(R.id.emptyTv);
        ObjectAnimator.ofInt(progress, "progress", 1000).setDuration(2000).start();

        firebaseDatabase = FirebaseDatabase.getInstance("https://magazineapp-3bfa5-default-rtdb.europe-west1.firebasedatabase.app/");
        recyclerView = findViewById(R.id.rv_QRCodes);
        searchView = findViewById(R.id.searchView);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                QRCodesActivity.super.onBackPressed();
                clickAlert.start();
            }
        });
        clickAlert = MediaPlayer.create(QRCodesActivity.this, R.raw.click);



    }

    @Override
    protected void onPause() {
        super.onPause();
        firebaseDatabase.getReference("Products").orderByChild("name").removeEventListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        firebaseDatabase.getReference("Products").orderByChild("name").addValueEventListener(this);

    }

    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {
        int size = (int) snapshot.getChildrenCount();
        productList.clear();
        if (size!=0)
        {



            for (DataSnapshot postSnapshot : snapshot.getChildren()){
                Product product = postSnapshot.getValue(Product.class);
                assert product != null;
                productList.add(new Product(product.getId(), product.getName()));
            }

            progress.setVisibility(View.GONE);
            loadingText.setVisibility(View.GONE);
            emptyList.setVisibility(View.GONE);

            CardCaptionedImageAdapterQR adapter = new CardCaptionedImageAdapterQR(productList);
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
            adapter.setListener(new CardCaptionedImageAdapterQR.Listener() {
                @Override
                public void onClick(int position) {
                   Product clickedProduct = new Product();
                    clickedProduct = productList.get(position);
                    id = clickedProduct.getId();
                    name = clickedProduct.getName();
                    setAlertDialog(id, name);
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

    private void setAlertDialog(String IdText, String NameText)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(QRCodesActivity.this);
        builder.setTitle("Usuwanie pozycji");
        builder.setMessage("Czy na pewno chcesz usunąć z bazy kod kreskowy dla produktu: \n\nNazwa: "+ NameText +"\nKod:" + IdText);
        builder.setPositiveButton("Usuń", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                clickAlert.start();
                if (getConnectionType(getApplicationContext())!=0)
                {
                    InitObjects();
                    firebaseDatabase.getReference("Products").child(IdText).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
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
                    setAlertDialog(IdText, NameText);
                }
                dialogInterface.dismiss();
            }
        });
        builder.setNegativeButton("Anuluj", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                clickAlert.start();
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