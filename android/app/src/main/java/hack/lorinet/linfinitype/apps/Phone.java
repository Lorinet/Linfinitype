package hack.lorinet.linfinitype.apps;

import static hack.lorinet.linfinitype.GestureUI.HANDLE_NULL;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;

import hack.lorinet.linfinitype.Application;
import hack.lorinet.linfinitype.GestureUI;

public class Phone extends Application
{
    private int mainMenuHandle = HANDLE_NULL;
    private int contactsMenuHandle = HANDLE_NULL;
    private int confirmMenuHandle = HANDLE_NULL;
    private int dialerTextHandler = HANDLE_NULL;

    private String currentPhoneNumber = "";

    public Phone()
    {
        name = "Phone";
        mainMenuHandle = GestureUI.registerGestureMenu(new GestureUI.GestureMenu("Phone", new String[]{"Dialer", "Contacts"}, new GestureUI.GestureMenu.handler()
        {
            @Override
            public void menuAction(String letter, String option)
            {
                switch(option)
                {
                    case "Dialer":
                        GestureUI.speakInterrupt("Use letters A to K for digits 0 to 9.");
                        GestureUI.activateTextInput(dialerTextHandler);
                        break;
                    case "Contacts":
                        GestureUI.activateMenu(contactsMenuHandle);
                        break;
                }
            }
        }));
        contactsMenuHandle = GestureUI.registerGestureMenu(new GestureUI.GestureMenu("You will have to grant Contacts permission.", new String[]{}, new GestureUI.GestureMenu.handler()
        {
            @Override
            public void menuAction(String letter, String option)
            {
                GestureUI.activateMenu(mainMenuHandle);
            }
        }));
        confirmMenuHandle = GestureUI.registerGestureMenu(new GestureUI.GestureMenu("", new String[]{"Call", "Cancel"}, new GestureUI.GestureMenu.handler()
        {
            @Override
            public void menuAction(String letter, String option)
            {
                switch(option)
                {
                    case "Call":
                        call(currentPhoneNumber);
                        currentPhoneNumber = "";
                        GestureUI.activateMenu(mainMenuHandle);
                        GestureUI.speakInterrupt("Calling");
                        break;
                    case "Cancel":
                        currentPhoneNumber = "";
                        GestureUI.activateMenu(mainMenuHandle);
                        break;
                }
            }
        }));
        dialerTextHandler = GestureUI.registerTextInputHandler(new GestureUI.TextInputHandler()
        {
            @Override
            public void input(String text)
            {
                String number = "";
                for(int i = 0; i < text.length(); i++)
                {
                    number += GestureUI.characterToNumber(String.valueOf(text.charAt(i)));
                }
                GestureUI.speakInterrupt(number);
                GestureUI.activateMenu(confirmMenuHandle);
            }
        });
    }

    public static void call(String number)
    {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + number));//change the number
        GestureUI.appContext.startActivity(callIntent);
    }

    @Override
    public void start()
    {
        if (GestureUI.favoriteContacts != null)
        {
            ArrayList<String> al = new ArrayList<>();
            for (String en : GestureUI.favoriteContacts.keySet())
            {
                al.add(en);
            }
            GestureUI.setGestureMenu(contactsMenuHandle, new GestureUI.GestureMenu("Contacts", al.toArray(new String[0]), new GestureUI.GestureMenu.handler()
            {
                @Override
                public void menuAction(String letter, String option)
                {
                    currentPhoneNumber = GestureUI.favoriteContacts.get(option);
                    GestureUI.activateMenu(confirmMenuHandle);
                }
            }));
        }
        GestureUI.activateMenu(mainMenuHandle);
    }

    @Override
    public void unregister()
    {
        mainMenuHandle = GestureUI.unregisterGestureMenu(mainMenuHandle);
        contactsMenuHandle = GestureUI.unregisterGestureMenu(contactsMenuHandle);
        confirmMenuHandle = GestureUI.unregisterGestureMenu(confirmMenuHandle);
        dialerTextHandler = GestureUI.unregisterTextInputHandler(dialerTextHandler);
    }
}
