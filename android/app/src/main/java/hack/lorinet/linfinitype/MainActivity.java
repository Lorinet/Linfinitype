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

package hack.lorinet.linfinitype;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity
{

    private BluetoothAdapter bluetoothAdap = null;
    private Set<BluetoothDevice> bluetoothDevices;
    private ListView devicelist;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bluetoothAdap = BluetoothAdapter.getDefaultAdapter();
        devicelist = findViewById(R.id.devicelist);
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onStart()
    {
        super.onStart();
        if (!bluetoothAdap.isEnabled())
        {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, 3);
        }
        else
        {
            pairedDevices();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 3)
        {
            pairedDevices();
        }
    }

    @SuppressLint("MissingPermission")
    private void pairedDevices()
    {
        bluetoothDevices = bluetoothAdap.getBondedDevices();
        ArrayList list = new ArrayList();

        if (bluetoothDevices.size() > 0)
        {
            for (BluetoothDevice bt : bluetoothDevices)
            {
                list.add(bt.getName() + "\n" + bt.getAddress());
            }
        }
        else
        {
            Toast.makeText(getApplicationContext(), "No paired Bluetooth devices found.", Toast.LENGTH_LONG).show();
        }

        final ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
        devicelist.setAdapter(adapter);

        devicelist.setOnItemClickListener(deviceListListener);
    }

    private AdapterView.OnItemClickListener deviceListListener = new AdapterView.OnItemClickListener()
    {
        public void onItemClick(AdapterView av, View v, int arg2, long arg3)
        {
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            Intent i = new Intent(MainActivity.this, InterfaceView.class);
            i.putExtra("address", address);
            i.putExtra("testMode", "disabled");
            startActivity(i);
        }
    };
}