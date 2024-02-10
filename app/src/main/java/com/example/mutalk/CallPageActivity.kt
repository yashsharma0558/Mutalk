package com.example.mutalk

import android.os.Bundle
import android.view.TextureView
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import im.zego.zegoexpress.ZegoExpressEngine
import im.zego.zegoexpress.callback.IZegoEventHandler
import im.zego.zegoexpress.constants.ZegoPlayerState
import im.zego.zegoexpress.constants.ZegoPublisherState
import im.zego.zegoexpress.constants.ZegoRoomStateChangedReason
import im.zego.zegoexpress.constants.ZegoScenario
import im.zego.zegoexpress.constants.ZegoUpdateType
import im.zego.zegoexpress.entity.ZegoCanvas
import im.zego.zegoexpress.entity.ZegoEngineProfile
import im.zego.zegoexpress.entity.ZegoRoomConfig
import im.zego.zegoexpress.entity.ZegoStream
import im.zego.zegoexpress.entity.ZegoUser
import org.json.JSONObject


class CallPageActivity : AppCompatActivity() {
    private var userID: String? = null
    private var userName: String? = null
    private lateinit var preview: TextureView
    private lateinit var remoteUserView: TextureView
    private lateinit var callEndButton: FloatingActionButton


    // The value of `roomID` is generated locally and must be globally unique.
    // Users must log in to the same room to call each other.
    private var roomID: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call_page)
        preview = findViewById(R.id.preview)
        remoteUserView = findViewById(R.id.remoteUserView)
        callEndButton = findViewById(R.id.floatingActionButton3)
        userID = intent.getStringExtra("userID")
        userName = intent.getStringExtra("userName")
        roomID = intent.getStringExtra("roomID")
        startListenEvent()
        loginRoom()
        // Set a listener for the call stopping button.
        callEndButton.setOnClickListener { finish() }
    }

    override fun onStop() {
        super.onStop()
        if (isFinishing) {
            stopListenEvent()
            stopPreview()
            logoutRoom()
        }
    }


    private fun startPreview(view :TextureView) {
        val previewCanvas = ZegoCanvas(view)
        ZegoExpressEngine.getEngine().startPreview(previewCanvas)
    }

    private fun stopPreview() {
        ZegoExpressEngine.getEngine().stopPreview()
    }

    // Log in to a room.
    private fun loginRoom() {
        val user = ZegoUser(userID, userName)
        val roomConfig = ZegoRoomConfig()
        // The `onRoomUserUpdate` callback can be received only when
        // `ZegoRoomConfig` in which the `isUserStatusNotify` parameter is set to
        // `true` is passed.
        roomConfig.isUserStatusNotify = true
        ZegoExpressEngine.getEngine().loginRoom(
            roomID, user, roomConfig
        ) { error: Int, extendedData: JSONObject? ->
            // Room login result. This callback is sufficient if you only need to
            // check the login result.
            if (error == 0) {
                // Login successful.
                // Start the preview and stream publishing.
                Toast.makeText(this, "Login successful.", Toast.LENGTH_LONG).show()
                startPreview(preview)
                startPublish()
            } else {
                // Login failed. For details, see [Error codes\|_blank](/404).
                Toast.makeText(this, "Login failed. error = $error", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun logoutRoom() {
        ZegoExpressEngine.getEngine().logoutRoom()
    }

    private fun startPublish() {
        // After calling the `loginRoom` method, call this method to publish streams.
        // The StreamID must be unique in the room.
        val streamID = roomID + "_" + userID + "_call"
        ZegoExpressEngine.getEngine().startPublishingStream(streamID)
    }

    private fun stopPublish() {
        ZegoExpressEngine.getEngine().stopPublishingStream()
    }

    fun startPlayStream(streamID: String?) {
        remoteUserView.visibility = View.VISIBLE
        val playCanvas = ZegoCanvas(remoteUserView)
        ZegoExpressEngine.getEngine().startPlayingStream(streamID, playCanvas)
    }

    fun stopPlayStream(streamID: String?) {
        ZegoExpressEngine.getEngine().stopPlayingStream(streamID)
        remoteUserView.visibility = View.GONE
    }

    private fun startListenEvent() {
        ZegoExpressEngine.getEngine().setEventHandler(object : IZegoEventHandler() {
            // Callback for updates on the status of the streams in the room.
            override fun onRoomStreamUpdate(
                roomID: String,
                updateType: ZegoUpdateType,
                streamList: ArrayList<ZegoStream>,
                extendedData: JSONObject
            ) {
                super.onRoomStreamUpdate(roomID, updateType, streamList, extendedData)
                // When `updateType` is set to `ZegoUpdateType.ADD`, an audio and video
                // stream is added, and you can call the `startPlayingStream` method to
                // play the stream.
                if (updateType == ZegoUpdateType.ADD) {
                    startPlayStream(streamList[0].streamID)
                } else {
                    stopPlayStream(streamList[0].streamID)
                }
            }

            // Callback for updates on the status of other users in the room.
            // Users can only receive callbacks when the isUserStatusNotify property of ZegoRoomConfig is set to `true` when logging in to the room (loginRoom).
            override fun onRoomUserUpdate(
                roomID: String,
                updateType: ZegoUpdateType,
                userList: ArrayList<ZegoUser>
            ) {
                super.onRoomUserUpdate(roomID, updateType, userList)
                // You can implement service logic in the callback based on the login
                // and logout status of users.
                if (updateType == ZegoUpdateType.ADD) {
                    for (user in userList) {
                        val text = user.userID + "logged in to the room."
                        Toast.makeText(applicationContext, text, Toast.LENGTH_LONG).show()
                    }
                } else if (updateType == ZegoUpdateType.DELETE) {
                    for (user in userList) {
                        val text = user.userID + "logged out of the room."
                        Toast.makeText(applicationContext, text, Toast.LENGTH_LONG).show()
                    }
                }
            }

            // Callback for updates on the current user's room connection status.
            override fun onRoomStateChanged(
                roomID: String,
                reason: ZegoRoomStateChangedReason,
                i: Int,
                jsonObject: JSONObject
            ) {
                super.onRoomStateChanged(roomID, reason, i, jsonObject)
                if (reason == ZegoRoomStateChangedReason.LOGINING) {
                    // Logging in to a room. When `loginRoom` is called to log in to a
                    // room or `switchRoom` is called to switch to another room, the room
                    // enters this status, indicating that it is requesting a connection
                    // to the server. On the app UI, the status of logging in to the room
                    // is displayed.
                } else if (reason == ZegoRoomStateChangedReason.LOGINED) {
                    // Logging in to a room succeeds. When a user successfully logs in to
                    // a room or switches the room, the room enters this status. In this
                    // case, the user can receive notifications of addition or deletion of
                    // other users and their streams in the room. Only after a user
                    // successfully logs in to a room or switches the room,
                    // `startPublishingStream` and `startPlayingStream` can be called to
                    // publish and play streams properly.
                } else if (reason == ZegoRoomStateChangedReason.LOGIN_FAILED) {
                    // Logging in to a room fails. When a user fails to log in to a room
                    // or switch the room due to a reason such as incorrect AppID or
                    // Token, the room enters this status.
                    Toast.makeText(
                        applicationContext,
                        "ZegoRoomStateChangedReason.LOGIN_FAILED",
                        Toast.LENGTH_LONG
                    ).show()
                } else if (reason == ZegoRoomStateChangedReason.RECONNECTING) {
                    // The room connection is temporarily interrupted. The SDK will retry
                    // internally if the interruption is caused by poor network quality.
                } else if (reason == ZegoRoomStateChangedReason.RECONNECTED) {
                    // Reconnecting a room succeeds. The SDK will retry internally if the
                    // interruption is caused by poor network quality. If the reconnection
                    // is successful, the room enters this status.
                } else if (reason == ZegoRoomStateChangedReason.RECONNECT_FAILED) {
                    // Reconnecting a room fails. The SDK will retry internally if the
                    // interruption is caused by poor network quality. If the reconnection
                    // fails, the room enters this status.
                    Toast.makeText(
                        applicationContext,
                        "ZegoRoomStateChangedReason.RECONNECT_FAILED",
                        Toast.LENGTH_LONG
                    ).show()
                } else if (reason == ZegoRoomStateChangedReason.KICK_OUT) {
                    // The server forces a user to log out of a room. If a user who has
                    // logged in to room A tries to log in to room B, the server forces
                    // the user to log out of room A and room A enters this status.
                    Toast.makeText(
                        applicationContext,
                        "ZegoRoomStateChangedReason.KICK_OUT",
                        Toast.LENGTH_LONG
                    ).show()
                } else if (reason == ZegoRoomStateChangedReason.LOGOUT) {
                    // Logging out of a room succeeds. This is the default status of a
                    // room before login. If a user successfully logs out of a room by
                    // calling `logoutRoom` or `switchRoom`, the room enters this status.
                } else if (reason == ZegoRoomStateChangedReason.LOGOUT_FAILED) {
                    // Logging out of a room fails. If a user fails to log out of a room
                    // by calling `logoutRoom` or `switchRoom`, the room enters this
                    // status.
                }
            }

            // Status notification of audio and video stream publishing.
            override fun onPublisherStateUpdate(
                streamID: String,
                state: ZegoPublisherState,
                errorCode: Int,
                extendedData: JSONObject
            ) {
                super.onPublisherStateUpdate(streamID, state, errorCode, extendedData)
                if (errorCode != 0) {
                    // Stream publishing exception.
                }
                if (state == ZegoPublisherState.PUBLISHING) {
                    // Publishing streams.
                } else if (state == ZegoPublisherState.NO_PUBLISH) {
                    // Streams not published.
                    Toast.makeText(
                        applicationContext,
                        "ZegoPublisherState.NO_PUBLISH",
                        Toast.LENGTH_LONG
                    ).show()
                } else if (state == ZegoPublisherState.PUBLISH_REQUESTING) {
                    // Requesting stream publishing.
                }
            }

            // Status notifications of audio and video stream playing.
            // This callback is received when the status of audio and video stream
            // playing of a user changes. If an exception occurs during stream playing
            // due to a network interruption, the SDK automatically retries to play
            // the streams.
            override fun onPlayerStateUpdate(
                streamID: String,
                state: ZegoPlayerState,
                errorCode: Int,
                extendedData: JSONObject
            ) {
                super.onPlayerStateUpdate(streamID, state, errorCode, extendedData)
                if (errorCode != 0) {
                    // Stream playing exception.
                    Toast.makeText(
                        applicationContext,
                        "onPlayerStateUpdate, state:" + state + "errorCode:" + errorCode,
                        Toast.LENGTH_LONG
                    ).show()
                }
                if (state == ZegoPlayerState.PLAYING) {
                    // Playing streams.
                } else if (state == ZegoPlayerState.NO_PLAY) {
                    // Streams not played.
                    Toast.makeText(applicationContext, "ZegoPlayerState.NO_PLAY", Toast.LENGTH_LONG)
                        .show()
                } else if (state == ZegoPlayerState.PLAY_REQUESTING) {
                    // Requesting stream playing.
                }
            }
        })
    }

    fun stopListenEvent() {
        ZegoExpressEngine.getEngine().setEventHandler(null)
    }

}