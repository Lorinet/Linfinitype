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

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.annotation.RequiresApi;


import com.google.i18n.phonenumbers.PhoneNumberUtil;

import java.util.ArrayList;
import java.util.Locale;

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
            @RequiresApi(api = Build.VERSION_CODES.M)
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
                currentPhoneNumber = number;
                GestureUI.activateMenu(confirmMenuHandle);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void call(String number)
    {
        TelephonyManager telephony = (TelephonyManager) GestureUI.appContext.getSystemService(Context.TELEPHONY_SERVICE);
        String region = telephony.getNetworkCountryIso().toUpperCase(Locale.getDefault());
        PhoneNumberUtil libphone = PhoneNumberUtil.getInstance();
        int code = libphone.getCountryCodeForRegion(region);
        String finalNum = "";
        if(!number.startsWith("00") && !number.startsWith("+")) finalNum += "+" + String.valueOf(code);
        finalNum += number;
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + finalNum));
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
