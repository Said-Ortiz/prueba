package com.example.pm01e1122160004;


public class Contactos {
    private int id;
    private String pais;
    private String nombre;
    private String telefono;
    private String nota;
    private byte[] imagen;

    public Contactos() { }

    public Contactos(int id, String pais, String nombre, String telefono, String nota, byte[] imagen) {
        this.id = id;
        this.pais = pais;
        this.nombre = nombre;
        this.telefono = telefono;
        this.nota = nota;
        this.imagen = imagen;
    }

    public Contactos(String pais, String nombre, String telefono, String nota, byte[] imagen) {
        this(0, pais, nombre, telefono, nota, imagen);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getNota() { return nota; }
    public void setNota(String nota) { this.nota = nota; }

    public byte[] getImagen() { return imagen; }
    public void setImagen(byte[] imagen) { this.imagen = imagen; }

    /** Extrae el código de área de "Honduras (504)" -> "504". */
    public String getCodigoPais() {
        if (pais == null) return "";
        int a = pais.indexOf('(');
        int b = pais.indexOf(')');
        if (a >= 0 && b > a) return pais.substring(a + 1, b);
        return "";
    }

    /** Número completo con el signo + y el código de país. */
    public String getNumeroCompleto() {
        return "+" + getCodigoPais() + telefono;
    }
}
