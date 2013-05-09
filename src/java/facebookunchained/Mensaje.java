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
public class Mensaje implements Serializable{

    private User usuario;
    private String mensaje;
    private LinkedList<User> amigos;
    private int tipo;

    public Mensaje(User usuario, String mensaje, LinkedList<User> amigos, int tipo) {
        this.usuario = usuario;
        this.mensaje = mensaje;
        this.amigos = amigos;
        this.tipo = tipo;
    }

    public Mensaje(User usuario, String mensaje, int tipo) {
        this.usuario = usuario;
        this.mensaje = mensaje;
        this.tipo = tipo;
        amigos = new LinkedList<User>();
    }

    public Mensaje(User usuario, int tipo) {
        this.usuario = usuario;
        this.tipo = tipo;
        amigos = new LinkedList<User>();
    }

    public Mensaje(String mensaje) {
        this.mensaje = mensaje;
        amigos = new LinkedList<User>();
    }
    
    public Mensaje() {
        amigos = new LinkedList<User>();
    }

    public User getUsuario() {
        return usuario;
    }

    public void setUsuario(User usuario) {
        this.usuario = usuario;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public LinkedList<User> getAmigos() {
        return amigos;
    }

    public void setAmigos(LinkedList<User> amigos) {
        this.amigos = amigos;
    }

    public int getTipo() {
        return tipo;
    }

    public void setTipo(int tipo) {
        this.tipo = tipo;
    }

    @Override
    public String toString() {
        return "Message{" + "usuario=" + usuario + ", mensaje=" + mensaje + ", tipo=" + tipo + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.usuario != null ? this.usuario.hashCode() : 0);
        hash = 89 * hash + (this.mensaje != null ? this.mensaje.hashCode() : 0);
        hash = 89 * hash + this.tipo;
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
        final Mensaje other = (Mensaje) obj;
        if (this.usuario != other.usuario && (this.usuario == null || !this.usuario.equals(other.usuario))) {
            return false;
        }
        if ((this.mensaje == null) ? (other.mensaje != null) : !this.mensaje.equals(other.mensaje)) {
            return false;
        }
        if (this.tipo != other.tipo) {
            return false;
        }
        return true;
    }
    
    
    
    
    
}
