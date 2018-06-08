package com.zpi.komunikator;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

class MyAdapter extends RecyclerView.Adapter {

    private final List<MainActivity.MyMessage> messages;

    public MyAdapter(List<MainActivity.MyMessage> messages){
        this.messages = messages;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v =  LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_row, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((MyViewHolder) holder).tvMessage.setText(messages.get(position).message);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView tvMessage;

        public MyViewHolder(View v) {
            super(v);
            tvMessage = v.findViewById(R.id.row_message);
        }
    }
}
