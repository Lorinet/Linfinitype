package hack.lorinet.linfinitype;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.webkit.WebView;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Map;

import hack.lorinet.linfinitype.apps.Assistant;
import hack.lorinet.linfinitype.apps.Calculator;
import hack.lorinet.linfinitype.apps.Chat;
import hack.lorinet.linfinitype.apps.Launcher;
import hack.lorinet.linfinitype.apps.Notes;
import hack.lorinet.linfinitype.apps.Phone;
import hack.lorinet.linfinitype.apps.Trainer;

public class GestureUI
{
    public static Context appContext;
    public static TextToSpeech textToSpeech;
    @SuppressLint("StaticFieldLeak")
    public static WebView webView;

    public static final int HANDLE_NULL = -1;

    public static char doubleCheckGesture = 256;
    public static char previousGesture = 256;
    public static String currentTextInput = "";
    public static boolean activated = false;
    public static boolean textInput = false;

    public static Map<String, String> favoriteContacts = null;

    public static class WebViewEventHandler
    {
        public interface handler
        {
            void onPageFinished(WebView view);
        }

        public String url;
        public handler onPageFinishedHandler;

        public WebViewEventHandler(String url, handler onPageFinishedHandler)
        {
            this.url = url;
            this.onPageFinishedHandler = onPageFinishedHandler;
        }

        public void onPageFinished(WebView view)
        {
            onPageFinishedHandler.onPageFinished(view);
        }
    }

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
            speakInterrupt(title);
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

    public static final char GESTURE_START = '!';
    public static final char GESTURE_OK = '$';
    public static final char GESTURE_EXIT = '^';
    public static final char GESTURE_DELETE = '-';

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void input(char gesture)
    {
        TestSuite.testLog(Character.toString(gesture));
        if (gesture != doubleCheckGesture)
        {
            previousGesture = doubleCheckGesture;
            doubleCheckGesture = gesture;
        }
        else if (previousGesture != doubleCheckGesture)
        {
            previousGesture = gesture;
            if (!activated && gesture == GESTURE_START)
            {
                activated = true;
                showMenu();
            }
            else if (activated)
            {
                if (textInput)
                {
                    switch (gesture)
                    {
                        case GESTURE_EXIT:
                            textInput = false;
                            currentTextInput = "";
                            showMenu();
                            break;
                        case GESTURE_DELETE:
                            if (currentTextInput.length() > 0)
                            {
                                currentTextInput = currentTextInput.substring(0, currentTextInput.length() - 1);
                                speakInterrupt(currentTextInput);
                            }
                            break;
                        case GESTURE_OK:
                            inputHandlers.get(currentInputHandler).input(currentTextInput);
                            currentTextInput = "";
                            break;
                        default:
                            currentTextInput += gesture;
                            speakInterrupt(Character.toString(gesture));
                            break;
                    }
                }
                else
                {
                    switch (gesture)
                    {
                        case GESTURE_START:
                            activated = false;
                            speakInterrupt("Off");
                            break;
                        case GESTURE_EXIT:
                            launchApplication(APP_LAUNCHER);
                            break;
                        case '?':
                            showMenu();
                            break;
                        default:
                            selectMenuItem(Character.toString(gesture));
                            break;
                    }
                }
            }
        }
    }

    public static ArrayList<WebViewEventHandler> webViewEventHandlers = new ArrayList<>();

    public static int currentInputHandler = 0;
    public static ArrayList<TextInputHandler> inputHandlers = new ArrayList<>();

    public static int currentMenu = 0;
    public static ArrayList<GestureMenu> menus = new ArrayList<>();

    public static final int APP_LAUNCHER = 0;
    public static ArrayList<Application> applications = new ArrayList<>();

    public static void start()
    {
        registerApplications();
        launchApplication(APP_LAUNCHER);
    }

    public static <T> int register(ArrayList<T> list, T obj)
    {
        for (int i = 0; i < list.size(); i++)
        {
            if (list.get(i) == null)
            {
                list.set(i, obj);
                return i;
            }
        }
        list.add(obj);
        return list.size() - 1;
    }

    public static <T> int unregister(ArrayList<T> list, int handle)
    {
        if (handle >= 0 && handle < list.size()) list.set(handle, null);
        return HANDLE_NULL;
    }

    public static void registerApplications()
    {
        applications.clear();
        registerApplication(new Launcher());
        registerApplication(new Chat());
        registerApplication(new Phone());
        registerApplication(new Assistant());
        registerApplication(new Notes());
        registerApplication(new Calculator());
        registerApplication(new Trainer());
    }

    public static void unregisterApplications()
    {
        for (int i = 0; i < applications.size(); i++)
        {
            unregisterApplication(i);
        }
    }

    public static int registerApplication(Application app)
    {
        return register(applications, app);
    }

    public static int registerGestureMenu(GestureMenu gmenu)
    {
        return register(menus, gmenu);
    }

    public static int registerActivateGestureMenu(GestureMenu gmenu)
    {
        int hmenu = registerGestureMenu(gmenu);
        activateMenu(hmenu);
        return hmenu;
    }

    public static int registerWebViewEventHandler(WebViewEventHandler wveh)
    {
        return register(webViewEventHandlers, wveh);
    }

    public static int registerTextInputHandler(TextInputHandler tih)
    {
        return register(inputHandlers, tih);
    }

    public static int unregisterApplication(int handle)
    {
        if (handle >= 0 && handle < applications.size())
        {
            if (applications.get(handle) != null)
            {
                applications.get(handle).unregister();
            }
            else
            {
                return HANDLE_NULL;
            }
        }
        return unregister(applications, handle);
    }

    public static int unregisterGestureMenu(int handle)
    {
        return unregister(menus, handle);
    }

    public static int unregisterWebViewEventHandler(int handle)
    {
        return unregister(webViewEventHandlers, handle);
    }

    public static int unregisterTextInputHandler(int handle)
    {
        return unregister(inputHandlers, handle);
    }

    public static GestureMenu getGestureMenu(int handle)
    {
        if (handle < menus.size()) return menus.get(handle);
        return null;
    }

    public static void setGestureMenu(int handle, GestureMenu gmenu)
    {
        if (handle < menus.size())
        {
            if (menus.get(handle) != null) menus.set(handle, gmenu);
        }
    }

    public static void launchApplication(int handle)
    {
        if (handle >= 0 && handle < applications.size())
        {
            if (applications.get(handle) != null) applications.get(handle).start();
        }
    }

    public static void activateMenu(int handle)
    {
        textInput = false;
        currentTextInput = "";
        currentMenu = handle;
        showMenu();
    }

    public static void activateTextInput(int handler)
    {
        currentInputHandler = handler;
        textInput = true;
    }

    public static String numberToCharacter(int i)
    {
        return i + 1 > 0 && i + 1 < 27 ? String.valueOf((char) (i + 65)) : null;
    }

    public static int characterToNumber(String c)
    {
        Log.i("CharToNum", String.valueOf((int) (c.charAt(0) - 97)));
        return c.charAt(0) - 97;
    }

    public static void spellOut(String text)
    {
        for(int i = 0; i < text.length(); i++) speak(String.valueOf(text.charAt(i)));
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
        menus.get(currentMenu).speakMenu();
    }

    public static void selectMenuItem(String letter)
    {
        menus.get(currentMenu).activateOption(letter);
    }

    public static void help()
    {
        speakInterrupt("To read the menu, clench all of your fingers. To activate an option, apply the gesture corresponding to the option's letter. To return to the main menu, apply the Back gesture.");
    }
}
