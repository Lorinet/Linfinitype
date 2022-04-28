package hack.lorinet.linfinitype;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputEditText;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class InterfaceView extends AppCompatActivity implements TextToSpeech.OnInitListener
{
    static final int REQUEST_CODE_CONTACTS_PERMISSION = 0;
    static final int REQUEST_CODE_EXTERNAL_STORAGE_PERMISSION = 1;

    // UUID for HC-05 module
    static final UUID deviceUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private TextToSpeech textToSpeech;
    private BluetoothAdapter bluetoothAdap = null;
    private BluetoothSocket bluetoothSocket = null;
    private ConnectedThread connectedThread;
    private Boolean connected = false;
    private String macAddress = null;

    private final Handler handler = new Handler()
    {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case 0:
                    char inp = (char) ((byte[]) msg.obj)[0];
                    GestureUI.input(inp);
                    break;
            }
        }
    };

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_interface);
        textToSpeech = new TextToSpeech(this, this);
        GestureUI.appContext = this;
        GestureUI.webView = (WebView) findViewById(R.id.appWebView);
        Intent intent = getIntent();
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
                connected = true;
                connectedThread = new ConnectedThread(bluetoothSocket);
                connectedThread.start();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        ((Button) findViewById(R.id.testButton)).setOnClickListener(new View.OnClickListener()
        {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v)
            {
                if(TestSuite.testing())
                {
                    TestSuite.stopTest();
                    Toast.makeText(getApplicationContext(), "Stopped data recording", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    TestSuite.newTest();
                    Toast.makeText(getApplicationContext(), "Recording new test", Toast.LENGTH_SHORT).show();
                }
            }
        });

        WebView webview = (WebView) findViewById(R.id.appWebView);
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
        webview.setWebViewClient(new WebViewClient()
        {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url)
            {
                for (int i = 0; i < GestureUI.webViewEventHandlers.size(); i++)
                {
                    if (url.contains(GestureUI.webViewEventHandlers.get(i).url))
                    {
                        GestureUI.webViewEventHandlers.get(i).onPageFinished(view);
                    }
                }
            }

            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl)
            {
                Log.e("WebBrowser", "ERROR: " + String.valueOf(errorCode) + " (" + description + ")");
                String htmlData = "<html><body><div style=\"margin-top: 100px; text-align: center;\"><h1>Nincs internetkapcsolat!</h1><br><button style=\"border-radius: 15px; color: black; border: 0px; background-color: #ddd; padding: 20px; font-size: 16px;\" onClick=\"window.history.go(-1);\">Újratöltés</button></div></body></html>";
                webview.loadUrl("about:blank");
                webview.loadDataWithBaseURL(null, htmlData, "text/html", "UTF-8", null);
                webview.invalidate();
            }
        });
        webview.setWebChromeClient(new WebChromeClient()
        {
            public boolean onJsAlert(WebView view, String url, String message, JsResult result)
            {
                GestureUI.speak(message);
                result.cancel();
                return true;
            }
        });
        webview.setHorizontalScrollBarEnabled(false);
        webview.setOnTouchListener(new View.OnTouchListener()
        {
            float m_downX;

            public boolean onTouch(View v, MotionEvent event)
            {
                if (event.getPointerCount() > 1)
                {
                    return true;
                }
                switch (event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                    {
                        m_downX = event.getX();
                        break;
                    }
                    case MotionEvent.ACTION_MOVE:
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                    {
                        event.setLocation(m_downX, event.getY());
                        break;
                    }
                }
                return false;
            }
        });
        webview.loadUrl("about:blank");
        requestPermissions();
        if (Build.VERSION.SDK_INT >= 30)
        {
            if (!Environment.isExternalStorageManager()){
                Intent getpermission = new Intent();
                getpermission.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(getpermission);
            }
        }
        GestureUI.start();
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
            GestureUI.textToSpeech = textToSpeech;
        }
        else
        {
            Log.e("TTS", "Init failed (" + String.valueOf(status) + ")");
        }
    }

    @Override
    public void onBackPressed()
    {
        finish();
    }

    @Override
    public void onDestroy()
    {
        if (textToSpeech != null)
        {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        if(connected) connected = false;
        super.onDestroy();
    }

    private class ConnectedThread extends Thread
    {
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private byte[] inputBuffer;
        private boolean stop = false;

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
                    if(!connected)
                    {
                        try
                        {
                            bluetoothSocket.close();
                            Log.i("LinfinitypeDevice", "Disconnected");
                        }
                        catch (IOException e)
                        {
                            Log.e("LinfinitypeDevice", "Could not close the connect socket", e);
                        }
                    }
                    if (inputStream.available() >= 1)
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
                    Log.e("LinfinitypeDevice", "Input stream was disconnected", e);
                    break;
                }
            }
        }

        public void cancel()
        {
            stop = true;
        }
    }

    private void requestPermissions()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.MANAGE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_CONTACTS))
                {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Read Contacts permission");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setMessage("Please enable access to contacts.");
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener()
                    {
                        @TargetApi(Build.VERSION_CODES.M)
                        @Override
                        public void onDismiss(DialogInterface dialog)
                        {
                            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.MANAGE_EXTERNAL_STORAGE}, REQUEST_CODE_CONTACTS_PERMISSION);
                        }
                    });
                    builder.show();
                }
                else
                {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.MANAGE_EXTERNAL_STORAGE}, REQUEST_CODE_CONTACTS_PERMISSION);
                }
            }
            else
            {
                getFavoriteContacts();
            }
        }
        else
        {
            getFavoriteContacts();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_CONTACTS_PERMISSION)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                getFavoriteContacts();
            }
            else
            {
                Toast.makeText(this, "You have denied the contacts permission", Toast.LENGTH_LONG).show();
            }
            return;
        }
    }

    @SuppressLint("Range")
    private void getFavoriteContacts()
    {
        HashMap<String, String> contactMap = new HashMap<>();
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if ((cur != null ? cur.getCount() : 0) > 0)
        {
            while (cur != null && cur.moveToNext())
            {
                @SuppressLint("Range") String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                @SuppressLint("Range") String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                @SuppressLint("Range") String fav = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.STARRED));
                if (fav.equals("1"))
                {
                    if (cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0)
                    {
                        Cursor pCur = cr.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                new String[]{id}, null);
                        while (pCur.moveToNext())
                        {
                            String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            contactMap.put(name, phoneNo);
                            Log.i("Loaded contact", name + ": " + phoneNo + " " + fav);
                        }
                        pCur.close();
                    }
                }
            }
        }
        if (cur != null)
        {
            cur.close();
        }
        GestureUI.favoriteContacts = contactMap;
    }
}
