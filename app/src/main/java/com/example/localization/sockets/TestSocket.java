package com.example.localization.sockets;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.localization.HomeActivity;
import com.example.localization.R;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.HubConnectionState;
import com.microsoft.signalr.TypeReference;

import java.lang.reflect.Type;

public class TestSocket extends AppCompatActivity {

    Button btnConnect, btnDisconnect, btnJoinLeave;
    HubConnection hubConnection;
    String token, username;
    TextView tvUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_socket);

        token = HomeActivity.myToken;
        username = HomeActivity.username;
        System.out.println("Token:"+token);
        System.out.println("Username"+username);

        // Create hubconnection
        hubConnection = HubConnectionBuilder.create("http://35.239.225.98:443/hubs/chat")
                .withHeader("Authorization", "Bearer "+token)
                .build();

        /*hubConnection = HubConnectionBuilder.create("http://10.0.2.2:5004/hubs/chat")
                .withHeader("Authorization", "Bearer "+token)
                .build();*/

        hubConnection.on("NewUser", (msg) -> {
            System.out.println("Message: "+(msg));
        }, String.class);

        hubConnection.on("LeftUser", (msg) -> {
            System.out.println((msg));
        }, String.class);

        /** Receive Object custom */
        Type newMessageType = new TypeReference<NewMessage>() { }.getType();
        this.hubConnection.<NewMessage>on("NewMessage", (message) -> { // OK!!
            System.out.println(message.userName);
            System.out.println(message.latitude);
            System.out.println(message.logitude);
        }, newMessageType);


        btnConnect = findViewById(R.id.buttonConnect);
        btnDisconnect = findViewById(R.id.buttonDisconnect);
        btnJoinLeave = findViewById(R.id.buttonJoinGroup);
        tvUsername = findViewById(R.id.textViewUsername);

        tvUsername.setText(username);
    }

    public void doConnection(View v){
        if(hubConnection.getConnectionState() == HubConnectionState.DISCONNECTED){
            hubConnection.start();
            Toast.makeText(this, "Successful Connection", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this, "You are already connected", Toast.LENGTH_SHORT).show();
        }
    }

    public void doDisconnection(View v){
        if(hubConnection.getConnectionState() == HubConnectionState.CONNECTED){
            hubConnection.stop();
            Toast.makeText(this, "You disconnected", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this, "You are not connected yet", Toast.LENGTH_SHORT).show();
        }
    }

    public void joinToGroup(View v){

        if(btnJoinLeave.getText().toString().equalsIgnoreCase("join")){
            if(hubConnection.getConnectionState() == HubConnectionState.CONNECTED && !username.isEmpty()){
                hubConnection.send("JoinGroup");
                //System.out.println("JoinToGroup: "+ username);

                btnJoinLeave.setText("leave");
            }
            else{
                Toast.makeText(this, "You are not connected", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            if(btnJoinLeave.getText().toString().equalsIgnoreCase("leave")){
                if(hubConnection.getConnectionState() == HubConnectionState.CONNECTED){

                    hubConnection.send("LeaveGroup");
                    System.out.println("LeaveGroup: "+ username);
                    btnJoinLeave.setText("join");
                }
                else{
                    Toast.makeText(this, "You are not connected", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void sendMessage(View v){
        NewMessage message = new NewMessage();

        message.setLatitude("150");
        message.setLogitude("250");
        message.setUserName(username);

        // si esta conectado y unido al canal/grupo
        if(hubConnection.getConnectionState() == HubConnectionState.CONNECTED &&
                btnJoinLeave.getText().toString().equalsIgnoreCase("leave")){

            /** Test to send message **/
            hubConnection.send("SendMessage", message);
        }
        else{
            Toast.makeText(this, "You are not connected", Toast.LENGTH_SHORT).show();
        }


    }
}