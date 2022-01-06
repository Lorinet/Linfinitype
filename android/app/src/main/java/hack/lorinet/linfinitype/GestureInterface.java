package hack.lorinet.linfinitype;

import static androidx.core.content.ContextCompat.startActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import java.util.HashMap;
import java.util.Map;

public class GestureInterface
{
    public static Context appContext;
    public static TextToSpeech textToSpeech;
    public static WebView webView;

    public static int doubleCheckGesture = 256;
    public static int previousGesture = 256;
    public static String currentInput = "";
    public static boolean activated = false;
    public static boolean textInput = false;

    public static final String chatUrl = "http://ec2-3-132-15-124.us-east-2.compute.amazonaws.com:8081";

    public static String chatCurrentUser = "";
    public static String currentPhoneNumber = "";

    public static Map<String, String> favoriteContacts;

    public static String[] gestureTable =
            {
                    "Start", // 00000
                    "Ok", // 10000
                    "?", // 01000
                    " ", // 11000
                    "Delete", // 00100
                    "Exit", // 10100
                    "A", // 01100
                    "B", // 11100
                    "C", // 00010
                    "D", // 10010
                    "E", // 01010
                    "F", // 11010
                    "G", // 00110
                    "H", // 10110
                    "I", // 01110
                    "J", // 11110
                    "K", // 00001
                    "L", // 10001
                    "M", // 01001
                    "N", // 11001
                    "O", // 00101
                    "P", // 10101
                    "Q", // 01101
                    "R", // 11101
                    "S", // 00011
                    "T", // 10011
                    "U", // 01011
                    "V", // 11011
                    "W", // 00111
                    "X", // 10111
                    "Y", // 01111
                    "Idle" // 11111
            };

    public interface TextInputHandler
    {
        void input(String text);
    }

    public static class GestureMenu
    {
        public interface handler
        {
            void menuAction(String letter, String option);
        }

        public String title;
        public String[] options;
        public handler handler;

        public GestureMenu(String title, String[] options, GestureMenu.handler handler)
        {
            this.title = title;
            this.options = options;
            this.handler = handler;
        }

        public void speakMenu()
        {
            speak(title);
            for (int i = 0; i < options.length; i++)
            {
                speak(numberToCharacter(i) + ", " + options[i]);
            }
        }

        public void activateOption(String opt)
        {
            int index = characterToNumber(opt);
            if (index >= 0 && index < options.length)
            {
                handler.menuAction(opt, options[index]);
            }
        }
    }

    public static int currentInputHandler = 0;
    public static TextInputHandler[] inputHandlers =
            {
                    new TextInputHandler()
                    {
                        @Override
                        public void input(String text)
                        {
                            speakInterrupt(currentInput);
                            webView.evaluateJavascript("javascript:reply('" + text + "')", new ValueCallback<String>()
                            {
                                @Override
                                public void onReceiveValue(String value)
                                {

                                }
                            });
                        }
                    },
                    new TextInputHandler()
                    {
                        @Override
                        public void input(String text)
                        {
                            String number = "";
                            for(int i = 0; i < text.length(); i++)
                            {
                                number += characterToNumber(String.valueOf(text.charAt(i)));
                            }
                            speakInterrupt(number);
                            textInput = false;
                            activateMenu(4);
                        }
                    }
            };

    public static int currentMenu = 0;
    public static GestureMenu[] menus =
            {
                    new GestureMenu("Main menu", new String[]{"Chat", "Phone", "Assistant", "Notes", "Calculator", "Music"}, new GestureMenu.handler()
                    {
                        @Override
                        public void menuAction(String letter, String option)
                        {
                            switch (option)
                            {
                                case "Chat":
                                    webView.loadUrl(chatUrl);
                                    break;
                                case "Phone":
                                    speakInterrupt("You selected Phone.");
                                    break;
                                case "Assistant":
                                    startActivity(appContext, new Intent(Intent.ACTION_VOICE_COMMAND).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK), null);
                                    break;
                            }
                        }
                    }),
                    null,
                    new GestureMenu("Phone", new String[]{"Dialer", "Contacts"}, new GestureMenu.handler()
                    {
                        @Override
                        public void menuAction(String letter, String option)
                        {
                            switch(option)
                            {
                                case "Dialer":
                                    speakInterrupt("Use letters A to K for digits 0 to 9.");
                                    currentInputHandler = 1;
                                    textInput = true;
                                    break;
                                case "Contacts":
                                    activateMenu(3);
                                    break;
                            }
                        }
                    }),
                    new GestureMenu("You will have to grant Contacts permission.", new String[]{}, new GestureMenu.handler()
                    {
                        @Override
                        public void menuAction(String letter, String option)
                        {
                            GestureInterface.activateMenu(2);
                        }
                    }),
                    new GestureMenu("", new String[]{"Call", "Cancel"}, new GestureMenu.handler()
                    {
                        @Override
                        public void menuAction(String letter, String option)
                        {
                            switch(option)
                            {
                                case "Call":
                                    call(currentPhoneNumber);
                                    break;
                                case "Cancel":
                                    currentPhoneNumber = "";
                                    break;
                            }
                        }
                    }),
                    new GestureMenu("Notes", new String[]{"New note"}, new GestureMenu.handler()
                    {
                        @Override
                        public void menuAction(String letter, String option)
                        {

                        }
                    })
            };

    public static void call(String number)
    {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + number));//change the number
        appContext.startActivity(callIntent);
    }

    public static void activateMenu(int index)
    {
        textInput = false;
        currentInput = "";
        currentMenu = index;
        showMenu();
    }

    public static String numberToCharacter(int i)
    {
        return i + 1 > 0 && i + 1 < 27 ? String.valueOf((char) (i + 65)) : null;
    }

    public static int characterToNumber(String c)
    {
        Log.i("CharToNum", String.valueOf((int) (c.charAt(0) - 65)));
        return c.charAt(0) - 65;
    }

    public static void speak(String text)
    {
        if (textToSpeech != null)
        {
            textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null, "");
        }
    }

    public static void speakInterrupt(String text)
    {
        if (textToSpeech != null)
        {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "");
        }
    }

    public static void showMenu()
    {
        menus[currentMenu].speakMenu();
    }

    public static void selectMenuItem(String letter)
    {
        menus[currentMenu].activateOption(letter);
    }

    public static void input(String gesture)
    {
        Log.i("Gesture", gesture);
        Log.i("CurrentMenu", String.valueOf(currentMenu));
        byte g = 0;
        g ^= (gesture.charAt(0) == '1' ? 1 << 0 : 0);
        g ^= (gesture.charAt(1) == '1' ? 1 << 1 : 0);
        g ^= (gesture.charAt(2) == '1' ? 1 << 2 : 0);
        g ^= (gesture.charAt(3) == '1' ? 1 << 3 : 0);
        g ^= (gesture.charAt(4) == '1' ? 1 << 4 : 0);
        int ge = g & 0xFF;
        if (ge != doubleCheckGesture)
        {
            previousGesture = doubleCheckGesture;
            doubleCheckGesture = ge;
        }
        else if (previousGesture != doubleCheckGesture)
        {
            previousGesture = ge;
            if (ge == 31) return;
            String gest = gestureTable[ge];
            if (!activated && gest == "Start")
            {
                activated = true;
                showMenu();
            }
            else if (activated)
            {
                if (textInput)
                {
                    if (gest == "?" || gest == " " || ge > 5)
                    {
                        currentInput += gest;
                        speakInterrupt(gest);
                    }
                    else
                    {
                        switch (gest)
                        {
                            case "Exit":
                                textInput = false;
                                currentInput = "";
                                break;
                            case "Delete":
                                currentInput = currentInput.substring(0, currentInput.length() - 1);
                                speakInterrupt(currentInput);
                                break;
                            case "Ok":
                                inputHandlers[currentInputHandler].input(currentInput);
                                currentInput = "";
                                break;
                        }
                    }
                }
                else
                {
                    if (ge > 5)
                    {
                        selectMenuItem(gest);
                    }
                    else
                    {
                        switch (gest)
                        {
                            case "Start":
                                activated = false;
                                break;
                            case "Exit":
                                activateMenu(0);
                                break;
                            case "?":
                                showMenu();
                                break;
                        }
                    }
                }
            }
        }
    }

    public static void enterInputMode()
    {
        textInput = true;
        speakInterrupt("Text mode");
    }

    public static void back()
    {
        currentMenu = 0;
        showMenu();
    }

    public static void help()
    {
        speakInterrupt("To read the menu, clench all of your fingers. To activate an option, apply the gesture corresponding to the option's letter. To return to the main menu, apply the Back gesture.");
    }
}
