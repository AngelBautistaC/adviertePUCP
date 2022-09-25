package com.example.adviertepucp.service;


import com.example.adviertepucp.entity.Usuario;
import com.example.adviertepucp.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Timer;

import static java.lang.Math.abs;

@Configuration
@EnableAsync
@RequiredArgsConstructor
public class MailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private ServletContext servletContext;

    @Autowired
    UsuarioRepository usuarioRepository;



    /*Consideración:*/
    /*tipoMensaje==1 : Nueva contraseña registro ;; tipoMensaje==2 : Restablecer Contraseña */
    @Async
    public void sendVerificationEmail(Usuario user, String codigoVerificacion, String contextPath,int tipoMensaje)
            throws MessagingException, UnsupportedEncodingException {

        String subject =null;
        String content = null;
        String verifyURL = contextPath +"nuevacontrasena"+ "?token=" + codigoVerificacion;

        if (tipoMensaje==1){
            subject = "Bienvenido a AdviertePUCP, completa tu registro";
            content = "Estimado [[name]],<br>"
                    + "Para completar tu registro en AdviertePUCP, has click en el enlace que se muestra a continuación para poder crear tu contraseña<br>"
                    + "<h3><a style='color:#770077' href="+ "'"+ verifyURL+"'" + " >Crear una contraseña</a></h3><br>"
                    + "Alternativamente, puedes registrarte usando el QR debajo:<br>"
                    + "<img src='"+"https://api.qrserver.com/v1/create-qr-code/?size=150x150&data="+verifyURL+"'>"
                    + "<i><br>Si los links no funcionan, puedes copiar el enlace en tu navegador:<br>"+verifyURL+"</i>"
                    + "<br>Muchas gracias,<br>"
                    + "El equipo de AdviertePUCP.";

        }
        else if (tipoMensaje==2){

            subject = "AdviertePUCP-Cambia tu contraseña";
            content = "Estimado [[name]],<br>"
                    + "Hemos recibido la solicitud para cambiar tu contraseña en AdviertePUCP, has click en el enlace que se muestra a continuación para poder crear tu contraseña<br>"
                    + "<h3><a style='color:#770077' href="+ "'"+ verifyURL+"'" + " >Cambiar mi contraseña</a></h3><br>"
                    + "Alternativamente, puedes registrarte usando el QR debajo:<br>"
                    + "<img src='"+"https://api.qrserver.com/v1/create-qr-code/?size=150x150&data="+verifyURL+"'>"
                    + "<i><br>Si los links no funcionan, puedes copiar el enlace en tu navegador:<br>"+verifyURL+"</i>"
                    + "<br>Muchas gracias,<br>"
                    + "El equipo de AdviertePUCP.<br>"
                    + "<i>Si no solicistaste el cambio de contraseña, ignora este mensaje</i>" ;

        }


        String toAddress = user.getCorreo();
        String fromAddress = "noreply.adviertepucp@gmail.com";
        String senderName = "Equipo de AdviertePUCP";


        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);
        helper.setFrom(fromAddress, senderName);
        helper.setTo(toAddress);
        assert subject != null;
        helper.setSubject(subject);
        content = content.replace("[[name]]", user.getNombre());
        content = content.replace("[[url]]", verifyURL);

        helper.setText(content, true);
        mailSender.send(message);
    }



    public void eliminaToken() {
        usuarioRepository.deleteToken();}

    public int contadorDiezMin(){

        LocalTime localTime = LocalTime.now(ZoneId.of("GMT-5"));

        String timex= String.valueOf(localTime);

        String[] time = timex.split(":", -2);

        return abs(30-Integer.parseInt(time[1])%30);
    }


}
