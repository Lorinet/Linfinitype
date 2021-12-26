package hack.lorinet.linfinitype;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.UUID;

public class Control extends AppCompatActivity implements TextToSpeech.OnInitListener
{
    BluetoothAdapter bluetoothAdap = null;
    BluetoothSocket bluetoothSocket = null;
    Boolean connected = false;
    static final UUID deviceUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    String macAddress = null;
    private ConnectedThread connectedThread;
    private TextToSpeech textToSpeech;

    private final Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case 0:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    //Toast.makeText(getApplicationContext(), readMessage, Toast.LENGTH_SHORT);
                    GestureInterface.input(readMessage);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_control);
        textToSpeech = new TextToSpeech(this, this);
        GestureInterface.appContext = this;
        Intent intent = getIntent();
        macAddress = intent.getStringExtra("address");
        try
        {
            if (bluetoothSocket == null || !connected)
            {
                bluetoothAdap = BluetoothAdapter.getDefaultAdapter();
                BluetoothDevice hc = bluetoothAdap.getRemoteDevice(macAddress);
                bluetoothSocket = hc.createInsecureRfcommSocketToServiceRecord(deviceUUID);
                bluetoothAdap.cancelDiscovery();
                bluetoothSocket.connect();
                connectedThread = new ConnectedThread(bluetoothSocket);
                connectedThread.start();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        ((Button)findViewById(R.id.testButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                GestureInterface.input(((TextInputEditText)findViewById(R.id.inputTextView)).getText().toString());
            }
        });
    }

    @Override
    public void onInit(int status)
    {
        if (status == TextToSpeech.SUCCESS)
        {
            int result = textToSpeech.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
            {
                Log.e("TTS", "Init failed. English language not supported.");
            }
            GestureInterface.textToSpeech = textToSpeech;
        }
        else
        {
            Log.e("TTS", "Init failed.");
        }
    }

    @Override
    public void onDestroy()
    {
        if (textToSpeech != null)
        {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    private class ConnectedThread extends Thread
    {
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private byte[] inputBuffer;

        public ConnectedThread(BluetoothSocket socket)
        {
            bluetoothSocket = socket;
            InputStream tmpIn = null;

            try
            {
                tmpIn = socket.getInputStream();
            }
            catch (IOException e)
            {
                Log.e("LinfinitypeDevice", "Error occurred when creating input stream", e);
            }

            inputStream = tmpIn;
        }

        public void run()
        {
            inputBuffer = new byte[1024];
            int numBytes;

            while (true)
            {
                try
                {
                    if (inputStream.available() >= 5)
                    {
                        numBytes = inputStream.read(inputBuffer);
                        byte[] mesgBytes = new byte[inputBuffer.length];
                        System.arraycopy(inputBuffer, 0, mesgBytes, 0, inputBuffer.length);
                        Message readMsg = handler.obtainMessage(0, numBytes, -1, mesgBytes);
                        readMsg.sendToTarget();
                    }
                }
                catch (IOException e)
                {
                    Log.d("LinfinitypeDevice", "Input stream was disconnected", e);
                    break;
                }
            }
        }

        public void cancel()
        {
            try
            {
                bluetoothSocket.close();
            }
            catch (IOException e)
            {
                Log.e("LinfinitypeDevice", "Could not close the connect socket", e);
            }
        }
    }
}
