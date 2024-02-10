package com.example.mutalk

import android.content.ContentValues.TAG
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ContactActivity : AppCompatActivity() {

    private val REQUEST_READ_CONTACTS = 101
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact)

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.READ_CONTACTS),
                REQUEST_READ_CONTACTS)
        } else {
            // Permission has already been granted, proceed to read contacts
            readContacts()
        }



    }

    private fun readContacts() {
        val contactsList = mutableListOf<Contact>()

        val cursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            null,
            null,
            null,
            null
        )

        cursor?.use { c ->
            val idIndex = c.getColumnIndex(ContactsContract.Contacts._ID)
            val nameIndex = c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
            val hasPhoneNumberIndex = c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)

            while (c.moveToNext()) {
                val id = c.getString(idIndex)
                val name = c.getString(nameIndex)
                val hasPhoneNumber = c.getInt(hasPhoneNumberIndex)

                if (hasPhoneNumber > 0) {
                    val phoneCursor = contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(id),
                        null
                    )

                    phoneCursor?.use { pc ->
                        val phoneNumberIndex = pc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                        // Ensure the phone number column index is valid
                        if (phoneNumberIndex != -1) {
                            while (pc.moveToNext()) {
                                val phoneNumber = pc.getString(phoneNumberIndex)
                                contactsList.add(Contact(name, phoneNumber))
                            }
                        } else {
                            // Handle the case where the phone number column is not found
                            Log.e(TAG, "Phone number column not found in phoneCursor")
                        }
                    }

                }
            }
        }

        // Now you have the list of contacts in contactsList, proceed to display them in RecyclerView
        displayContacts(contactsList)
    }

    private fun displayContacts(contactsList: List<Contact>) {
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        val adapter = ContactsAdapter(contactsList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }



}