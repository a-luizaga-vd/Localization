package com.example.localization.sockets;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.localization.HomeActivity;
import com.example.localization.R;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.HubConnectionState;

import java.util.Locale;

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
        /*hubConnection = HubConnectionBuilder.create("http://35.239.225.98:443/hubs/chat")
                .withHeader("Authorization", "Bearer "+token)
                .build();*/

        hubConnection = HubConnectionBuilder.create("http://10.0.2.2:5004/hubs/chat")
                .withHeader("Authorization", "Bearer "+token)
                .build();

        hubConnection.on("NewUser", (msg) -> {
            System.out.println("Messeage: "+(msg));
        }, String.class);

        hubConnection.on("LeftUser", (msg) -> {
            System.out.println((msg));
        }, String.class);

        btnConnect = findViewById(R.id.buttonConnect);
        btnDisconnect = findViewById(R.id.buttonDisconnect);
        btnJoinLeave = findViewById(R.id.buttonJoinGroup);
        tvUsername = findViewById(R.id.textViewUsername);

        tvUsername.setText(username);
    }

    public void doConnection(View v){
        if(hubConnection.getConnectionState() == HubConnectionState.DISCONNECTED){
            hubConnection.start();
            Toast.makeText(this, "Conexion exitosa", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this, "Y estas conectado amigo..", Toast.LENGTH_SHORT).show();
        }
    }

    public void doDisconnection(View v){
        if(hubConnection.getConnectionState() == HubConnectionState.CONNECTED){
            hubConnection.stop();
            Toast.makeText(this, "Te desconectaste chau...", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this, "No estas conectado todavia.", Toast.LENGTH_SHORT).show();
        }
    }

    public void joinToGroup(View v){

        NewMessage message = new NewMessage();
        message.setUserName(username);
        message.setLatitud("-100");
        message.setLogitud("-50");

        if(btnJoinLeave.getText().toString().equalsIgnoreCase("join")){
            if(hubConnection.getConnectionState() == HubConnectionState.CONNECTED && !username.isEmpty()){
                hubConnection.send("JoinGroup");
                //System.out.println("JoinToGroup: "+ username);

                /** Test to send messeage **/
                /*hubConnection.send("NewMesseage", message);*/
                btnJoinLeave.setText("leave");
            }
            else{
                Toast.makeText(this, "You are not conneceted", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            if(btnJoinLeave.getText().toString().equalsIgnoreCase("leave")){
                if(hubConnection.getConnectionState() == HubConnectionState.CONNECTED){
                    //String username = etName.getText().toString();
                    hubConnection.send("LeaveGroup");
                    System.out.println("LeaveGroup: "+ username);
                    btnJoinLeave.setText("join");
                }
                else{
                    Toast.makeText(this, "You are not conneceted", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}