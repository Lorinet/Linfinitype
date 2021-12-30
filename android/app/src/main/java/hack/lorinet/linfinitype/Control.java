package hack.lorinet.linfinitype;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
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
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_control);
        textToSpeech = new TextToSpeech(this, this);
        GestureInterface.appContext = this;
        GestureInterface.webView = (WebView)findViewById(R.id.chatView);
        Intent intent = getIntent();
        if(intent.getStringExtra("testMode").equals("enabled"))
        {
            Log.i("Linfinitype", "Input test mode");
        }
        else
        {
            Log.i("Linfinitype", "Connecting to Linfinity device");
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
        }
        ((Button)findViewById(R.id.testButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                GestureInterface.input(((TextInputEditText)findViewById(R.id.inputTextView)).getText().toString());
                GestureInterface.input(((TextInputEditText)findViewById(R.id.inputTextView)).getText().toString());
                ((TextInputEditText)findViewById(R.id.inputTextView)).setText("");
            }
        });

        WebView webview = (WebView)findViewById(R.id.chatView);
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setAllowContentAccess(true);
        webview.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webview.getSettings().setAllowFileAccess(true);
        webview.getSettings().setAllowFileAccessFromFileURLs(true);
        webview.getSettings().setAllowUniversalAccessFromFileURLs(true);
        webview.getSettings().setDatabaseEnabled(true);
        webview.getSettings().setLoadsImagesAutomatically(true);
        webview.getSettings().setDomStorageEnabled(true);
        CookieManager.getInstance().setAcceptCookie(true);
        webview.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webview.setWebViewClient(new WebViewClient() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
            @Override
            public void onPageFinished(WebView view, String url)
            {
                if(url.endsWith("chats.php"))
                {
                    view.evaluateJavascript("javascript:localStorage.getItem('username')", new ValueCallback<String>() {
                        @Override public void onReceiveValue(String s) {
                            SharedPreferences userPref = getSharedPreferences("username", 0);
                            SharedPreferences.Editor userPrefEdit = userPref.edit();
                            userPrefEdit.putString("username", s.replace("\"", "")).commit();
                        }
                    });
                    view.evaluateJavascript("javascript:getActiveContacts()", new ValueCallback<String>() {
                        @Override public void onReceiveValue(String s) {
                            try
                            {
                                JSONArray ja = new JSONArray(s.substring(1, s.length() - 1).replace("\\", ""));
                                String[] conts = new String[ja.length()];
                                for(int i = 0; i < ja.length(); i++)
                                {
                                    conts[i] = ja.getString(i);
                                }
                                GestureInterface.menus[1] = new GestureInterface.GestureMenu("Chats", conts, new GestureInterface.GestureMenu.handler()
                                {
                                    @Override
                                    public void menuAction(String letter, String option)
                                    {
                                        Log.i("Chat", "Chat selected: " + option);
                                        GestureInterface.speakInterrupt(option);
                                        GestureInterface.chatCurrentUser = option;
                                        view.loadUrl(GestureInterface.chatUrl + "/conversation.php?user=" + option);
                                    }
                                });
                                GestureInterface.currentMenu = 1;
                                GestureInterface.showMenu();
                            }
                            catch (JSONException e)
                            {
                                e.printStackTrace();
                            }

                        }
                    });

                }
                else if(url.contains("conversation.php"))
                {
                    GestureInterface.enterInputMode();
                }
            }
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Log.e("WebBrowser", "ERROR: " + String.valueOf(errorCode) + " (" + description + ")");
                String htmlData = "<html><body><div style=\"margin-top: 100px; text-align: center;\"><h1>Nincs internetkapcsolat!</h1><br><button style=\"border-radius: 15px; color: black; border: 0px; background-color: #ddd; padding: 20px; font-size: 16px;\" onClick=\"window.history.go(-1);\">Újratöltés</button></div></body></html>";
                webview.loadUrl("about:blank");
                webview.loadDataWithBaseURL(null,htmlData, "text/html", "UTF-8",null);
                webview.invalidate();
            }
        });
        webview.setWebChromeClient(new WebChromeClient() {
            public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
                GestureInterface.speak(message);
                result.cancel();
                return true;
            }
        });
        webview.setHorizontalScrollBarEnabled(false);
        webview.setOnTouchListener(new View.OnTouchListener() {
            float m_downX;
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getPointerCount() > 1) {
                    return true;
                }
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        m_downX = event.getX();
                        break;
                    }
                    case MotionEvent.ACTION_MOVE:
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP: {
                        event.setLocation(m_downX, event.getY());
                        break;
                    }
                }
                return false;
            }
        });
        webview.loadUrl("about:blank");
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
            Log.e("TTS", "Init failed (" + String.valueOf(status) + ")");
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
            inputBuffer = new byte[16];
            int numBytes;

            while (true)
            {
                try
                {
                    if (inputStream.available() >= 6)
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
