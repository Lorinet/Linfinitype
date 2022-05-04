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

import hack.lorinet.linfinitype.Application;
import hack.lorinet.linfinitype.GestureUI;

public class Trainer extends Application
{
    private int mainMenuHandle = HANDLE_NULL;
    private int guideMenuHandle = HANDLE_NULL;
    private int inputHandle = HANDLE_NULL;

    private String helptextAboutGestures = "Gestures are defined by the binary code formed by clenching your fingers in different combinations. There are 32 such combinations, and the purpose of this program is to make you familiar with using and learning them. Stick your middle finger out to repeat any section.";
    private String helptextBinary = "Counting in binary using your fingers is easy, it only needs a bit of practice. Firstly, label each finger with numbers zero to four, starting from the thumb. These labels represent the powers of two that add up to a particular number. For every clenched finger, you raise two to the power represented by that finger, and add these results together. For example, you can form the number 5 by representing it as two to the power of zero plus two to the power of two, so you clench your thumb and middle finger. Once you understand how this works, you will be able to figure out how to count incrementally, and this skill might help you learn the alphabetical gestures faster. We can also take binary numbers directly, like so: one zero one zero zero. From now on, the gestures will be represented like that.";
    private String helptextCharacters = "The first 26 binary codes starting from zero are mapped to the English alphabet, in a slightly modified alphabetical order designed to make the most straining gestures correspond to the less common letters. Letter A is one zero zero zero zero or 1 in binary, letter B is zero one zero zero zero or 2 in binary, and so on, up to Z, which is zero one zero one one, or 26 in binary. The modifications are the following: the gesture that would normally correspond to Q, one zero zero zero one, stands for U, the gesture that would normally go for T, zero zero one zero one, signifies Q, and T is now represented using a different gesture, which is one zero one one one.";
    private String helptextControlGestures = "Beyond alphabetic glyphs, there are a few really important gestures to be learnt. There is a question mark, which can be shown using the following combo: one one zero one one, equivalent to sticking out your middle finger and clenching all the rest. You can use this gesture in any menu to repeat the title and the options, or as a literal question mark symbol in text. The OK gesture, represented with zero one one one one, just like a thumbs-up, is used for confirmation and registration of text input. The Delete gesture, which is zero zero one one one, removes the last character of your typed text string, and reads out the remaining part. Clenching all your fingers, showing the gesture one one one one one one, represents a space character for use in textual input.";
    private String helptextContactGestures = "In addition, there is a pair of contact pads placed between the inner side of your thumb and outer side of your index, and another pair between the top of your index fingertip and the underside of the middle fingertip. By touching the upside of the end of the index using your middle fingertip, you can activate the Exit gesture, which simply exits from the current text input or from the current menu. If you activate the Exit gesture while your thumb is in contact with the proximal part of your index, it becomes the On/Off gesture, which activates or deactivates the device instantly. This system is done to make sure the device is only active when it is actively used, making sure no accidental inputs happen.";
    private String helptextMenusAndInput = "The user interface consists of two major interaction methods: menus and text inputs. Using menus, an application can present you with one or more choices or actions, which can be activated using letter gestures. In a menu, the app reads out the title and all the options preceded by their corresponding letter. When an app requires textual input, it enters text input mode, which pieces together a string from your sequentially applied gestures. If you mistyped a gesture, you can always use the Delete gesture to delete unwanted characters. When you are done typing, use the OK gesture to make the app accept your input, or alternatively the Exit gesture to cancel.";
    private String helptextApps = "The Linfinity experience comes bundled with different apps optimized for eyes-free usage: Chat, Assistant, Phone, Notes, Calculator and Trainer. Following future updates, the application suite will grow, making the Linfinitype even more capable of helping you out in your daily tasks. To practice and learn using this powerful system, choose a difficulty level and try repeating the presented and spelled words using gestures. By successfully finishing all three levels, you are ready to use the Linfinitype in real life!";

    private String[] easyWordlist = { "see", "mud", "lie", "jaw", "tip", "set", "age", "gun", "nun", "fog", "sip", "can", "oil", "joy", "hay", "boy", "law", "god", "jet", "spy", "lip", "rub", "end", "sow", "sea", "inn", "kit", "pay", "win", "pit", "fit", "gas", "job", "dip", "tax", "dry", "lid", "egg", "way", "tie", "rib", "far", "bee", "put", "die", "fix", "kid", "dog", "era", "bed" };
    private String[] mediumWordlist = { "level", "embox", "means", "tough", "touch", "brush", "agree", "range", "elite", "quota", "swear", "mouse", "haunt", "final", "cater", "vague", "pilot", "tooth", "torch", "truck", "arise", "tempt", "tease", "muggy", "laser", "ghost", "worth", "abbey", "pause", "grant", "admit", "equal", "spoil", "lobby", "evoke", "drain", "stuff", "wreck", "reach", "plane", "weave", "metal", "eagle", "enemy", "track", "order", "harsh", "trick", "right", "punch" };
    private String[] hardWordlist = { "girlfriend", "functional", "competence", "fastidious", "permission", "separation", "motorcycle", "photograph", "attractive", "preference", "retirement", "mastermind", "houseplant", "reflection", "multimedia", "unpleasant", "federation", "confession", "insistence", "vegetation", "proportion", "correspond", "goalkeeper", "dictionary", "straighten", "gregarious", "distortion", "conception", "restaurant", "concession", "constraint", "conviction", "television", "reluctance", "convulsion", "regulation", "obligation", "philosophy", "reputation", "memorandum", "litigation", "disappoint", "difference", "chauvinist", "instrument", "acceptance", "basketball", "experiment", "exhibition", "resolution" };

    private String[] trainingQueue;
    private int promptIndex = 0;

    public Trainer()
    {
        name = "Trainer";
        inputHandle = GestureUI.registerTextInputHandler(new GestureUI.TextInputHandler()
        {
            @Override
            public void input(String text)
            {
                if(text.equals(trainingQueue[promptIndex]))
                {
                    GestureUI.speak("Good!");
                    promptIndex += 1;
                    if(promptIndex >= trainingQueue.length)
                    {
                        GestureUI.speak("You completed the selected training set. Good job!");
                        GestureUI.activateMenu(mainMenuHandle);
                        return;
                    }
                }
                else
                {
                    GestureUI.speak("Try again!");
                }
                GestureUI.speak(trainingQueue[promptIndex]);
                GestureUI.spellOut(trainingQueue[promptIndex]);
                GestureUI.activateTextInput(inputHandle);
            }
        });
        mainMenuHandle = GestureUI.registerActivateGestureMenu(new GestureUI.GestureMenu("Learn how to use your Linfinitype!", new String[]{"Instruction guide", "Easy", "Medium", "Hard"}, new GestureUI.GestureMenu.handler()
        {
            @Override
            public void menuAction(String letter, String option)
            {
                switch(letter)
                {
                    case "a":
                        if(guideMenuHandle != HANDLE_NULL) guideMenuHandle = GestureUI.unregisterGestureMenu(guideMenuHandle);
                        guideMenuHandle = GestureUI.registerActivateGestureMenu(new GestureUI.GestureMenu(helptextAboutGestures, new String[]{"Next", "Quit"}, new GestureUI.GestureMenu.handler()
                        {
                            @Override
                            public void menuAction(String letter, String option)
                            {
                                switch(letter)
                                {
                                    case "a":
                                        GestureUI.unregisterGestureMenu(guideMenuHandle);
                                        guideMenuHandle = GestureUI.registerActivateGestureMenu(new GestureUI.GestureMenu(helptextBinary, new String[]{"Next", "Quit"}, new GestureUI.GestureMenu.handler()
                                        {
                                            @Override
                                            public void menuAction(String letter, String option)
                                            {
                                                switch(letter)
                                                {
                                                    case "a":
                                                        GestureUI.unregisterGestureMenu(guideMenuHandle);
                                                        guideMenuHandle = GestureUI.registerActivateGestureMenu(new GestureUI.GestureMenu(helptextCharacters, new String[]{"Next", "Quit"}, new GestureUI.GestureMenu.handler()
                                                        {
                                                            @Override
                                                            public void menuAction(String letter, String option)
                                                            {
                                                                switch(letter)
                                                                {
                                                                    case "a":
                                                                        if(guideMenuHandle != HANDLE_NULL) guideMenuHandle = GestureUI.unregisterGestureMenu(guideMenuHandle);
                                                                        guideMenuHandle = GestureUI.registerActivateGestureMenu(new GestureUI.GestureMenu(helptextControlGestures, new String[]{"Next", "Quit"}, new GestureUI.GestureMenu.handler()
                                                                        {
                                                                            @Override
                                                                            public void menuAction(String letter, String option)
                                                                            {
                                                                                switch(letter)
                                                                                {
                                                                                    case "a":
                                                                                        GestureUI.unregisterGestureMenu(guideMenuHandle);
                                                                                        guideMenuHandle = GestureUI.registerActivateGestureMenu(new GestureUI.GestureMenu(helptextContactGestures, new String[]{"Next", "Quit"}, new GestureUI.GestureMenu.handler()
                                                                                        {
                                                                                            @Override
                                                                                            public void menuAction(String letter, String option)
                                                                                            {
                                                                                                switch(letter)
                                                                                                {
                                                                                                    case "a":
                                                                                                        GestureUI.unregisterGestureMenu(guideMenuHandle);
                                                                                                        guideMenuHandle = GestureUI.registerActivateGestureMenu(new GestureUI.GestureMenu(helptextMenusAndInput, new String[]{"Next", "Quit"}, new GestureUI.GestureMenu.handler()
                                                                                                        {
                                                                                                            @Override
                                                                                                            public void menuAction(String letter, String option)
                                                                                                            {
                                                                                                                switch(letter)
                                                                                                                {
                                                                                                                    case "a":
                                                                                                                        GestureUI.unregisterGestureMenu(guideMenuHandle);
                                                                                                                        guideMenuHandle = GestureUI.registerActivateGestureMenu(new GestureUI.GestureMenu(helptextApps, new String[]{"Quit"}, new GestureUI.GestureMenu.handler()
                                                                                                                        {
                                                                                                                            @Override
                                                                                                                            public void menuAction(String letter, String option)
                                                                                                                            {
                                                                                                                                GestureUI.activateMenu(mainMenuHandle);
                                                                                                                            }
                                                                                                                        }));
                                                                                                                        break;
                                                                                                                    default:
                                                                                                                        GestureUI.activateMenu(mainMenuHandle);
                                                                                                                        return;
                                                                                                                }
                                                                                                            }
                                                                                                        }));
                                                                                                        break;
                                                                                                    default:
                                                                                                        GestureUI.activateMenu(mainMenuHandle);
                                                                                                        return;
                                                                                                }
                                                                                            }
                                                                                        }));
                                                                                        break;
                                                                                    default:
                                                                                        GestureUI.activateMenu(mainMenuHandle);
                                                                                        return;
                                                                                }
                                                                            }
                                                                        }));
                                                                        break;
                                                                    default:
                                                                        GestureUI.activateMenu(mainMenuHandle);
                                                                        return;
                                                                }
                                                            }
                                                        }));
                                                        break;
                                                    default:
                                                        GestureUI.activateMenu(mainMenuHandle);
                                                        return;
                                                }
                                            }
                                        }));
                                        break;
                                    default:
                                        GestureUI.activateMenu(mainMenuHandle);
                                        return;
                                }
                            }
                        }));
                        return;
                    case "b":
                        trainingQueue = easyWordlist;
                        break;
                    case "c":
                        trainingQueue = mediumWordlist;
                        break;
                    case "d":
                        trainingQueue = hardWordlist;
                        break;
                    default:
                        return;
                }
                promptIndex = 0;
                GestureUI.speak("Type the words you hear!");
                GestureUI.speak(trainingQueue[promptIndex]);
                GestureUI.spellOut(trainingQueue[promptIndex]);
                GestureUI.activateTextInput(inputHandle);
            }
        }));
    }

    @Override
    public void start()
    {
        GestureUI.activateMenu(mainMenuHandle);
    }

    @Override
    public void unregister()
    {
        GestureUI.unregisterGestureMenu(mainMenuHandle);
        GestureUI.unregisterGestureMenu(guideMenuHandle);
        GestureUI.unregisterTextInputHandler(inputHandle);
    }
}
