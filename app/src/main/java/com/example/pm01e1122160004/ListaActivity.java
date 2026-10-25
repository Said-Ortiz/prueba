package com.example.pm01e1122160004;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.uth.pm01e1.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Pantalla 2: lista, buscador, compartir, ver imagen, eliminar, actualizar y llamar. */
public class ListaActivity extends AppCompatActivity {

    private ListView lvContactos;
    private EditText etBuscar;
    private Button btnAtras, btnCompartir, btnVerImagen, btnEliminar, btnActualizar;

    private DBHelper db;
    private ContactoAdapter adapter;
    private Contactos seleccionado = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.uth.pm01e1.R.layout.activity_lista);

        db = new DBHelper(this);

        lvContactos = findViewById(R.id.lvContactos);
        etBuscar = findViewById(R.id.etBuscar);
        btnAtras = findViewById(R.id.btnAtras);
        btnCompartir = findViewById(R.id.btnCompartir);
        btnVerImagen = findViewById(R.id.btnVerImagen);
        btnEliminar = findViewById(R.id.btnEliminar);
        btnActualizar = findViewById(R.id.btnActualizar);

        adapter = new ContactoAdapter();
        lvContactos.setAdapter(adapter);

        lvContactos.setOnItemClickListener((parent, view, position, id) -> {
            seleccionado = adapter.getItem(position);
            adapter.notifyDataSetChanged();
            confirmarLlamada(seleccionado);
        });

        etBuscar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {
                adapter.filtrar(s.toString());
            }
            @Override public void afterTextChanged(Editable s) { }
        });

        btnAtras.setOnClickListener(v -> finish());
        btnCompartir.setOnClickListener(v -> compartir());
        btnVerImagen.setOnClickListener(v -> verImagen());
        btnEliminar.setOnClickListener(v -> eliminar());
        btnActualizar.setOnClickListener(v -> actualizar());
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarLista();
    }

    private void cargarLista() {
        adapter.setDatos(db.listar());
        adapter.filtrar(etBuscar.getText().toString());
        seleccionado = null;
        adapter.notifyDataSetChanged();
    }

    private boolean haySeleccion() {
        if (seleccionado == null) {
            Toast.makeText(this, "Seleccione un contacto de la lista", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void confirmarLlamada(Contactos c) {
        new AlertDialog.Builder(this)
                .setTitle("Acción")
                .setMessage("¿Desea llamar a " + c.getNombre() + "?")
                .setNegativeButton("No", null)
                .setPositiveButton("Sí", (d, w) -> {
                    Intent i = new Intent(ListaActivity.this, LlamarActivity.class);
                    i.putExtra("nombre", c.getNombre());
                    i.putExtra("numero", c.getNumeroCompleto());
                    startActivity(i);
                })
                .show();
    }

    private void compartir() {
        if (!haySeleccion()) return;
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_TEXT,
                "Nombre: " + seleccionado.getNombre() + "\nTeléfono: " + seleccionado.getNumeroCompleto());
        startActivity(Intent.createChooser(send, "Compartir contacto"));
    }

    private void verImagen() {
        if (!haySeleccion()) return;
        byte[] img = seleccionado.getImagen();
        if (img == null || img.length == 0) {
            Toast.makeText(this, "Este contacto no tiene imagen", Toast.LENGTH_SHORT).show();
            return;
        }
        Bitmap bmp = BitmapFactory.decodeByteArray(img, 0, img.length);
        ImageView iv = new ImageView(this);
        iv.setImageBitmap(bmp);
        iv.setAdjustViewBounds(true);
        iv.setPadding(24, 24, 24, 24);
        new AlertDialog.Builder(this)
                .setTitle(seleccionado.getNombre())
                .setView(iv)
                .setPositiveButton("Cerrar", null)
                .show();
    }

    private void eliminar() {
        if (!haySeleccion()) return;
        new AlertDialog.Builder(this)
                .setTitle("Eliminar contacto")
                .setMessage("¿Desea eliminar a " + seleccionado.getNombre() + "?")
                .setNegativeButton("No", null)
                .setPositiveButton("Sí", (d, w) -> {
                    db.eliminar(seleccionado.getId());
                    Toast.makeText(this, "Contacto eliminado", Toast.LENGTH_SHORT).show();
                    cargarLista();
                })
                .show();
    }

    private void actualizar() {
        if (!haySeleccion()) return;
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("id", seleccionado.getId());
        startActivity(i);
    }

    // ---------- Adaptador con filtro de búsqueda ----------
    private class ContactoAdapter extends BaseAdapter {
        private List<Contactos> todos = new ArrayList<>();
        private List<Contactos> visibles = new ArrayList<>();

        void setDatos(List<Contactos> datos) { todos = datos; visibles = new ArrayList<>(datos); }

        void filtrar(String texto) {
            String q = texto.trim().toLowerCase(Locale.getDefault());
            visibles = new ArrayList<>();
            for (Contactos c : todos) {
                if (q.isEmpty() || c.getNombre().toLowerCase(Locale.getDefault()).contains(q)
                        || c.getTelefono().contains(q)) {
                    visibles.add(c);
                }
            }
            notifyDataSetChanged();
        }

        @Override public int getCount() { return visibles.size(); }
        @Override public Contactos getItem(int position) { return visibles.get(position); }
        @Override public long getItemId(int position) { return visibles.get(position).getId(); }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(ListaActivity.this)
                        .inflate(R.layout.item_contacto, parent, false);
            }
            Contactos c = getItem(position);
            TextView tv = convertView.findViewById(R.id.tvContacto);
            tv.setText(c.getNombre() + " | " + c.getCodigoPais() + c.getTelefono());
            boolean sel = seleccionado != null && seleccionado.getId() == c.getId();
            convertView.setBackgroundColor(sel ? Color.parseColor("#90B8F0")
                    : (position % 2 == 0 ? Color.parseColor("#FFFFFF") : Color.parseColor("#E3EEFB")));
            return convertView;
        }
    }
}
