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
import static hack.lorinet.linfinitype.GestureUI.activateMenu;

import android.content.SharedPreferences;

import java.util.ArrayList;

import hack.lorinet.linfinitype.Application;
import hack.lorinet.linfinitype.GestureUI;

public class Notes extends Application
{
    private int notesMenuHandle = HANDLE_NULL;
    private int actionMenuHandle = HANDLE_NULL;
    private int titleTextHandler = HANDLE_NULL;
    private int contentTextHandler = HANDLE_NULL;

    private String currentNoteTitle = "";
    private String currentNoteContents = "";

    public Notes()
    {
        name = "Notes";
        notesMenuHandle = GestureUI.registerGestureMenu(new GestureUI.GestureMenu("Notes", new String[]{"New note"}, new GestureUI.GestureMenu.handler()
        {
            @Override
            public void menuAction(String letter, String option)
            {
                switch(letter)
                {
                    case "a":
                        GestureUI.speakInterrupt("Enter title");
                        GestureUI.activateTextInput(titleTextHandler);
                        break;
                    default:
                        String nt = "";
                        for(int i = 0; i < GestureUI.getGestureMenu(notesMenuHandle).options.length; i++)
                        {
                            nt = GestureUI.getGestureMenu(notesMenuHandle).options[i];
                            if(nt.equals(option))
                            {
                                SharedPreferences spn = GestureUI.appContext.getSharedPreferences("notes", 0);
                                currentNoteTitle = nt;
                                currentNoteContents = spn.getString(nt, "Empty");
                                GestureUI.activateMenu(actionMenuHandle);
                            }
                        }
                        break;
                }
            }
        }));
        actionMenuHandle = GestureUI.registerGestureMenu(new GestureUI.GestureMenu("Actions", new String[]{"Read", "Edit", "Delete", "Back"}, new GestureUI.GestureMenu.handler()
        {
            @Override
            public void menuAction(String letter, String option)
            {
                switch(letter)
                {
                    case "a":
                        GestureUI.speakInterrupt(currentNoteContents);
                        break;
                    case "b":
                        GestureUI.speakInterrupt("Enter contents");
                        GestureUI.activateTextInput(contentTextHandler);
                        break;
                    case "c":
                        SharedPreferences spn = GestureUI.appContext.getSharedPreferences("notes", 0);
                        SharedPreferences.Editor spedn = spn.edit();
                        spedn.remove(currentNoteTitle);
                        spedn.apply();
                        currentNoteTitle = "";
                        GestureUI.speakInterrupt("Note deleted");
                        loadNotes();
                        GestureUI.activateMenu(notesMenuHandle);
                        break;
                    case "d":
                        currentNoteTitle = "";
                        loadNotes();
                        GestureUI.activateMenu(notesMenuHandle);
                        break;
                }
            }
        }));
        titleTextHandler = GestureUI.registerTextInputHandler(new GestureUI.TextInputHandler()
        {
            @Override
            public void input(String text)
            {
                currentNoteTitle = text;
                GestureUI.speakInterrupt("Enter contents");
                GestureUI.activateTextInput(contentTextHandler);
            }
        });
        contentTextHandler = GestureUI.registerTextInputHandler(new GestureUI.TextInputHandler()
        {
            @Override
            public void input(String text)
            {
                SharedPreferences spn = GestureUI.appContext.getSharedPreferences("notes", 0);
                SharedPreferences.Editor spedn = spn.edit();
                spedn.putString(currentNoteTitle, text);
                spedn.commit();
                currentNoteTitle = "";
                GestureUI.speakInterrupt("Note saved");
                loadNotes();
                GestureUI.activateMenu(notesMenuHandle);
            }
        });
    }
    @Override
    public void start()
    {
        loadNotes();
        GestureUI.activateMenu(notesMenuHandle);
    }

    @Override
    public void unregister()
    {
        notesMenuHandle = GestureUI.unregisterGestureMenu(notesMenuHandle);
        actionMenuHandle = GestureUI.unregisterGestureMenu(actionMenuHandle);
        titleTextHandler = GestureUI.unregisterTextInputHandler(titleTextHandler);
        contentTextHandler = GestureUI.unregisterTextInputHandler(contentTextHandler);
    }

    private void loadNotes()
    {
        SharedPreferences spn = GestureUI.appContext.getSharedPreferences("notes", 0);
        ArrayList<String> aln = new ArrayList<>();
        aln.add("New note");
        aln.addAll(spn.getAll().keySet());
        GestureUI.getGestureMenu(notesMenuHandle).options = aln.toArray(new String[0]);
    }
}
