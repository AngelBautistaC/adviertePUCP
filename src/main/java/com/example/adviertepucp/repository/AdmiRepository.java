package com.example.adviertepucp.repository;

import com.example.adviertepucp.dto.AdminUsuariosDto;
import com.example.adviertepucp.dto.CategoriaDto;
import com.example.adviertepucp.dto.UsuariosDBDto;
import com.example.adviertepucp.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface AdmiRepository extends JpaRepository<Usuario, Integer> {
    @Query(value ="SELECT c.idcategoria as id_ca, u.codigo as codigo, concat(u.apellido,' ', u.nombre) as nombres, u.correo as correo,\n" +
            "                u.suspendido as estado,\n" +
            "                u.habilitado as registrado,\n" +
            "               c.nombre as rol, f.idfotoalmacenada as foto  FROM usuario u \n" +
            "                      inner join categoria c on (u.categoria = c.idcategoria)\n" +
            "                     inner join fotoalmacenada f on (u.foto=f.idfotoalmacenada)\n" +
            "                     order by case when c.nombre = 'administrativo' then 1 else 2 end",
            nativeQuery = true)
    List<AdminUsuariosDto> listaUsuariosAdmin();

    @Query(value = "select codigo from usuario where codigo = ?1",
            nativeQuery = true)
    String selectionCodigo(String codigo);

    @Query(value ="SELECT codigo, nombre, apellido, dni, correo FROM usuariobd;",
            nativeQuery = true)
    List<UsuariosDBDto> UsuariosDB();

    @Query(value = "SELECT c.nombre as categoria, u.categoria as idCategoria FROM usuario u\n" +
            "inner join categoria c on (c.idcategoria=u.categoria);",
            nativeQuery = true)
    List<CategoriaDto> CategoriaList();
    @Transactional
    @Modifying
    @Query(nativeQuery = true,
            value = "UPDATE `adviertedb`.`usuario` SET `suspendido` = 3 WHERE (`codigo` = ?1);")
    void suspenderUsuario(Integer id_codigo);
    @Transactional
    @Modifying
    @Query(nativeQuery = true,
            value = "UPDATE `adviertedb`.`usuario` SET `suspendido` = 0 WHERE (`codigo` = ?1);")
    void activarUsuario(Integer id_codigo);

    @Transactional
    @Modifying
    @Query(nativeQuery = true,
            value = "UPDATE usuario SET nombre = ?1, apellido = ?2, dni = ?3,\n" +
                    " celular = ?4, correo = ?5, categoria = ?6 WHERE (`codigo` = ?7);\n")
    void actualizarUsuario(String nombre, String apellido, String dni,
                            String celular, String email, int categoria, String id);
    @Transactional
    @Modifying
    @Query(nativeQuery = true,
            value = "update usuario set codigo = ?1 where codigo = '20175557'")
    void actualizarCodigo(String id);

    @Modifying
    @Transactional
    @Query(nativeQuery = true,
            value = "insert into usuario (codigo, nombre, apellido, dni, correo, categoria, suspendido, habilitado)" +
                    " values (?1, ?2, ?3, ?4, ?5,?6, ?7, ?8);")
    void crearUsuario(String codigo, String nombre, String apellido, String dni, String correo, int categoria,
                        int suspendido, int habilitado);


    @Modifying
    @Transactional
    @Query(nativeQuery = true,
            value = "insert into usuario (codigo, nombre, apellido, dni, correo, categoria, suspendido, habilitado,otp,secret,pwd)" +
                    " values (?1, ?2, ?3, ?4, ?5,?6, ?7, 1,1,2,?8);")
    void crearSeguridad(String codigo, String nombre, String apellido, String dni, String correo, int categoria,
                      int suspendido, String otp);

}
