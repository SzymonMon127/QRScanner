package com.cambotutorial.sovary.qrscanner.Objects;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cambotutorial.sovary.qrscanner.R;
import com.google.android.material.snackbar.Snackbar;

import org.apache.commons.lang3.StringUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CardCaptionedImageMagazine extends RecyclerView.Adapter<CardCaptionedImageMagazine.CardCaptionedImageMagazineViewHolder> implements Filterable {

    public List<Magazine> exampleList;
    public List<Magazine> exampleListFull;
    public Listener listener;
    Animation scaleUp, scaleDown;
    MediaPlayer clickAlert;
    public int filterInt;
    private Context context;
    private Date date1;
    private Date date2;
    private Date date3;
    Calendar cal1 = Calendar.getInstance();
    Calendar cal2 = Calendar.getInstance();
    Calendar cal3 = Calendar.getInstance();
    String dateMin;
    String dateMax;
    public interface Listener
    {
        void onClick(int position);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public class CardCaptionedImageMagazineViewHolder extends RecyclerView.ViewHolder {

        TextView id;
        TextView name;
        TextView date;


        CardCaptionedImageMagazineViewHolder(View itemView) {
            super(itemView);
            id = itemView.findViewById(R.id.product_id);
            name = itemView.findViewById(R.id.product_name);
            date = itemView.findViewById(R.id.date_text);

        }
    }
    public CardCaptionedImageMagazine(List<Magazine> exampleList) {
        this.exampleList = exampleList;
        exampleListFull = new ArrayList<>(exampleList);
    }

    @NonNull
    @Override
    public CardCaptionedImageMagazineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_captioned_image_magazine,
                parent, false);
        context = parent.getContext();
        return new CardCaptionedImageMagazineViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CardCaptionedImageMagazineViewHolder holder, int position) {
        Magazine magazine = exampleList.get(position);

        clickAlert = MediaPlayer.create(holder.itemView.getContext(), R.raw.click);
        scaleUp = AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.scale_up);
        scaleDown = AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.scale_down);
        holder.id.setText(magazine.getQR());
       holder.name.setText(magazine.getName());
       holder.date.setText(magazine.getDate());



        holder.itemView.setOnClickListener(v -> {
            if (listener != null)
            {
                listener.onClick(position);

                AnimationSet s = new AnimationSet(false);//false means don't share interpolators
                s.addAnimation(scaleDown);
                s.addAnimation(scaleUp);
                v.startAnimation(s);
            }
        });



    }

    @Override
    public int getItemCount() {
        return exampleList.size();
    }

    @Override
    public Filter getFilter() {
        return exampleFilter;
    }


    private Filter exampleFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Magazine> filteredList = new ArrayList<>();
            filterInt = PrefConfig.loadTotalFromPref(context);
            if (filterInt==1)
            {
                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(exampleListFull);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();

                    for (Magazine item : exampleListFull) {
                        if (item.getName().toLowerCase().contains(filterPattern)) {
                            filteredList.add(item);
                        }
                    }
                }


            }
            else if (filterInt ==2)
            {

                DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");
                date1 = null;
                date2 = null;
                date3 = null;
                dateMin = "";
                dateMax ="";



                try {
                    String contraintString = String.valueOf(constraint);
                    dateMin = StringUtils.substringBefore(contraintString, "&&");
                    dateMax = StringUtils.substringAfter(contraintString, "&&");

                    date1 = df1 .parse(dateMin);
                    date2 = df1 .parse(dateMax);
                    cal1.setTime(date1);
                    cal2.setTime(date2);


                } catch (ParseException e) {
                    Log.w("TAG", "Filtering issue", e);
                }


                if (constraint == null || constraint.length() == 0 || date1 == null || date2 == null) {
                    filteredList.addAll(exampleListFull);
                    if ((constraint != null && constraint.length() != 0) && (date1 == null || date2 == null))
                    {
                        Toast.makeText(context, "Problem z wyszukiwaniem", Toast.LENGTH_SHORT).show();

                    }
                } else {
                    for (Magazine item : exampleListFull) {
                            try {
                                date3 = df1 .parse(item.getDate());
                                cal3.setTime(date3);
                                if (cal3.after(cal1) && cal3.before(cal2) || cal3.equals(cal1) || cal3.equals(cal2)) {
                                    filteredList.add(item);
                                }
                            } catch (ParseException e) {
                                filteredList.addAll(exampleListFull);
                                Toast.makeText(context, "Problem z wyszukiwaniem", Toast.LENGTH_SHORT).show();
                            }

                        }
                }





            }


            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {

            if (filterInt ==1)
            {
                exampleList.clear();
                exampleList.addAll((List) results.values);
                notifyDataSetChanged();
            }
            else if (filterInt ==2)
            {
                exampleList.clear();
                exampleList.addAll((List) results.values);
                notifyDataSetChanged();
            }
        }
    };
}
