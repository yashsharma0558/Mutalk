package com.example.mutalk

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.zegocloud.uikit.prebuilt.call.invite.widget.ZegoSendCallInvitationButton
import com.zegocloud.uikit.service.defines.ZegoUIKitUser

class VideoCallActivity : AppCompatActivity()  {

    private lateinit var receiverUserId: EditText
    private lateinit var textView: TextView
    private lateinit var videoCallBtn: ZegoSendCallInvitationButton
    private lateinit var audioCallBtn: ZegoSendCallInvitationButton
    private lateinit var buttonLayout: LinearLayout
    private lateinit var startCall: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_call)

        receiverUserId = findViewById(R.id.receiver_user_id_text_field)
        textView = findViewById(R.id.user_id_text_view)
        videoCallBtn = findViewById(R.id.video_call_btn)
        audioCallBtn = findViewById(R.id.audio_call_btn)
        buttonLayout = findViewById(R.id.buttons_layout)
        startCall = findViewById(R.id.startCallButton)

        val userId = intent.getStringExtra("userID")
        textView.text = "Hi $userId!"

        startCall.setOnClickListener {
            val _receiverId = receiverUserId.text.toString()
            if (_receiverId.isNotEmpty()) {

                buttonLayout.visibility = View.VISIBLE
                startVideoCall(_receiverId)
                startAudioCall(_receiverId)

            }
        }

    }

    private fun startVideoCall(receiverId: String) {
        videoCallBtn.setIsVideoCall(true)
        videoCallBtn.resourceID = "zego_uikit_call"
        videoCallBtn.setInvitees(listOf(ZegoUIKitUser(receiverId)))
    }

    private fun startAudioCall(receiverId: String) {
        audioCallBtn.setIsVideoCall(false)
        audioCallBtn.resourceID = "zego_uikit_call"
        audioCallBtn.setInvitees(listOf(ZegoUIKitUser(receiverId)))
    }
}