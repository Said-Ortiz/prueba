package com.example.pm01e1122160004;



import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.uth.pm01e1.R;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.regex.Pattern;

/** Pantalla 1: registro (y edición) de contactos. */
public class MainActivity extends AppCompatActivity {

    private static final Pattern REGEX_NOMBRE = Pattern.compile("^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ ]+$");
    private static final Pattern REGEX_TELEFONO = Pattern.compile("^[0-9]+$");

    private Spinner spPais;
    private EditText etNombre, etTelefono, etNota;
    private ImageView ivFoto;
    private ImageButton btnAgregarFoto;
    private Button btnGuardar, btnContactos;

    private DBHelper db;
    private byte[] imagenBytes = null;
    private int editId = -1;

    private ActivityResultLauncher<Intent> camaraLauncher;
    private ActivityResultLauncher<Intent> galeriaLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DBHelper(this);

        spPais = findViewById(R.id.spPais);
        etNombre = findViewById(R.id.etNombre);
        etTelefono = findViewById(R.id.etTelefono);
        etNota = findViewById(R.id.etNota);
        ivFoto = findViewById(R.id.ivFoto);
        btnAgregarFoto = findViewById(R.id.btnAgregarFoto);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnContactos = findViewById(R.id.btnContactos);

        spPais.setAdapter(ArrayAdapter.createFromResource(this, R.array.paises,
                android.R.layout.simple_spinner_dropdown_item));

        configurarFiltros();
        configurarLaunchers();

        btnAgregarFoto.setOnClickListener(v -> elegirOrigenImagen());
        ivFoto.setOnClickListener(v -> elegirOrigenImagen());
        btnGuardar.setOnClickListener(v -> guardar());
        btnContactos.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ListaActivity.class)));

        editId = getIntent().getIntExtra("id", -1);
        if (editId >= 0) cargarParaEditar(editId);
    }

    /** Expresiones regulares aplicadas mientras se escribe (limitan la entrada). */
    private void configurarFiltros() {
        InputFilter soloLetras = (source, start, end, dest, dstart, dend) -> {
            for (int i = start; i < end; i++) {
                if (!REGEX_NOMBRE.matcher(String.valueOf(source.charAt(i))).matches()) return "";
            }
            return null;
        };
        InputFilter soloDigitos = (source, start, end, dest, dstart, dend) -> {
            for (int i = start; i < end; i++) {
                if (!REGEX_TELEFONO.matcher(String.valueOf(source.charAt(i))).matches()) return "";
            }
            return null;
        };
        etNombre.setFilters(new InputFilter[]{soloLetras, new InputFilter.LengthFilter(50)});
        etTelefono.setFilters(new InputFilter[]{soloDigitos, new InputFilter.LengthFilter(15)});
    }

    private void configurarLaunchers() {
        camaraLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null
                    && result.getData().getExtras() != null) {
                Bitmap bmp = (Bitmap) result.getData().getExtras().get("data");
                if (bmp != null) establecerImagen(bmp);
            }
        });

        galeriaLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                Uri uri = result.getData().getData();
                if (uri == null) return;
                try (InputStream is = getContentResolver().openInputStream(uri)) {
                    Bitmap bmp = BitmapFactory.decodeStream(is);
                    if (bmp != null) establecerImagen(bmp);
                } catch (Exception e) {
                    Toast.makeText(this, "No se pudo leer la imagen", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void elegirOrigenImagen() {
        new AlertDialog.Builder(this)
                .setTitle("Imagen del contacto")
                .setItems(new String[]{"Tomar foto", "Elegir de la galería"}, (d, which) -> {
                    try {
                        if (which == 0) {
                            camaraLauncher.launch(new Intent(MediaStore.ACTION_IMAGE_CAPTURE));
                        } else {
                            galeriaLauncher.launch(new Intent(Intent.ACTION_PICK,
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
                        }
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(this, "No hay aplicación disponible", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    /** Reduce la imagen, la muestra y la convierte a bytes para guardarla en SQLite. */
    private void establecerImagen(Bitmap original) {
        int max = 480;
        int w = original.getWidth(), h = original.getHeight();
        float escala = Math.min(1f, (float) max / Math.max(w, h));
        Bitmap bmp = escala < 1f
                ? Bitmap.createScaledBitmap(original, Math.round(w * escala), Math.round(h * escala), true)
                : original;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        imagenBytes = baos.toByteArray();
        ivFoto.setImageBitmap(bmp);
    }

    private void alerta(String mensaje) {
        new AlertDialog.Builder(this)
                .setTitle("Alerta")
                .setMessage(mensaje)
                .setPositiveButton("Aceptar", null)
                .show();
    }

    private void guardar() {
        String nombre = etNombre.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String nota = etNota.getText().toString().trim();
        String pais = spPais.getSelectedItem().toString();

        if (nombre.isEmpty()) { alerta("Debe escribir un nombre"); etNombre.requestFocus(); return; }
        if (!REGEX_NOMBRE.matcher(nombre).matches()) { alerta("El nombre solo puede contener letras"); return; }
        if (telefono.isEmpty()) { alerta("Debe escribir un teléfono"); etTelefono.requestFocus(); return; }
        if (!REGEX_TELEFONO.matcher(telefono).matches()) { alerta("El teléfono solo puede contener dígitos"); return; }
        if (telefono.length() < 7) { alerta("El teléfono debe tener al menos 7 dígitos"); return; }
        if (nota.isEmpty()) { alerta("Debe escribir una nota"); etNota.requestFocus(); return; }

        Contactos c = new Contactos(pais, nombre, telefono, nota, imagenBytes);

        if (editId >= 0) {
            c.setId(editId);
            if (db.actualizar(c) > 0) {
                Toast.makeText(this, "Contacto actualizado", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                alerta("No se pudo actualizar el contacto");
            }
        } else {
            if (db.insertar(c) > 0) {
                Toast.makeText(this, "Contacto guardado", Toast.LENGTH_SHORT).show();
                limpiar();
            } else {
                alerta("No se pudo guardar el contacto");
            }
        }
    }

    private void limpiar() {
        etNombre.setText("");
        etTelefono.setText("");
        etNota.setText("");
        spPais.setSelection(0);
        imagenBytes = null;
        ivFoto.setImageResource(R.drawable.ic_person);
        etNombre.requestFocus();
    }

    private void cargarParaEditar(int id) {
        Contactos c = db.obtener(id);
        if (c == null) { finish(); return; }
        ArrayAdapter<?> adapter = (ArrayAdapter<?>) spPais.getAdapter();
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).toString().equals(c.getPais())) { spPais.setSelection(i); break; }
        }
        etNombre.setText(c.getNombre());
        etTelefono.setText(c.getTelefono());
        etNota.setText(c.getNota());
        imagenBytes = c.getImagen();
        if (imagenBytes != null) {
            ivFoto.setImageBitmap(BitmapFactory.decodeByteArray(imagenBytes, 0, imagenBytes.length));
        }
        btnGuardar.setText("Actualizar Contacto");
        btnContactos.setVisibility(View.GONE);
    }
}