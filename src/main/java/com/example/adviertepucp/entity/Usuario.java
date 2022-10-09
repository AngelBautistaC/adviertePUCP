package com.example.adviertepucp.entity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "usuario")
public class Usuario implements Serializable {
    @Id
    @Size(max = 8)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo", nullable = false, length = 8)
    private String id;

    @Size(max = 45)
    @NotNull
    @Column(name = "nombre", nullable = false, length = 45)
    private String nombre;

    @Size(max = 45)
    @NotNull
    @Column(name = "apellido", nullable = false, length = 45)
    private String apellido;

    @Size(max = 8)
    @Column(name = "dni", length = 8)
    private String dni;

    @Size(max = 9)
    @Column(name = "celular", length = 9)
    private String celular;

    @Size(max = 80)
    @NotNull
    @Column(name = "correo", nullable = false, length = 80)
    private String correo;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "categoria", nullable = false)
    private Categoria categoria;

    @NotNull
    @Column(name = "suspendido", nullable = false)
    private Integer suspendido;

    @Size(max = 64)
    @Column(name = "codigoverificacion", length = 64)
    private String codigoverificacion;

    @Size(max = 256)
    @NotNull
    @Column(name = "pwd", nullable = false, length = 256)
    private String pwd;

    @ManyToOne
    @JoinColumn(name = "foto")
    private Fotoalmacenada foto;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "icono", nullable = false)
    private Icono icono;
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public Integer getSuspendido() {
        return suspendido;
    }

    public void setSuspendido(Integer suspendido) {
        this.suspendido = suspendido;
    }

    public String getCodigoverificacion() {
        return codigoverificacion;
    }

    public void setCodigoverificacion(String codigoverificacion) {
        this.codigoverificacion = codigoverificacion;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public Fotoalmacenada getFoto() {
        return foto;
    }

    public void setFoto(Fotoalmacenada foto) {
        this.foto = foto;
    }

    public Icono getIcono() {
        return icono;
    }

    public void setIcono(Icono icono) {
        this.icono = icono;
    }

}