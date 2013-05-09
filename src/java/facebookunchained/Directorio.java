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
public class Directorio implements Serializable{
    private String nombre;
    private LinkedList<User> usuarios;

    public Directorio(String nombre, LinkedList<User> usuarios) {
        this.nombre = nombre;
        this.usuarios = usuarios;
    }
    
    public Directorio(String nombre) {
        this.nombre = nombre;
        usuarios = new LinkedList<User>();
    }

    public Directorio() {
        usuarios = new LinkedList<User>();
    }
    
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public LinkedList<User> getUsuarios() {
        return usuarios;
    }

    public void setUsuarios(LinkedList<User> usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + (this.nombre != null ? this.nombre.hashCode() : 0);
        hash = 17 * hash + (this.usuarios != null ? this.usuarios.hashCode() : 0);
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
        final Directorio other = (Directorio) obj;

        if ((this.nombre == null) ? (other.nombre != null) : !this.nombre.equals(other.nombre)) {
            return false;
        }
        if (this.usuarios != other.usuarios && (this.usuarios == null || !this.usuarios.equals(other.usuarios))) {
            return false;
        }
        return true;
    }
    
    public User buscarUsuario(String nombre){
        boolean existe = false;
        for(User u : usuarios){
            if(u.getNombre().equals(nombre))
                return u;
        }
                
        return null;
    }
    public User buscarUsuario(User x){
        boolean existe = false;
        for(User u : usuarios){
            if(u.getNombre().equals(x.getNombre()))
                return u;
        }
                
        return null;
    }
    
    public void agregaUsuario(User u){
        usuarios.add(u);
    }
    
    public boolean isEmpty(){
        return usuarios.isEmpty();
    }
    
    
}
