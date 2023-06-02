package com.example.meteotablet2.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.meteotablet2.R;
import com.example.meteotablet2.entities.SearchedData;

import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private List<SearchedData> list;
    private final LayoutInflater inflater;

    private final RecyclerInterface recyclerInterface;

    public RecyclerAdapter(Context context, List<SearchedData> list,
                           RecyclerInterface recyclerInterface) {
        this.list = list;
        this.inflater = LayoutInflater.from(context);
        this.recyclerInterface = recyclerInterface;
    }

    @NonNull
    @Override
    public RecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.search_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SearchedData searchedData = list.get(position);
        holder.date.setText(searchedData.date);
        holder.city.setText(searchedData.city);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public void clearData(){
        list.clear();
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        final TextView date;
        final TextView city;

        public ViewHolder(@NonNull View itemView){
            super(itemView);
            date = itemView.findViewById(R.id.searchDate);
            city = itemView.findViewById(R.id.searchCity);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(recyclerInterface != null){
                        int position = getAdapterPosition();

                        if(position != RecyclerView.NO_POSITION){
                            recyclerInterface.onItemClick(position);
                        }
                    }
                }
            });
        }
    }

}
