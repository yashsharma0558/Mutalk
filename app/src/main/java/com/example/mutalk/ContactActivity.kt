package com.example.mutalk

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import im.zego.zegoexpress.ZegoExpressEngine
import im.zego.zegoexpress.constants.ZegoScenario
import im.zego.zegoexpress.entity.ZegoEngineProfile

class ContactActivity : AppCompatActivity() {

    private val REQUEST_READ_CONTACTS = 101
    companion object {
        const val PICK_CONTACT_REQUEST = 1
    }

    private val MY_CAMERA_REQUEST_CODE = 100
    private lateinit var textView: TextView
    private lateinit var addContact: Button
    private lateinit var joinRoom: Button
    private lateinit var roomField: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var extraID: String
    private lateinit var extraName: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact)
        sharedViewModel= ViewModelProvider(this)[SharedViewModel::class.java]
        recyclerView = findViewById(R.id.recyclerView)
        textView = findViewById(R.id.welcome)
        addContact = findViewById(R.id.button)
        joinRoom = findViewById(R.id.button5)
        roomField = findViewById(R.id.room)
        extraID = intent.getStringExtra("userID").toString()
        extraName = intent.getStringExtra("userName").toString()


        if(sharedViewModel.getAllContacts().isEmpty()){
            textView.visibility = View.VISIBLE
            addContact.visibility = View.VISIBLE
            recyclerView.visibility = View.INVISIBLE
        }
        else{
            displayContacts(sharedViewModel.getAllContacts())
        }

        addContact.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.READ_CONTACTS),
                    REQUEST_READ_CONTACTS)
            } else {
                // Permission has already been granted, proceed to read contacts
                val pickContactIntent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
                startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST)
            }
        }

        joinRoom.setOnClickListener {
            val roomID = roomField.text.toString()
            createEngine()
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), MY_CAMERA_REQUEST_CODE)
            val intent = Intent(this, CallPageActivity::class.java)
            intent.putExtra("userID", extraID)
            intent.putExtra("userName", extraName)
            intent.putExtra("roomID", roomID)
            startActivity(intent)
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        destroyEngine()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_CONTACT_REQUEST && resultCode == Activity.RESULT_OK) {
            // Get the contact URI
            val contactUri = data?.data

            contactUri?.let {
                // Query the contact details
                val cursor = contentResolver.query(it, null, null, null, null)
                cursor?.let { c ->
                    if (c.moveToFirst()) {
                        val addUser: Contact = Contact("", "", "")
                        // Retrieve contact name
                        val contactNameIndex = c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                        addUser.name = if (contactNameIndex != -1) {
                            c.getString(contactNameIndex)
                        } else {
                            // Handle case where DISPLAY_NAME column doesn't exist
                            ""
                        }

                        // Retrieve contact phone number
                        val idIndex = c.getColumnIndex(ContactsContract.Contacts._ID)
                        val contactId = if (idIndex != -1) {
                            c.getString(idIndex)
                        } else {
                            // Handle case where _ID column is not found
                            null // or provide a default value
                        }

                        val phoneCursor = contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            arrayOf(contactId),
                            null
                        )
                        var phoneNumber: String? = null
                        phoneCursor?.use { pc ->
                            if (pc.moveToFirst()) {
                                val phoneNumberIndex = pc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                phoneNumber = if (phoneNumberIndex != -1) {
                                    pc.getString(phoneNumberIndex)
                                } else {
                                    // Handle case where PHONE column doesn't exist
                                    null
                                }
                            }
                        }
                        addUser.phoneNumber = phoneNumber.toString()

                        // Retrieve contact profile photo
                        val photoUriIndex = c.getColumnIndex(ContactsContract.Contacts.PHOTO_URI)
                        val photoUri = if (photoUriIndex != -1) {
                            c.getString(photoUriIndex)
                        } else {
                            // Handle case where PHOTO_URI column doesn't exist
                            null
                        }
                        addUser.profilePicture = photoUri.toString()
                        // Log or use the retrieved data
                        Log.d("ContactInfo", "Name: ${addUser.name}, Phone: $phoneNumber, Photo URI: $photoUri")
                        sharedViewModel.addContact(addUser)
                        recreate()
                        // Close the cursors
                        phoneCursor?.close()
                    }
                    c.close()
                }
            }
        } else {
            // Handle case where user didn't select a contact
            Toast.makeText(this, "No contact selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayContacts(contactsList: List<Contact>) {

        val adapter = ContactsAdapter(contactsList, extraID, extraName)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun createEngine() {
        val profile = ZegoEngineProfile()

        // Get your AppID and AppSign from ZEGOCLOUD Console
        //[My Projects -> AppID] : https://console.zegocloud.com/project
        profile.appID = BuildConfig.ZEGO_APP_ID.toLong()
        profile.appSign = BuildConfig.ZEGO_APP_SIGN
        profile.scenario = ZegoScenario.DEFAULT // General scenario.
        profile.application = application
        ZegoExpressEngine.createEngine(profile, null)
    }


    // destroy engine
    private fun destroyEngine() {
        ZegoExpressEngine.destroyEngine(null)
    }


}