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

import android.util.Log;

import hack.lorinet.linfinitype.Application;
import hack.lorinet.linfinitype.GestureUI;

public class Calculator extends Application
{
    private int numberInputHandle = HANDLE_NULL;
    private int operationMenuHandle = HANDLE_NULL;

    private float accumulator = 0;
    private char operation = '=';

    public Calculator()
    {
        name = "Calculator";
    }

    @Override
    public void start()
    {
        accumulator = 0;
        operation = '=';
        if(numberInputHandle != HANDLE_NULL) numberInputHandle = GestureUI.unregisterTextInputHandler(numberInputHandle);
        if(operationMenuHandle != HANDLE_NULL) operationMenuHandle = GestureUI.unregisterGestureMenu(operationMenuHandle);
        numberInputHandle = GestureUI.registerTextInputHandler(new GestureUI.TextInputHandler()
        {
            @Override
            public void input(String text)
            {
                String ns = "";
                for(int i = 0; i < text.length(); i++)
                {
                    char ca = text.charAt(i);
                    if(ca == ' ') ns += '.';
                    else ns += (char)(ca - 49);
                }
                float num = Float.parseFloat(ns);
                switch(operation)
                {
                    case '=':
                        accumulator = num;
                        break;
                    case '+':
                        accumulator += num;
                        break;
                    case '-':
                        accumulator -= num;
                        break;
                    case '*':
                        accumulator *= num;
                        break;
                    case '/':
                        accumulator /= num;
                        break;
                }
                readout();
                GestureUI.activateMenuSilent(operationMenuHandle);
                GestureUI.showMenuNoInterrupt();
            }
        });
        operationMenuHandle = GestureUI.registerGestureMenu(new GestureUI.GestureMenu("Operations", new String[]{"Add", "Subtract", "Multiply", "Divide", "Square root"}, new GestureUI.GestureMenu.handler()
        {
            @Override
            public void menuAction(String letter, String option)
            {
                switch(letter)
                {
                    case "a":
                        operation = '+';
                        break;
                    case "b":
                        operation = '-';
                        break;
                    case "c":
                        operation = '*';
                        break;
                    case "d":
                        operation = '/';
                        break;
                    case "e":
                        operation = '=';
                        accumulator = (float) Math.sqrt(accumulator);
                        readout();
                        GestureUI.showMenuNoInterrupt();
                        return;
                }
                GestureUI.speakInterrupt("Type number");
                GestureUI.activateTextInput(numberInputHandle);
            }
        }));
        GestureUI.speakInterrupt("Calculator. Use letters from A to J for digits 0 to 9, and Space as floating point.");
        GestureUI.activateTextInput(numberInputHandle);
    }

    @Override
    public void unregister()
    {

    }

    private void readout()
    {
        GestureUI.speakInterrupt("Result is " + String.valueOf(accumulator));
        Log.i("Result is ", String.valueOf(accumulator));
    }
}
