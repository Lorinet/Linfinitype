package hack.lorinet.linfinitype.apps;

import static hack.lorinet.linfinitype.GestureUI.HANDLE_NULL;

import android.content.SharedPreferences;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import org.json.JSONArray;
import org.json.JSONException;

import hack.lorinet.linfinitype.Application;
import hack.lorinet.linfinitype.GestureUI;

public class Chat extends Application
{
    private int menuHandle = HANDLE_NULL;
    private int chatsWebViewEventHandler = HANDLE_NULL;
    private int conversationWebViewEventHandler = HANDLE_NULL;
    private int replyTextHandler = -1;

    public static final String chatUrl = "http://nebulonia.ro:8088";
    public static String currentUser = "";

    public Chat()
    {
        name = "Chat";
        replyTextHandler = GestureUI.registerTextInputHandler(new GestureUI.TextInputHandler()
        {
            @Override
            public void input(String text)
            {
                GestureUI.speakInterrupt(GestureUI.currentTextInput);
                GestureUI.webView.evaluateJavascript("javascript:reply('" + text + "')", new ValueCallback<String>()
                {
                    @Override
                    public void onReceiveValue(String value)
                    {

                    }
                });
            }
        });
        chatsWebViewEventHandler = GestureUI.registerWebViewEventHandler(new GestureUI.WebViewEventHandler("chats.php", new GestureUI.WebViewEventHandler.handler()
        {
            @Override
            public void onPageFinished(WebView view)
            {
                view.evaluateJavascript("javascript:localStorage.getItem('username')", new ValueCallback<String>()
                {
                    @Override
                    public void onReceiveValue(String s)
                    {
                        SharedPreferences userPref = GestureUI.appContext.getSharedPreferences("username", 0);
                        SharedPreferences.Editor userPrefEdit = userPref.edit();
                        userPrefEdit.putString("username", s.replace("\"", "")).commit();
                    }
                });
                view.evaluateJavascript("javascript:getActiveContacts()", new ValueCallback<String>()
                {
                    @Override
                    public void onReceiveValue(String s)
                    {
                        try
                        {
                            JSONArray ja = new JSONArray(s.substring(1, s.length() - 1).replace("\\", ""));
                            String[] conts = new String[ja.length()];
                            for (int i = 0; i < ja.length(); i++)
                            {
                                conts[i] = ja.getString(i);
                            }
                            menuHandle = GestureUI.registerGestureMenu(new GestureUI.GestureMenu("Chats", conts, new GestureUI.GestureMenu.handler()
                            {
                                @Override
                                public void menuAction(String letter, String option)
                                {
                                    Log.i("Chat", "Chat selected: " + option);
                                    GestureUI.speakInterrupt(option);
                                    currentUser = option;
                                    view.loadUrl(chatUrl + "/conversation.php?user=" + option);
                                }
                            }));
                            GestureUI.activateMenu(menuHandle);
                        }
                        catch (JSONException e)
                        {
                            e.printStackTrace();
                        }

                    }
                });
            }
        }));
        conversationWebViewEventHandler = GestureUI.registerWebViewEventHandler(new GestureUI.WebViewEventHandler("conversation.php", new GestureUI.WebViewEventHandler.handler()
        {
            @Override
            public void onPageFinished(WebView view)
            {
                GestureUI.activateTextInput(replyTextHandler);
            }
        }));
    }

    @Override
    public void start()
    {
        GestureUI.webView.loadUrl(chatUrl);
    }

    @Override
    public void unregister()
    {
        menuHandle = GestureUI.unregisterGestureMenu(menuHandle);
        chatsWebViewEventHandler = GestureUI.unregisterWebViewEventHandler(chatsWebViewEventHandler);
        conversationWebViewEventHandler = GestureUI.unregisterWebViewEventHandler(conversationWebViewEventHandler);
        replyTextHandler = GestureUI.unregisterTextInputHandler(replyTextHandler);
    }
}
