package hack.lorinet.linfinitype;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.webkit.WebView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;

import java.util.Dictionary;

public class ChatApi
{
    public static String username;
    public static Context context;
    public static RequestQueue requestQueue;
    public static WebView webView;

    public static void init(Context ctx, WebView wv)
    {
        context = ctx;
        webView = wv;
        SharedPreferences userPref = context.getSharedPreferences("username", 0);
        username = userPref.getString("username", "");
        RequestQueue requestQueue = Volley.newRequestQueue(context);
    }

}
