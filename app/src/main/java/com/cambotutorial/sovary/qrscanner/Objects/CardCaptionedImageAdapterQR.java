package com.cambotutorial.sovary.qrscanner.Objects;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.cambotutorial.sovary.qrscanner.R;

import java.util.ArrayList;
import java.util.List;

public class CardCaptionedImageAdapterQR extends RecyclerView.Adapter<CardCaptionedImageAdapterQR.CardCaptionedImageAdapterQRViewHolder> implements Filterable {

    public List<Product> exampleList;
    public List<Product> exampleListFull;
    public Listener listener;
    Animation scaleUp, scaleDown;
    MediaPlayer clickAlert;
    public interface Listener
    {
        void onClick(int position);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public class CardCaptionedImageAdapterQRViewHolder extends RecyclerView.ViewHolder {

        TextView id;
        TextView name;


        CardCaptionedImageAdapterQRViewHolder(View itemView) {
            super(itemView);
            id = itemView.findViewById(R.id.product_id);
            name = itemView.findViewById(R.id.product_name);

        }
    }
    public CardCaptionedImageAdapterQR(List<Product> exampleList) {
        this.exampleList = exampleList;
        exampleListFull = new ArrayList<>(exampleList);
    }

    @NonNull
    @Override
    public CardCaptionedImageAdapterQRViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_captioned_image,
                parent, false);
        return new CardCaptionedImageAdapterQRViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CardCaptionedImageAdapterQRViewHolder holder, int position) {
        Product product = exampleList.get(position);

        clickAlert = MediaPlayer.create(holder.itemView.getContext(), R.raw.click);
        scaleUp = AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.scale_up);
        scaleDown = AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.scale_down);
        holder.id.setText(product.getId());
       holder.name.setText(product.getName());



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
            List<Product> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(exampleListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Product item : exampleListFull) {
                    if (item.getName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            exampleList.clear();
            exampleList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };
}
