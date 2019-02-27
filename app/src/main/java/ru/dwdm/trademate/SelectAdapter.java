package ru.dwdm.trademate;

import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class SelectAdapter extends RecyclerView.Adapter<SelectAdapter.ViewHolder> {

    private ArrayList<Model> items;
    private SelectCallback callback;

    public SelectAdapter(ArrayList<Model> items, SelectCallback callback) {
        this.callback = callback;
        this.items = items;
    }

    @NonNull
    @Override
    public SelectAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.select_item, null);
        return new SelectAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.bind(items.get(i));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        private final CardView cardView;
        private final ImageView image;
        private final TextView description;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.select_model_card_view);
            image = itemView.findViewById(R.id.select_model_image);
            description = itemView.findViewById(R.id.select_model_description);
        }

        void bind(Model model) {
            cardView.setOnClickListener(v -> callback.onItemSelected(model));
            image.setImageBitmap(BitmapFactory.decodeResource(image.getResources(), model.getIdRes()));
            description.setText(model.getDescription());
        }

    }

    public interface SelectCallback {
        void onItemSelected(Model model);
    }
}
