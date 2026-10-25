package com.example.pm01e1122160004;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.uth.pm01e1.R;

/** Pantalla 3: marca el número con ACTION_CALL (código de país + número). */
public class LlamarActivity extends AppCompatActivity {

    private static final int REQ_CALL = 100;
    private String numero;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.uth.pm01e1.R.layout.activity_llamar);

        numero = getIntent().getStringExtra("numero");
        String nombre = getIntent().getStringExtra("nombre");

        TextView tvNombre = findViewById(R.id.tvNombre);
        TextView tvNumero = findViewById(R.id.tvNumero);
        Button btnAtras = findViewById(R.id.btnAtras);
        ImageButton btnLlamar = findViewById(R.id.btnLlamar);

        tvNombre.setText(nombre);
        tvNumero.setText(numero);

        btnAtras.setOnClickListener(v -> finish());
        btnLlamar.setOnClickListener(v -> llamar());
    }

    private void llamar() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, REQ_CALL);
            return;
        }
        realizarLlamada();
    }

    private void realizarLlamada() {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + numero));
        try {
            startActivity(callIntent);
        } catch (SecurityException e) {
            Toast.makeText(this, "Permiso de llamada denegado", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_CALL) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                realizarLlamada();
            } else {
                Toast.makeText(this, "Se necesita el permiso para llamar", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
