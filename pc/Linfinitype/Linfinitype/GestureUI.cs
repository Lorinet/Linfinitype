using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.IO;
using System.IO.Ports;
using Linfinitype.Apps;

namespace Linfinitype
{
    class GestureUI
    {
        public static int HANDLE_NULL = -1;

        public static int DoubleCheckGesture = 256;
        public static int PreviousGesture = 256;
        public static string CurrentTextInput = "";
        public static bool Activated = false;
        public static bool TextInput = false;

        public static int CurrentInputHandler = 0;
        public static List<TextInputHandler> InputHandlers = new List<TextInputHandler>();

        public static int CurrentMenu = 0;
        public static List<GestureMenu> Menus = new List<GestureMenu>();

        public static int APP_LAUNCHER = 0;
        public static List<Application> Applications = new List<Application>();

        public interface TextInputHandler
        {
            void Input(string text);
        }
        public class GestureMenu
        {
            public interface ActionHandler
            {
                void MenuAction(string letter, string option);
            }

            public string Title;
            public string[] Options;
            public ActionHandler? Handler;

            public GestureMenu(string title, string[] options, GestureMenu.ActionHandler handler)
            {
                Title = title;
                Options = options;
                Handler = handler;
            }

            public void ShowMenu()
            {
                Output(Title);
                for(int i = 0; i < Options.Length; i++)
                {
                    Output(NumberToCharacter(i) + " - " + Options[i]);
                }
            }

            public void ActivateOption(string opt)
            {
                int index = CharacterToNumber(opt);
                if(index >= 0 && index < Options.Length)
                {
                    Handler.MenuAction(opt, Options[index]);
                }
            }
        }

        public static string[] GestureTable =
            {
                    "Start", // 00000
                    "Ok", // 10000
                    "?", // 01000
                    " ", // 11000
                    "Delete", // 00100
                    "Exit", // 10100
                    "A", // 01100 0
                    "B", // 11100 1
                    "C", // 00010 2
                    "D", // 10010 3
                    "E", // 01010 4
                    "F", // 11010 5
                    "G", // 00110 6
                    "H", // 10110 7
                    "I", // 01110 8
                    "J", // 11110 9
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

        public static void input(string gesture)
        {
            Log("Gesture", gesture);
            Log("CurrentMenu", CurrentMenu.ToString());
            byte g = 0;
            g ^= (byte)(gesture[0] == '1' ? 1 << 0 : 0);
            g ^= (byte)(gesture[1] == '1' ? 1 << 1 : 0);
            g ^= (byte)(gesture[2] == '1' ? 1 << 2 : 0);
            g ^= (byte)(gesture[3] == '1' ? 1 << 3 : 0);
            g ^= (byte)(gesture[4] == '1' ? 1 << 4 : 0);
            int ge = g & 0xFF;
            if(ge != DoubleCheckGesture)
            {
                PreviousGesture = DoubleCheckGesture;
                DoubleCheckGesture = ge;
            }
            else if(PreviousGesture != DoubleCheckGesture)
            {
                PreviousGesture = ge;
                if (ge == 31) return;
                string gest = GestureTable[ge];
                if(!Activated && gest == "Start")
                {
                    Activated = true;
                    ShowMenu();
                }
                else if(Activated)
                {
                    if(TextInput)
                    {
                        if(gest == "?" || gest == " " || ge > 5)
                        {
                            CurrentTextInput += gest;
                            Output(gest); // speak interrupt
                        }
                        else
                        {
                            switch(gest)
                            {
                                case "Exit":
                                    TextInput = false;
                                    CurrentTextInput = "";
                                    ShowMenu();
                                    break;
                                case "Delete":
                                    CurrentTextInput = CurrentTextInput.Substring(0, CurrentTextInput.Length - 1);
                                    Output(CurrentTextInput); // speak interrupt
                                    break;
                                case "Ok":
                                    InputHandlers[CurrentInputHandler].Input(CurrentTextInput);
                                    CurrentTextInput = "";
                                    break;
                            }
                        }
                    }
                    else
                    {
                        if(ge > 5)
                        {
                            SelectMenuItem(gest);
                        }
                        else
                        {
                            switch(gest)
                            {
                                case "Start":
                                    Activated = false;
                                    break;
                                case "Exit":
                                    LaunchApplication(APP_LAUNCHER);
                                    break;
                                case "?":
                                    ShowMenu();
                                    break;
                            }
                        }
                    }
                }
            }
        }

        public static void Start()
        {
            RegisterApplications();
            LaunchApplication(APP_LAUNCHER);
        }

        public static int Register<T>(List<T> list, T obj)
        {
            list.Add(obj);
            return list.Count - 1;
        }

        public static int Unregister<T>(List<T> list, int handle)
        {
            return HANDLE_NULL;
        }

        public static void RegisterApplications()
        {
            Applications.Clear();
            RegisterApplication(new Launcher());
        }

        public static void UnregisterApplications()
        {
            for(int i = 0; i < Applications.Count; i++)
            {
                UnregisterApplication(i);
            }
        }

        public static int RegisterApplication(Application app)
        {
            return Register(Applications, app);
        }

        public static int RegisterGestureMenu(GestureMenu gmenu)
        {
            return Register(Menus, gmenu);
        }

        public static int RegisterTextInputHandler(TextInputHandler tih)
        {
            return Register(InputHandlers, tih);
        }

        public static int UnregisterApplication(int handle)
        {
            if(handle >= 0 && handle < Applications.Count)
            {
                if (Applications[handle] != null) Applications[handle].Unregister();
                else return HANDLE_NULL;
            }
            return HANDLE_NULL;
        }

        public static int UnregisterGestureMenu(int handle)
        {
            return Unregister(Menus, handle);
        }

        public static int UnregisterTextInputHandler(int handle)
        {
            return Unregister(InputHandlers, handle);
        }

        public static GestureMenu GetGestureMenu(int handle)
        {
            if (handle < Menus.Count) return Menus[handle];
            return null;
        }

        public static void SetGestureMenu(int handle, GestureMenu gmenu)
        {
            if(handle < Menus.Count)
            {
                if (Menus[handle] != null) Menus[handle] = gmenu;
            }
        }

        public static void LaunchApplication(int handle)
        {
            if(handle >= 0 && handle < Applications.Count)
            {
                if (Applications[handle] != null) Applications[handle].Start();
            }
        }

        public static void ActivateMenu(int handle)
        {
            TextInput = false;
            CurrentTextInput = "";
            CurrentMenu = handle;
            ShowMenu();
        }

        public static void ActivateTextInput(int handler)
        {
            CurrentInputHandler = handler;
            TextInput = true;
        }

        public static void ShowMenu()
        {
            Menus[CurrentMenu].ShowMenu();
        }

        public static void SelectMenuItem(string letter)
        {
            Menus[CurrentMenu].ActivateOption(letter);
        }

        public static void Help()
        {
            Output("To read menu, clench all your fingers. To activate an option, apply the gesture corresponding to the option's letter. To return to the main menu, apply the Back gesture.");
        }


        public static string NumberToCharacter(int i)
        {
            return i + 1 > 0 && i + 1 < 27 ? ((char)(i + 65)).ToString() : null;
        }

        public static int CharacterToNumber(string c)
        {
            return c[0] - 65;
        }

        public static void Output(string text)
        {
            Console.WriteLine(text);
        }

        public static void Log(string title, string text)
        {
            using(StreamWriter sw = File.AppendText("linfinitype.log"))
            {
                sw.WriteLine(title + ": " + text);
            }
        }
    }
}
