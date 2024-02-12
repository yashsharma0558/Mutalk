package com.example.mutalk

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions

class ContactsAdapter(private val contactsList: List<Contact>, private val extraID: String, private val extraName: String) :
    RecyclerView.Adapter<ContactsAdapter.ContactViewHolder>() {

    inner class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        val phoneNumberTextView: TextView = itemView.findViewById(R.id.phoneNumberTextView)
        val profilePictureImageView: ImageView = itemView.findViewById(R.id.profileImageView)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val contact = contactsList[position]
                    val intent = Intent(itemView.context, ContactProfileActivity::class.java).apply {
                        putExtra("contact_name", contact.name)
                        putExtra("contact_phone", contact.phoneNumber)
                        putExtra("contact_photo", contact.profilePicture)
                        putExtra("userID", extraID)
                        putExtra("userName", extraName)
                    }
                    itemView.context.startActivity(intent)
                }
            }


        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.contact_card_view, parent, false)
        return ContactViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contactsList[position]
        holder.nameTextView.text = contact.name
        holder.phoneNumberTextView.text = contact.phoneNumber
        if(contact.profilePicture!=""){
            Glide.with(holder.profilePictureImageView.context)
                .load(Uri.parse(contact.profilePicture))
                .apply(RequestOptions().transforms(CircleCrop()))
                .into(holder.profilePictureImageView)
        }


    }

    override fun getItemCount(): Int {
        return contactsList.size
    }
}

