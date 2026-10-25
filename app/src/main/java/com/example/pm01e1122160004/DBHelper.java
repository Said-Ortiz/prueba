package com.example.pm01e1122160004;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "contactos.db";
    private static final int DB_VERSION = 1;
    private static final String TABLA = "contactos";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLA + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "pais TEXT NOT NULL, "
                + "nombre TEXT NOT NULL, "
                + "telefono TEXT NOT NULL, "
                + "nota TEXT NOT NULL, "
                + "imagen BLOB)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLA);
        onCreate(db);
    }

    private ContentValues valores(Contactos c) {
        ContentValues v = new ContentValues();
        v.put("pais", c.getPais());
        v.put("nombre", c.getNombre());
        v.put("telefono", c.getTelefono());
        v.put("nota", c.getNota());
        v.put("imagen", c.getImagen());
        return v;
    }

    public long insertar(Contactos c) {
        SQLiteDatabase db = getWritableDatabase();
        return db.insert(TABLA, null, valores(c));
    }

    public int actualizar(Contactos c) {
        SQLiteDatabase db = getWritableDatabase();
        return db.update(TABLA, valores(c), "id = ?", new String[]{String.valueOf(c.getId())});
    }

    public int eliminar(int id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLA, "id = ?", new String[]{String.valueOf(id)});
    }

    public Contactos obtener(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cur = db.query(TABLA, null, "id = ?", new String[]{String.valueOf(id)}, null, null, null);
        Contactos c = null;
        if (cur.moveToFirst()) c = desdeCursor(cur);
        cur.close();
        return c;
    }

    public List<Contactos> listar() {
        List<Contactos> lista = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cur = db.query(TABLA, null, null, null, null, null, "nombre COLLATE NOCASE ASC");
        while (cur.moveToNext()) lista.add(desdeCursor(cur));
        cur.close();
        return lista;
    }

    private Contactos desdeCursor(Cursor cur) {
        return new Contactos(
                cur.getInt(cur.getColumnIndexOrThrow("id")),
                cur.getString(cur.getColumnIndexOrThrow("pais")),
                cur.getString(cur.getColumnIndexOrThrow("nombre")),
                cur.getString(cur.getColumnIndexOrThrow("telefono")),
                cur.getString(cur.getColumnIndexOrThrow("nota")),
                cur.getBlob(cur.getColumnIndexOrThrow("imagen")));
    }
}