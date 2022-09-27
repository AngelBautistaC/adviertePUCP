package com.example.adviertepucp.repository;

import com.example.adviertepucp.dto.AdminUsuariosDto;
import com.example.adviertepucp.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdmiRepository extends JpaRepository<Usuario, Integer> {
    @Query(value ="SELECT c.idcategoria as id_ca, u.codigo as codigo, concat(u.apellido,' ', u.nombre) as nombres, u.correo as correo,\n" +
            "            u.suspendido as estado,\n" +
            "            c.nombre as rol, f.fotoalmacenada as foto  FROM usuario u \n" +
            "            inner join categoria c on (u.categoria = c.idcategoria)\n" +
            "            inner join fotoalmacenada f on (u.foto=f.idfotoalmacenada)\n" +
            "            order by case when c.nombre = 'administrativo' then 1 else 2 end",
            nativeQuery = true)
    List<AdminUsuariosDto> listaUsuariosAdmin();


}
