/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package facebookunchained;

import java.io.Serializable;
import java.util.LinkedList;

/**
 *
 * @author Juancho
 */
public class User implements Serializable {

    private String nombre;
    private String cola;
    private String diccionario;
    private LinkedList<User> amigos;

    public User(String nombre) {
        this.nombre = nombre;
        amigos = new LinkedList<User>();
    }

    public User(String nombre, String cola, String diccionario) {
        this.nombre = nombre;
        this.cola = cola;
        this.diccionario = diccionario;
        amigos = new LinkedList<User>();
    }

    public User(String nombre, String cola) {
        this.nombre = nombre;
        this.cola = cola;
        amigos = new LinkedList<User>();
    }

    public User(String nombre, String cola, LinkedList<User> amigos) {
        this.nombre = nombre;
        this.cola = cola;
        this.amigos = amigos;
    }

    public User() {
        amigos = new LinkedList<User>();
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCola() {
        return cola;
    }

    public void setCola(String cola) {
        this.cola = cola;
    }

    public String getDiccionario() {
        return diccionario;
    }

    public void setDiccionario(String diccionario) {
        this.diccionario = diccionario;
    }

    public LinkedList<User> getAmigos() {
        return amigos;
    }

    public void setAmigos(LinkedList<User> amigos) {
        this.amigos = amigos;
    }

    public void addAmigo(String nombre, String cola, String directorio) {
        if (this.amigos == null) {
            amigos = new LinkedList<User>();
            amigos.add(new User(nombre, cola, directorio));
        } else {
            if (amigos.isEmpty()) {
                amigos.add(new User(nombre, cola, directorio));
            } else {
                boolean existe = false;
                for (User n : amigos) {
                    if (n.nombre.equals(nombre)) {
                        existe = true;
                    }
                }
                if (!existe) {
                    amigos.add(new User(nombre, cola, directorio));
                }
            }
        }
    }

    public void addAmigo(String nombre, String directorio) {
        if (this.amigos == null) {
            amigos = new LinkedList<User>();
            amigos.add(new User(nombre, "User" + nombre, directorio));
        } else {
            if (amigos.isEmpty()) {
                amigos.add(new User(nombre));
            } else {
                boolean existe = false;
                for (User n : amigos) {
                    if (n.nombre.equals(nombre)) {
                        existe = true;
                    }
                }
                if (!existe) {
                    amigos.add(new User(nombre));
                }
            }
        }
    }

    public void addAmigo(String nombre) {
        if (this.amigos == null) {
            amigos = new LinkedList<User>();
            amigos.add(new User(nombre, "User" + nombre));
        } else {
            if (amigos.isEmpty()) {
                amigos.add(new User(nombre));
            } else {
                boolean existe = false;
                for (User n : amigos) {
                    if (n.nombre.equals(nombre)) {
                        existe = true;
                    }
                }
                if (!existe) {
                    amigos.add(new User(nombre));
                }
            }
        }
    }

    public boolean tieneAmigos() {
        return !amigos.isEmpty();
    }

    @Override
    public String toString() {
        return "User{" + "nombre=" + nombre + ", cola=" + cola + ", diccionario=" + diccionario + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.nombre != null ? this.nombre.hashCode() : 0);
        hash = 89 * hash + (this.cola != null ? this.cola.hashCode() : 0);
        hash = 89 * hash + (this.diccionario != null ? this.diccionario.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final User other = (User) obj;
        if ((this.nombre == null) ? (other.nombre != null) : !this.nombre.equals(other.nombre)) {
            return false;
        }
        if ((this.cola == null) ? (other.cola != null) : !this.cola.equals(other.cola)) {
            return false;
        }
        if ((this.diccionario == null) ? (other.diccionario != null) : !this.diccionario.equals(other.diccionario)) {
            return false;
        }
        return true;
    }
}
