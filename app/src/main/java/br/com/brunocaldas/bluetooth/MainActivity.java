package br.com.brunocaldas.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ToggleButton ligar;
    Button visibilidade, dispositivos;
    BluetoothAdapter bluetoothAdapter;
    ListView listaDispositivos;
    private List<String> lista;
    private ArrayAdapter<String> dispositivosAdapter;

    static final int HABILITA_BT = 1;
    static final int HABILITA_DESCOBERTA = 2;

    private TextView mTextMessage;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    mTextMessage.setText(R.string.title_home);
                    return true;
                case R.id.navigation_dashboard:
                    mTextMessage.setText(R.string.title_dispositivos);
                    return true;
                case R.id.navigation_notifications:
                    mTextMessage.setText(R.string.title_notifications);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextMessage = (TextView) findViewById(R.id.message);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

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
        lista = new ArrayList<>();
        dispositivosAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

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
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                Toast.makeText(getApplicationContext(), device.getName(), Toast.LENGTH_SHORT).show();


                lista.add(device.getName());
                    dispositivosAdapter.add(device.getName());

                // When discovery is finished, change the Activity title
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

                Integer contagem = dispositivosAdapter.getCount();
                Integer contagemString = lista.size();
                Toast.makeText(getApplicationContext(), contagemString.toString(), Toast.LENGTH_SHORT).show();
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
        listaDispositivos = (ListView) findViewById(R.id.lstDispositivos);
    }

//    private void preencherListView(String nome) {
//        if (nome == null) {
//            pacientes = new PacienteDao(getApplicationContext()).obterTodos();
//        }else{
//            pacientes = new PacienteDao(getApplicationContext()).obterPorNome(nome);
//        }
//
//        List<String> list = new ArrayList<>();
//        if (pacientes != null && !pacientes.isEmpty()) {
//            for(Paciente p: pacientes){
//                list.add(p.getNome());
//            }
//            ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
//                    android.R.layout.simple_list_item_1, list);
//            dataAdapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
//            lstPacientes.setAdapter(dataAdapter);
//        }
//    }

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
