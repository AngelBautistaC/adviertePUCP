
package com.example.adviertepucp.controller;

import com.example.adviertepucp.entity.Usuario;
import com.example.adviertepucp.repository.UsuarioRepository;
import com.example.adviertepucp.service.MailService;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import net.bytebuddy.utility.RandomString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.samstevens.totp.util.Utils.getDataUriForImage;

@Controller
public class LoguinController {
    private static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,64}$";
    private static final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);



    @Autowired
    MailService mailService;

    @Autowired
    UsuarioRepository usuarioRepository;

    /*El código es número?*/



    /*Pagina principal:Loguin*/
    /*Borrador*/

//    @GetMapping({"loginForm"})
//    public String loginForm(){
//        return "loguin/form";
//    }

    @PostMapping({"logout"})
    public String logout(){
        return "redirect:/loginForm";
    }

    @GetMapping({"suspendido"})
    public String suspendido(HttpSession session){

        Usuario usuario =(Usuario) session.getAttribute("usuariolog");
        if (usuario.getSuspendido()!=3){
            return "redirect:/redirectByRole";
        }
        return "loguin/suspendido";
    }


    @GetMapping({"/redirectByRole"})
    public String redirectByRole(Authentication auth,HttpSession session){
        String rol="";
        for(GrantedAuthority role:auth.getAuthorities()){
            rol=role.getAuthority();
            break;
        }

        Usuario usuario=null;

        Optional<Usuario> optusuario=usuarioRepository.findById(auth.getName());
        if (optusuario.isPresent()){
            usuario= optusuario.get();
            session.setAttribute("usuariolog",usuario);
            session.setAttribute("rol",rol);
        }
        else if  (usuario==null && rol!=null){
            usuario= (Usuario) session.getAttribute("usuariolog");
            session.setAttribute("usuariolog",usuario);
            session.setAttribute("rol",rol);
        }

        if (usuario.getSuspendido()==3){
            return "redirect:/suspendido";
        }


        if(rol.equals("Administrativo")){
            return "redirect:/administrador/";
        }
        if(rol.equals("Seguridad")){
            //Se cambia el rol del seguridad mientras verifica su QR: transitorio::
            Authentication authe = SecurityContextHolder.getContext().getAuthentication();

            List<GrantedAuthority> updatedAuthorities = new ArrayList<>(authe.getAuthorities());
            updatedAuthorities.remove(0);
            updatedAuthorities.add(new SimpleGrantedAuthority("transitorio"));

            Authentication newAuth = new UsernamePasswordAuthenticationToken(authe.getPrincipal(), authe.getCredentials(), updatedAuthorities);

            SecurityContextHolder.getContext().setAuthentication(newAuth);
            return "redirect:/autenticacion";
        }
        if(rol.equals("transitorio")){
            return "redirect:/autenticacion";
        }
        else{
            return "redirect:/usuario/";
        }
    }

    /*2FA AUTH: Google Authenticator*/
    @GetMapping({"autenticacion"})
    public String authenticacion(HttpSession session){

       Usuario rolTransitorio=(Usuario) session.getAttribute("usuariolog");

       //Lógica Nuevo Seguridad
       if (rolTransitorio.getHabilitado()==1 && Objects.equals(rolTransitorio.getSecret(), "2")){
           return "redirect:/qrnuevoseguridad";
       }
       //else
        //TODO: esto va en el POST  de la autentitcacion...
        Authentication authe = SecurityContextHolder.getContext().getAuthentication();

        List<GrantedAuthority> updatedAuthorities = new ArrayList<>(authe.getAuthorities());
        updatedAuthorities.remove(0);
        updatedAuthorities.add(new SimpleGrantedAuthority("Seguridad"));
        Authentication newAuth = new UsernamePasswordAuthenticationToken(authe.getPrincipal(), authe.getCredentials(), updatedAuthorities);
        SecurityContextHolder.getContext().setAuthentication(newAuth);

        return "loguin/autenticacion";
    }

    @GetMapping({"qrnuevoseguridad"})
    public String qrnuevoseguridad(HttpSession session) throws MessagingException, UnsupportedEncodingException {

        Usuario rolTransitorio=(Usuario) session.getAttribute("usuariolog");

        if (Objects.equals(rolTransitorio.getSecret(), "2")){
            //Se Asigna secret al seguridad:
            SecretGenerator secretGenerator = new DefaultSecretGenerator();
            String secret = secretGenerator.generate();
            usuarioRepository.asignarSecret(secret,rolTransitorio.getId());

            QrData data = new QrData.Builder()
                    .label(rolTransitorio.getCorreo())
                    .secret(secret)
                    .issuer("AdviertePUCP")
                    .algorithm(HashingAlgorithm.SHA1)
                    .digits(6)
                    .period(30)
                    .build();
            QrGenerator generator = new ZxingPngQrGenerator();

            byte[] imageData;
            try {
                imageData = generator.generate(data);
            } catch (QrGenerationException e) {
                throw new RuntimeException(e);
            }
            String mimeType = generator.getImageMimeType();

            String dataUri = getDataUriForImage(imageData, mimeType);
            mailService.enviaQRSecreto(rolTransitorio,data.getUri());
            session.setAttribute("dataUri",dataUri );
        }

        if (session.getAttribute("dataUri") == null) {
            return "loguin/autenticacion";
        }


        return "loguin/qrnuevoseguridad";
    }


//    @GetMapping({"/autenticacion"})
//    public String autenticacion()
//    {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
//            return "loguin/autenticacion";
//        }
//
//        return "redirect:/redirectByRole";
//    }







    //localhost:8080

    @GetMapping({"loginForm",""})
    public String index() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return "loguin/loguin";
        }

        return "redirect:/redirectByRole";

    }



    /*Registro de Usuario*/

    @GetMapping({"registro"})
    public String registro()
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return "loguin/registro";
        }

        return "redirect:/redirectByRole";

    }

    @PostMapping("enviaDatosRestablecer")
    public String enviaDatosRestablecer(@RequestParam("id")  String id,
                                        @RequestParam("correo")  String correo, RedirectAttributes attr, Model model, HttpServletRequest httpServletRequest) throws MessagingException, UnsupportedEncodingException {

        attr.addFlashAttribute("id", id);
        attr.addFlashAttribute("correo", correo);

        List<Usuario> usuarioEncontrado=usuarioRepository.validarUsuario(id,correo);
        if (usuarioEncontrado.size()==0){
            attr.addFlashAttribute("fail", "Los datos ingresados no coinciden con el registro de usuarios.");
        } else{
            if(usuarioEncontrado.get(0).getHabilitado()==1){
                if (usuarioEncontrado.get(0).getContadortoken()==3){

                    attr.addFlashAttribute("tresintentos","3 intentos");
                }
                else {
                    int contadortoken=usuarioEncontrado.get(0).getContadortoken()+1;
                    int endIndex=httpServletRequest.getRequestURL().length()-21;
                    String contextpath=httpServletRequest.getRequestURL().substring(0,endIndex);

                    String codigoVerificacion = RandomString.make(64);
                    usuarioRepository.enviarcodigo(codigoVerificacion,contadortoken ,id);
                    mailService.sendVerificationEmail(usuarioEncontrado.get(0), codigoVerificacion,contextpath,2);

                    attr.addFlashAttribute("success", "Datos confirmados correctamente, se ha enviado un correo de confirmación.");
                }
            }
            else{
                attr.addFlashAttribute("already", "Usuario no registrado.");
            }
        }
        return "redirect:/restablecercontrasena";
    }

    @PostMapping("enviaDatosRegistro")
    public String enviaRegistro(@RequestParam("id")  String id,
                                @RequestParam("correo")  String correo, RedirectAttributes attr, Model model, HttpServletRequest httpServletRequest) throws MessagingException, UnsupportedEncodingException {

        attr.addFlashAttribute("id", id);
        attr.addFlashAttribute("correo", correo);

        List<Usuario> usuarioEncontrado=usuarioRepository.validarUsuario(id,correo);
        if (usuarioEncontrado.size()==0){
            attr.addFlashAttribute("fail", "Los datos ingresados no coinciden con el registro de usuarios.");
        } else{
            if(usuarioEncontrado.get(0).getHabilitado()==1){
                attr.addFlashAttribute("already", "Este usuario ya se encuentra registrado en el sistema.");
            }
            else{
                if (usuarioEncontrado.get(0).getContadortoken()>=3){
                    attr.addFlashAttribute("tresintentos","3 intentos");
                }
                else {
                    int contadortoken=usuarioEncontrado.get(0).getContadortoken()+1;


                    int endIndex=httpServletRequest.getRequestURL().length()-18;
                    String contextpath=httpServletRequest.getRequestURL().substring(0,endIndex);

                    String codigoVerificacion = RandomString.make(64);
                    usuarioRepository.enviarcodigo(codigoVerificacion,contadortoken ,id);
                    mailService.sendVerificationEmail(usuarioEncontrado.get(0), codigoVerificacion,contextpath,1);


                    attr.addFlashAttribute("success", "Datos confirmados correctamente, se ha enviado un correo de confirmación.");
                }
            }
        }
        return "redirect:/registro";
    }


    @GetMapping({"nuevacontrasena"})
    public String nuevacontrasena(@RequestParam("token") String token,Model model, RedirectAttributes attr)
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            if (token.length() ==64){
                Usuario usuario=usuarioRepository.validarToken(token);
                if (usuario != null){
                    model.addAttribute("token", token);
                    return "loguin/nuevacontrasena";
                }
            }
            attr.addFlashAttribute("invalidtoken", "Error: token inválido o vencido.");
            return "redirect:/registro";
        }
        return "redirect:/redirectByRole";
    }

    /*Restablecer Contraseña-Post mapping de registro*/

    @GetMapping({"/restablecercontrasena"})
    public String restablececontrasena()
    {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication instanceof AnonymousAuthenticationToken) {
            return "loguin/restablececontrasena";
        }

        return "redirect:/redirectByRole";
    }

    @PostMapping({"/nuevopwd"})
    public String nuevapwd(@RequestParam("token") String token,
                           @RequestParam("pwd") String pwd,
                           @RequestParam("confirmpwd") String confirmpwd,
                           RedirectAttributes attr,
                           HttpServletRequest request) {
        Usuario usuario =usuarioRepository.validarToken(token);
        attr.addFlashAttribute("pwd", pwd);
        attr.addFlashAttribute("confirmpwd", confirmpwd);
        Matcher matcher =pattern.matcher(pwd);
        String referer = request.getHeader("Referer");
        boolean pwdvalido=matcher.matches();

        if (usuario == null){
            attr.addFlashAttribute("invalidtoken", "Error:token inválido o vencido (El token de verificación vence cada media hora).");
            return "redirect:/registro";
        }
        if (!Objects.equals(pwd, confirmpwd)){
            attr.addFlashAttribute("fail", "Los campos de contraseña no coinciden.");
            return "redirect:"+ referer;
        }
        if (!pwdvalido){
            attr.addFlashAttribute("fail", "Comprueba que los campos cumplan las condiciones mínimas.");
            return "redirect:"+ referer;
        }

        String passwd=new BCryptPasswordEncoder().encode(pwd);

        if (usuario.getHabilitado()==0){
            usuarioRepository.establecerContrasena(passwd, usuario.getId());
            usuarioRepository.deleteTokenbyId(usuario.getId());
            usuarioRepository.registroResetearContador(usuario.getId());
            attr.addFlashAttribute("registrado", "Cuanta registrada correctamente, ahora puedes ingresar al sistema.");
        }
        else if (usuario.getHabilitado()==1){
            usuarioRepository.reestablecerContrasena(passwd, usuario.getId());
            usuarioRepository.deleteTokenbyId(usuario.getId());
            usuarioRepository.registroResetearContador(usuario.getId());
            attr.addFlashAttribute("registrado", "Contraseña cambiada correctamente.");
        }
        return "redirect:/";
    }




    @GetMapping({"oauth2/login"})
    public String oauth2Login(OAuth2AuthenticationToken oAuth2AuthenticationToken, HttpSession session,RedirectAttributes attr,Authentication auth) {

        Map<String,Object> currentUser = oAuth2AuthenticationToken.getPrincipal().getAttributes();

        Usuario correoUsuario=usuarioRepository.oauth2User((String) currentUser.get("email"));

        if (correoUsuario==null){
            session.invalidate();
            SecurityContextHolder.clearContext();
            attr.addFlashAttribute("noexiste", "Error: cuenta google ingresada no existe en el registro de usuarios.");
            return "redirect:/loginForm?error";
        }

        Optional<Usuario>oauth2User=usuarioRepository.findById(correoUsuario.getId());

        Usuario usuario=null;


        if(oauth2User.isPresent()){
            usuario = oauth2User.get();
            if ( (Objects.equals(usuario.getCategoria().getNombre(), "Administrativo")) || (Objects.equals(usuario.getCategoria().getNombre(), "Seguridad"))  ){
                attr.addFlashAttribute("CancelLogin", "Error: El inicio de sesión con Google solo es válido para el Usuario PUCP, mas no para el Administrador o personal de Seguridad.");
                session.invalidate();
                SecurityContextHolder.clearContext();
                return "redirect:/loginForm";
            }
            if (usuario.getHabilitado()==0){
                String codigoVerificacion = RandomString.make(64);
                usuarioRepository.enviarcodigo(codigoVerificacion,2,usuario.getId() );
                session.invalidate();
                SecurityContextHolder.clearContext();
                attr.addFlashAttribute("noregistrado","Aún no te has registrado, por favor ingresa tu contraseña para continuar");
                return "redirect:/nuevacontrasena?token="+codigoVerificacion;

            }
            session.setAttribute("usuariolog",usuario);
            return "redirect:/usuario";
        }
        session.invalidate();
        SecurityContextHolder.clearContext();
        return "redirect:/registro";


    }




}
