package com.hagai.gettproject;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hagai.gettproject.comm.PlacesResult;

import java.util.List;


/**
 * Created by hagai on 8/13/2017.
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {


    private List<PlacesResult.Results> stLstStores;
    private List<MarkerModel> models;


    public RecyclerViewAdapter(List<PlacesResult.Results> stores, List<MarkerModel> markerModels) {

        stLstStores = stores;
        models = markerModels;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.marker_list_row, parent, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        holder.setData(stLstStores.get(holder.getAdapterPosition()), holder, models.get(holder.getAdapterPosition()));
    }


    @Override
    public int getItemCount() {
        return stLstStores.size();
    }

    public void clearData() {
        models.clear();
        stLstStores.clear();
        notifyDataSetChanged();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {


        TextView txtStoreName;
        TextView txtStoreAddr;
        MarkerModel model;


        public MyViewHolder(View itemView) {
            super(itemView);
            this.txtStoreName = itemView.findViewById(R.id.textMarkerName);
            this.txtStoreAddr = itemView.findViewById(R.id.textMarkerAddr);
        }


        public void setData(PlacesResult.Results info, MyViewHolder holder, MarkerModel storeModel) {
            this.model = storeModel;
            holder.txtStoreName.setText(info.name);
            holder.txtStoreAddr.setText(info.vicinity);


        }

    }
}
