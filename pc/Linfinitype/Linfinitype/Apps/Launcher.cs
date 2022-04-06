using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Linfinitype.Apps
{
    class Launcher : Application
    {
        public int MainMenuHandle = GestureUI.HANDLE_NULL;
        public Dictionary<string, int> ApplicationHandles = new Dictionary<string, int>();

        class LauncherAppMenu : GestureUI.GestureMenu.ActionHandler
        {
            public Launcher SuperRef;

            public LauncherAppMenu(Launcher sr)
            {
                SuperRef = sr;
            }

            public void MenuAction(string letter, string option)
            {
                GestureUI.LaunchApplication(SuperRef.ApplicationHandles[option]);
            }
        }

        public Launcher()
        {
            Name = "Launcher";
        }

        public override void Start()
        {
            if (MainMenuHandle != -1) MainMenuHandle = GestureUI.UnregisterGestureMenu(MainMenuHandle);
            List<string> appTitles = new List<string>();
            for(int i = 1; i < GestureUI.Applications.Count; i++)
            {
                ApplicationHandles.Add(GestureUI.Applications[i].Name, i);
                appTitles.Add(GestureUI.Applications[i].Name);
            }
            MainMenuHandle = GestureUI.RegisterGestureMenu(new GestureUI.GestureMenu("Applications", appTitles.ToArray(), new LauncherAppMenu(this)));
            GestureUI.ActivateMenu(MainMenuHandle);
        }

        public override void Unregister()
        {
            MainMenuHandle = GestureUI.UnregisterGestureMenu(MainMenuHandle);
        }
    }
}
