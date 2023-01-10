package com.example.localization

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdapterBid(var listBid : ArrayList<Bid>) : RecyclerView.Adapter<AdapterBid.ViewHolderBid>() {

//    var listBid : ArrayList<Bid> = ArrayList<Bid>()

    class ViewHolderBid(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.idBid)

        @SuppressLint("SetTextI18n")
        fun setData(bid: Bid) {
            textView.text = bid.userName + " " + bid.price
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterBid.ViewHolderBid {
        val view : View = LayoutInflater.from(parent.context).inflate(R.layout.item_bid,null,false)

        return AdapterBid.ViewHolderBid(view)
    }

    override fun onBindViewHolder(holder: AdapterBid.ViewHolderBid, position: Int) {
        holder.setData(listBid[position])
    }

    override fun getItemCount(): Int {
        return listBid.size
    }
}