package hack.lorinet.linfinitype.apps;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;

import hack.lorinet.linfinitype.Application;
import hack.lorinet.linfinitype.GestureUI;

public class Assistant extends Application
{
    public Assistant()
    {
        name = "Assistant";
    }

    @Override
    public void start()
    {
        startActivity(GestureUI.appContext, new Intent(Intent.ACTION_VOICE_COMMAND).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK), null);
    }

    @Override
    public void unregister()
    {

    }
}
