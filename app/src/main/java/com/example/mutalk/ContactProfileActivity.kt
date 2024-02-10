package com.example.mutalk

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import im.zego.zegoexpress.ZegoExpressEngine
import im.zego.zegoexpress.constants.ZegoScenario
import im.zego.zegoexpress.entity.ZegoEngineProfile
import java.util.Random

class ContactProfileActivity : AppCompatActivity() {

    private val MY_CAMERA_REQUEST_CODE = 100
    private lateinit var makeVideoCall: Button
    private lateinit var shareCodeWhatsApp: Button
    private lateinit var shareCodeText: Button
    private lateinit var roomCodeTextView: TextView
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var extraID: String
    private lateinit var extraName: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createEngine()
        val roomID = generateID(10)
        setContentView(R.layout.activity_contact_profile)
        sharedViewModel= ViewModelProvider(this)[SharedViewModel::class.java]

        val contactName = intent.getStringExtra("contact_name")
        val contactPhone = intent.getStringExtra("contact_phone")
        extraID = intent.getStringExtra("userID").toString()
        extraName = intent.getStringExtra("userName").toString()

        findViewById<TextView>(R.id.textView2).text = contactName
        findViewById<TextView>(R.id.textView3).text = contactPhone

        makeVideoCall = findViewById(R.id.button2)
        shareCodeText = findViewById(R.id.button4)
        shareCodeWhatsApp = findViewById(R.id.button3)
        roomCodeTextView = findViewById(R.id.textView4)
        roomCodeTextView.text = roomID

        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), MY_CAMERA_REQUEST_CODE)

        makeVideoCall.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED){
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), MY_CAMERA_REQUEST_CODE)
            }
            else{
                intent = Intent(this, CallPageActivity::class.java)
                intent.putExtra("userID", extraID)
                intent.putExtra("userName", extraName)
                intent.putExtra("roomID", roomID)
                startActivity(intent)
            }

        }
        shareCodeWhatsApp.setOnClickListener {
            shareTextToWhatsApp(contactPhone.toString(), roomID)
        }
        shareCodeText.setOnClickListener {
            shareCodeToText(contactPhone.toString(), roomID)
        }



    }




    override fun onDestroy() {
        super.onDestroy()
        destroyEngine()
    }
    private fun generateID(limit: Int): String {
        val builder = StringBuilder()
        val random = Random()
        while (builder.length <= limit) {
            val nextInt: Int = random.nextInt(10)
            if (builder.isEmpty() && nextInt == 0) {
                continue
            }
            builder.append(nextInt)
        }
        return builder.toString()
    }

    private fun shareCodeToText(phoneNumber: String, roomID: String) {
        val uri = Uri.parse("smsto:$phoneNumber")
        val intent = Intent(Intent.ACTION_SENDTO, uri)
        intent.putExtra("sms_body", roomID)
        this.startActivity(intent)
    }

    private fun shareTextToWhatsApp(contactNumber: String, message: String) {
        // Format the phone number to remove special characters
        val formattedNumber = contactNumber.replace("[^\\d]".toRegex(), "")

        // Create the intent with the appropriate action and data
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.setPackage("com.whatsapp")
        intent.putExtra(Intent.EXTRA_TEXT, message)
        intent.putExtra("jid", "$formattedNumber@s.whatsapp.net") // This is the key line

        try {
            // Start the WhatsApp application
            startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
            // Handle cases where WhatsApp is not installed on the device
            Toast.makeText(this, "WhatsApp is not installed on this device", Toast.LENGTH_SHORT).show()
        }
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