package hack.lorinet.linfinitype.apps;

import static androidx.core.content.ContextCompat.startActivity;

import static hack.lorinet.linfinitype.GestureUI.HANDLE_NULL;

import android.content.Intent;

import java.util.ArrayList;
import java.util.HashMap;

import hack.lorinet.linfinitype.Application;
import hack.lorinet.linfinitype.GestureUI;

public class Launcher extends Application
{
    private int mainMenuHandle = HANDLE_NULL;
    private HashMap<String, Integer> applicationHandles = new HashMap<String, Integer>();

    public Launcher()
    {
        name = "Launcher";
    }

    @Override
    public void start()
    {
        if(mainMenuHandle != -1) mainMenuHandle = GestureUI.unregisterGestureMenu(mainMenuHandle);
        ArrayList<String> appTitles = new ArrayList<String>();
        for(int i = 1; i < GestureUI.applications.size(); i++)
        {
            applicationHandles.put(GestureUI.applications.get(i).name, i);
            appTitles.add(GestureUI.applications.get(i).name);
        }
        mainMenuHandle = GestureUI.registerGestureMenu(new GestureUI.GestureMenu("Applications", appTitles.toArray(new String[0]), new GestureUI.GestureMenu.handler()
        {
            @Override
            public void menuAction(String letter, String option)
            {
                GestureUI.launchApplication(applicationHandles.get(option));
            }
        }));
        GestureUI.activateMenu(mainMenuHandle);
    }

    @Override
    public void unregister()
    {
        mainMenuHandle = GestureUI.unregisterGestureMenu(mainMenuHandle);
    }
}
