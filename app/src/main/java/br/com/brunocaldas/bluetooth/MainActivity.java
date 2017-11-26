package br.com.brunocaldas.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity {

    ToggleButton ligar;
    Button visibilidade;
    BluetoothAdapter bluetoothAdapter;
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
                    mTextMessage.setText(R.string.title_dashboard);
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

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Não foi encontrado nenhum adaptador Bluetooth no aparelho", Toast.LENGTH_LONG).show();
        }

        if (bluetoothAdapter.isEnabled()) {
            ligar.setChecked(true);
        }

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
    }

    private void binding() {
        ligar = (ToggleButton) findViewById(R.id.btnLigar);
        visibilidade = (Button) findViewById(R.id.btnVisibilidade);
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
