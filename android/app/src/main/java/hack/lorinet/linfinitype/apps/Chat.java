/*
 * Linfinitype Android application
 * Copyright (C) 2022 Kovacs Lorand; Linfinity Technologies
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Lesser Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

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
    private int loginWebViewEventHandler = HANDLE_NULL;
    private int replyTextHandler = HANDLE_NULL;
    private int usernameTextHandler = HANDLE_NULL;
    private int passwordTextHandler = HANDLE_NULL;

    private String loginUsername = "";
    private String loginPassword = "";

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
        usernameTextHandler = GestureUI.registerTextInputHandler(new GestureUI.TextInputHandler()
        {
            @Override
            public void input(String text)
            {
                loginUsername = text;
                GestureUI.speak("Enter password");
                GestureUI.activateTextInput(passwordTextHandler);
            }
        });
        passwordTextHandler = GestureUI.registerTextInputHandler(new GestureUI.TextInputHandler()
        {
            @Override
            public void input(String text)
            {
                loginPassword = text;
                GestureUI.webView.evaluateJavascript("document.getElementById('usernameBox').value = '" + loginUsername + "'; document.getElementById('passwordBox').value = '" + loginPassword + "'; document.getElementById('loginForm').submit();", new ValueCallback<String>()
                {
                    @Override
                    public void onReceiveValue(String value)
                    {

                    }
                });
            }
        });
        loginWebViewEventHandler = GestureUI.registerWebViewEventHandler(new GestureUI.WebViewEventHandler("login.php", new GestureUI.WebViewEventHandler.handler()
        {
            @Override
            public void onPageFinished(WebView view)
            {
                GestureUI.speak("Enter username");
                GestureUI.activateTextInput(usernameTextHandler);
            }
        }));
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
                view.evaluateJavascript("alertNewMessages = true;", new ValueCallback<String>()
                {
                    @Override
                    public void onReceiveValue(String value)
                    {

                    }
                });
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
