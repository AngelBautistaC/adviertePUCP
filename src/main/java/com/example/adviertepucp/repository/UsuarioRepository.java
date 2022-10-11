//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.example.adviertepucp.repository;

import com.example.adviertepucp.dto.IncidenciaListadto;
import com.example.adviertepucp.dto.UsuarioEstaCreandoDto;
import com.example.adviertepucp.entity.Favorito;
import com.example.adviertepucp.entity.Fotoalmacenada;
import com.example.adviertepucp.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface UsuarioRepository extends JpaRepository<Usuario, String> {


    @Query(value = "select * from usuario where codigo=?1 and correo=?2 ",nativeQuery = true)
    List<Usuario> validarUsuario(String codigo, String correo);

    public Usuario findByEmail(String correo);

    @Modifying
    @Transactional
    @Query(value="update usuario set codigoverificacion=?1,contadortoken=?2 where codigo=?3",nativeQuery = true)
    void enviarcodigo(String codigoverificacion,int contadortoken ,String codigo);

    /*Verifica token para el registro del usuario==Establecer contraseña*/

    @Query(value = "select * from usuario where codigoverificacion=?1",nativeQuery = true)
    Usuario validarToken(String token);

    @Modifying
    @Transactional
    @Query(value="update usuario set codigoverificacion=null,contadortoken=0 ",nativeQuery = true)
    void deleteToken();


    @Modifying
    @Transactional
    @Query(value="update usuario set codigoverificacion=null where codigo=?1 ",nativeQuery = true)
    void deleteTokenbyId(String id);

    @Modifying
    @Transactional
    @Query(value="update usuario set contadortoken=0 where codigo=?1 ",nativeQuery = true)
    void registroResetearContador(String id);


    @Modifying
    @Transactional
    @Query(value="update usuario set pwd=?1, habilitado=1 where codigo=?2",nativeQuery = true)
    void establecerContrasena(String pwd, String id);

    @Modifying
    @Transactional
    @Query(value="update usuario set pwd=?1 where codigo=?2",nativeQuery = true)
    void reestablecerContrasena(String pwd, String id);


    @Query(value = "select * from usuario where codigo=?1",nativeQuery = true)
    Usuario usuarioExiste(String id);

    @Query(value = "select * from usuario where pwd=?1",nativeQuery = true)
    Usuario contrasenaescorrecta(String id);

    @Query(value = "select * from usuario where correo=?1",nativeQuery = true)
    Usuario oauth2User(String correo);


    @Query (value = "select idincidencia as idI , titulo as titulo , descripcion as descripcion , fecha as fecha , estado as estado , urgencia as urgencia, t.nombre as tincidencia ,t.color as color,latitud as latitud, longitud as longitud ,  z.nombre as zonapucp from incidencia i inner join zonapucp z on (z.idzonapucp=i.zonapucp) inner join tipoincidencia t on (t.idtipoincidencia=i.tipoincidencia)", nativeQuery = true)
    List<IncidenciaListadto> listaIncidencia();

    //Para extraer las fotos de cada incidencia
    @Query(value="select fotoalmacenada from  incidencia i inner join incidenciatienefoto it on (i.idincidencia=it.idincidencia) inner join fotoalmacenada f on (it.idfotoalmacenada=f.idfotoalmacenada) where i.idincidencia=?1;",nativeQuery = true)
    List<Fotoalmacenada> listaFotoIncidencia(int id);

    @Query(nativeQuery = true,
    value = "select f.idinteraccion,f.usuario_codigo,i.idincidencia\n" +
            "from favorito f left join incidencia i on (f.incidencia_idincidencia=i.idincidencia)\n" +
            "where f.usuario_codigo=?1 and f.esfavorito=0 and i.publicado=0;")
    UsuarioEstaCreandoDto obtenerCreandoUsuario(String codigo);

}
