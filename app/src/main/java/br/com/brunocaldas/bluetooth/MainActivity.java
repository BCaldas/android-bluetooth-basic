package br.com.brunocaldas.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity {

    ToggleButton ligar;
    Button visibilidade, dispositivos;
    BluetoothAdapter bluetoothAdapter;
    ListView listaDispositivosNovos, listaDispositivosPareados;
    ProgressBar bar;
    private ArrayAdapter<String> dispositivosNovosAdapter, dispositivosPareadosAdapter;

    static final int HABILITA_BT = 1;
    static final int HABILITA_DESCOBERTA = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        binding();

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Não foi encontrado nenhum adaptador Bluetooth no aparelho", Toast.LENGTH_LONG).show();
        }

        if (bluetoothAdapter.isEnabled()) {
            ligar.setChecked(true);
        }

        dispositivosNovosAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        dispositivosPareadosAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        listaDispositivosNovos.setAdapter(dispositivosNovosAdapter);
        listaDispositivosPareados.setAdapter(dispositivosPareadosAdapter);

        ligar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (!bluetoothAdapter.isEnabled()) {
                        Intent bluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(bluetoothIntent, HABILITA_BT);
                    }
                } else {
                    if (bluetoothAdapter.isEnabled()) {
                        if (bluetoothAdapter.disable()) {
                            Toast.makeText(getApplicationContext(), "Bluetooth Desligado!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Falha ao desligar o Bluetooth!", Toast.LENGTH_SHORT).show();
                            ligar.setChecked(true);
                        }
                    }
                }
            }
        });

        visibilidade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent bluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                startActivityForResult(bluetoothIntent, HABILITA_DESCOBERTA);
                ligar.setChecked(true);
            }
        });

        dispositivos.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (bluetoothAdapter.isEnabled()) {
                    if (bluetoothAdapter.isDiscovering()) {
                        bluetoothAdapter.cancelDiscovery();
                    }
                    Toast.makeText(getApplicationContext(), "Buscando Dispositivos", Toast.LENGTH_SHORT).show();
                    dispositivosNovosAdapter.clear();
                    dispositivosPareadosAdapter.clear();
                    bar.setVisibility(View.VISIBLE);
                    bluetoothAdapter.startDiscovery();
                } else {
                    Toast.makeText(getApplicationContext(), "O Bluetooth está desligado. Ative-o antes de escanear por dispositivos.", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                try{
                    if(device.getType() != BluetoothDevice.DEVICE_TYPE_UNKNOWN) {
                        if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                            dispositivosPareadosAdapter.add(device.getName());


                        } else{
                            dispositivosNovosAdapter.add(device.getName());
                        }
                    }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.toString(),Toast.LENGTH_LONG).show();
                }

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                bar.setVisibility(View.INVISIBLE);
                Integer contagem = dispositivosNovosAdapter.getCount() + dispositivosPareadosAdapter.getCount();
                Toast.makeText(getApplicationContext(), "A busca finalizou e retornou " + contagem.toString() + " dispositivos", Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    public void onDestroy() {
        unregisterReceiver(mReceiver);

        super.onDestroy();
    }

    private void binding() {
        ligar = (ToggleButton) findViewById(R.id.btnLigar);
        visibilidade = (Button) findViewById(R.id.btnVisibilidade);
        dispositivos = (Button) findViewById(R.id.btnDispositivos);
        listaDispositivosNovos = (ListView) findViewById(R.id.lstDispositivosNovos);
        listaDispositivosPareados = (ListView) findViewById(R.id.lstDispositivosPareados);
        bar = (ProgressBar) findViewById(R.id.progressBar);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == HABILITA_BT) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(getApplicationContext(), "Bluetooth Ligado!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), "Falha ao ativar o Bluetooth!", Toast.LENGTH_SHORT).show();
                ligar.setChecked(false);
            }
        }

        if (requestCode == HABILITA_DESCOBERTA) {
            if (resultCode > 0) {
                Toast.makeText(getApplicationContext(), "O Bluetooth está visível por " + resultCode + " segundos.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "Falha ao tornar o Bluetooth visível!", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
