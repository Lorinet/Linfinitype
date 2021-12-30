package hack.lorinet.linfinitype;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.webkit.ValueCallback;
import android.webkit.WebView;

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

    public static String[] gestureTable =
            {
                    "Start",
                    "Ok",
                    "?",
                    " ",
                    "Delete",
                    "Exit",
                    "A",
                    "B",
                    "C",
                    "D",
                    "E",
                    "F",
                    "G",
                    "H",
                    "I",
                    "J",
                    "K",
                    "L",
                    "M",
                    "N",
                    "O",
                    "P",
                    "Q",
                    "R",
                    "S",
                    "T",
                    "U",
                    "V",
                    "W",
                    "X",
                    "Y",
                    "Idle"
            };

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
            if(index >= 0 && index < options.length)
                handler.menuAction(opt, options[index]);
        }
    }

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
                    null
            };

    public static String numberToCharacter(int i)
    {
        return i + 1 > 0 && i + 1 < 27 ? String.valueOf((char) (i + 65)) : null;
    }

    public static int characterToNumber(String c)
    {
        Log.i("CharToNum", String.valueOf((int)(c.charAt(0) - 65)));
        return c.charAt(0) - 65;
    }

    public static void speak(String text)
    {
        if(textToSpeech != null)
            textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null, "");
    }

    public static void speakInterrupt(String text)
    {
        if(textToSpeech != null)
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "");
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
        if(ge != doubleCheckGesture)
        {
            previousGesture = doubleCheckGesture;
            doubleCheckGesture = ge;
        }
        else if(previousGesture != doubleCheckGesture)
        {
            previousGesture = ge;
            if(ge == 31) return;
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
                                speakInterrupt(currentInput);
                                if(currentMenu == 1)
                                {
                                    webView.evaluateJavascript("javascript:reply('" + currentInput + "')", new ValueCallback<String>()
                                    {
                                        @Override
                                        public void onReceiveValue(String value)
                                        {

                                        }
                                    });
                                }
                                currentInput = "";
                                break;
                        }
                    }
                }
                else
                {
                    if (ge > 5) selectMenuItem(gest);
                    else
                    {
                        switch (gest)
                        {
                            case "Start":
                                activated = false;
                                break;
                            case "Exit":
                                currentMenu = 0;
                                showMenu();
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
