package com.example.adviertepucp.controller;

import com.example.adviertepucp.dto.UsuariosDBDto;
import com.example.adviertepucp.entity.*;
import com.example.adviertepucp.repository.*;
import com.example.adviertepucp.service.MailService;
import net.bytebuddy.utility.RandomString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/administrador")
public class AdminController extends Usuario {
    private static final String EMAIL_PATTERN = "([A-Za-z0-9-_.]+@[pucp]+(?:\\.[edu]+)+(?:\\.[pe]+)+)";
    private static final Pattern emailPa = Pattern.compile(EMAIL_PATTERN);
    private static final String EMAIL_PATTERN2 = "([A-Za-z0-9-_.]+@[pucp]+(?:\\.[pe]+)+)";
    private static final Pattern emailPa2 = Pattern.compile(EMAIL_PATTERN2);

    private static final String NOMBRE_PATTERN = "^\\pL+[\\pL\\pZ\\pP]{1,45}$";
    private static final Pattern nombrePa = Pattern.compile(NOMBRE_PATTERN);

    private static final String APELLIDO_PATTERN = "\\pL+[\\pL\\pZ\\pP]{1,45}$";
    private static final Pattern apellidoPa = Pattern.compile(APELLIDO_PATTERN);

    private static final String DNI_PATTERN = "^[0-9]{8}$";
    private static final Pattern dniPa = Pattern.compile(DNI_PATTERN);

    private static final String CELULAR_PATTERN = "^[0-9]{9}$";
    private static final Pattern celularPa = Pattern.compile(CELULAR_PATTERN);

    private static final String IMAGE_PATTER = "[^\\\\s]+(.*?)\\\\.(jpg|jpeg|png|JPG|JPEG|PNG)$";
    private static  final Pattern imageVa = Pattern.compile(IMAGE_PATTER);


    @Autowired
    AdmiRepository admiRepository;
    @Autowired
    IncidenciaRepository incidenciaRepository;

    @Autowired
    TipoincidenciaRepository tipoincidenciaRepository;

    @Autowired
    FotoalmacenadaRepository fotoalmacenadaRepository;
    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    CategoriaRepository categoriaRepository;

    @Autowired
    UsuarioBDRepository usuarioBDRepository;

    @Autowired
    MailService mailService;



    @GetMapping("")
    public String listaUsuarios(Model model){
        model.addAttribute("listaUsuarios", admiRepository.listaUsuariosAdmin());
        model.addAttribute("listaUsuarios1", admiRepository.findAll());
        model.addAttribute("usuariosDB", admiRepository.UsuariosDB());
        model.addAttribute("listacategorias", admiRepository.CategoriaList());
        return "admin/listaUsuarios";
    }

    //Perfil
    @GetMapping({"/perfil"})
    public String perfil(HttpSession session)
    {
        Usuario usuario= (Usuario) session.getAttribute("usuariolog");
        return "admin/perfil";
    }



        @GetMapping("/suspenderUser")
    public String suspenderUser(@RequestParam("id") int id) {
        System.out.println("ESTO ES ID: " + id);


            admiRepository.suspenderUsuario(id);
            System.out.println("Se ha suspendido correctamente");

            return "redirect:/administrador/";

    }
    @GetMapping("/activarUser")
    public String activarUser(@RequestParam("id") int id) {
        System.out.println("ESTO ES ID: " + id);

            admiRepository.activarUsuario(id);
            System.out.println("Se ha reactivado correctamente");

            return "redirect:/administrador/";

    }


    @GetMapping("fotoUser/{id2}")
    public ResponseEntity<byte[]>mostrarImagenUser(@PathVariable("id2") int id2){
        Optional<Fotoalmacenada> opt = fotoalmacenadaRepository.findById(id2);
        if (opt.isPresent()){
            Fotoalmacenada f = opt.get();
            byte[] fotoComoByte = f.getFotoalmacenada();
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(
                    MediaType.parseMediaType("image/png"));
            return new ResponseEntity<>(
                    fotoComoByte,
                    httpHeaders,
                    HttpStatus.OK);
        } else {
            return null;
        }
    }




    @GetMapping("/incidencias")
    String listaIncidencias( Model model){

        model.addAttribute("listaTipos",incidenciaRepository.listaTipo());
        return "admin/listaIncidentes";
    }
    @GetMapping("/prueba")

    String pruebas(){
        return "admin/prueba";
    }

    //Para obtener imagenes de la base de datos
    //Para ver al el llamado vayan al archivo listaIncidentes.html
    @GetMapping("/image/{id}")
    public ResponseEntity<byte[]> mostrarImagen(@PathVariable("id") int id) {
        Optional<Tipoincidencia> opt = tipoincidenciaRepository.findById(id);
        if (opt.isPresent()) {
            Tipoincidencia p = opt.get();

            byte[] imagenComoBytes = p.getLogo().getFotoalmacenada();

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(
                    MediaType.parseMediaType("image/png"));

            return new ResponseEntity<>(
                    imagenComoBytes,
                    httpHeaders,
                    HttpStatus.OK);
        } else {
            return null;
        }
    }


    @GetMapping("/crearUser")
    public String crearUser(@ModelAttribute("usuario") Usuario usuario,
                            Model model){
        model.addAttribute("listacategorias", categoriaRepository.findAll());
        return "admin/newUser";
    }

    @PostMapping("/guardarCrearUser")
    public String guardarCrearUsuario(Model model, @RequestParam("id")
                                String codigo,
                                @RequestParam("nombre") @Size(max = 45) @NotNull(message = "Este campo no puede estar nulo")
                                String nombre,
                                @RequestParam("apellido")@Size(max = 45) @NotNull(message = "Este campo no puede estar nulo")
                                String apellido,
                                @RequestParam("dni") @Size(max = 8 , message = "El DNI tiene un máximo de 8 dígitos")
                                String dni,
                                @RequestParam("correo") @Size(max = 80) @NotNull(message = "Este campo no puede estar nulo")
                                String correo,
                                @RequestParam("categoria") @NotNull(message = "Este campo no puede estar nulo")
                                int categoria,
                                @ModelAttribute("usuario") Usuario usuario,
                                RedirectAttributes attr) {

        Usuario usuario1 = new Usuario();
        int flag = 0;
        attr.addFlashAttribute("flashcode",codigo);
        attr.addFlashAttribute("flashname",nombre);
        attr.addFlashAttribute("flashlastname",apellido);
        attr.addFlashAttribute("flashdni",dni);
        attr.addFlashAttribute("flashcorreo",correo);
        attr.addFlashAttribute("fcategoria",categoria);
        System.out.println("PROAABDNO SU LLEGA: " + nombre);
        /* Validación de Nombre */
        Matcher matcher1 = nombrePa.matcher(nombre);
        boolean nombreCompleto = matcher1.matches();
        /* Validación de apellido */
        Matcher matcherap = apellidoPa.matcher(apellido);
        boolean apellidoCompleto1 = matcherap.find();
        /* Validación de DNI */
        Matcher matcherDNI = dniPa.matcher(dni);
        boolean dnivalid1 = matcherDNI.find();
        /* Validación de Correo */
        Matcher matchercorreo = emailPa.matcher(correo);
        boolean email1 = matchercorreo.find();
        Matcher matchercorreo2 = emailPa2.matcher(correo);
        boolean email2 = matchercorreo2.find();

        Usuario usuarioExiste=null;
        Optional<Usuario> userExiste1=usuarioRepository.findById(codigo);
        if (userExiste1.isPresent()){
            attr.addFlashAttribute("usuarioExiste", "El código que está registrando ya existe");
            flag ++;
        }

        if (codigo.length() != 8) {
            attr.addFlashAttribute("msg", "El codigo debe ser de 9 dígitos y con formato numérico");
            flag ++;
        }
        if (!nombreCompleto){
            attr.addFlashAttribute("msg1", "El nombre no respeta el Formato requerido");
            flag ++;
        }
        if (nombre.isEmpty() || nombre.length() > 45){
            attr.addFlashAttribute("msg1", "Debe ingresar un nombre");
            flag ++;
        }
        if (!apellidoCompleto1){
            attr.addFlashAttribute("msg2", "El apellido no respeta el Formato requerido");
            flag ++;
        }
        if (apellido.isEmpty() || apellido.length() > 45){
            attr.addFlashAttribute("msg2", "Debe ingresar un apellido");
            flag ++;
        }
        if (!dnivalid1){
            attr.addFlashAttribute("msg3", "El DNI dede ser de 8 digitos y/o con formato numérico");
            flag ++;
        }
        if (!email1 || !email2) {
            attr.addFlashAttribute("msg5", "Correo debe respetar de formato @pucp.edu.pe o @pucp.pe");
            flag ++;
        }

        if (flag !=0){
            model.addAttribute("listacategorias", categoriaRepository.findAll());
            return "redirect:/administrador/crearUser";
        }
        try {
            int suspendido = 0;
            int habilitado = 0;
            String otp="";

            if (categoria==2){
                otp=RandomString.make(16);
                mailService.otpSeguridad(correo,nombre,codigo,otp);
                otp=new BCryptPasswordEncoder().encode(otp);
                admiRepository.crearSeguridad(codigo,nombre,apellido,dni,correo,categoria, suspendido,otp);
                usuarioBDRepository.crearUsuarioBD(codigo,nombre,apellido,dni,correo);

                /*TODO: A pesar de que sale mensaje de error al momento de crear un usuario, se genera un DataIntegrityViolationException en la consola de Java
                  TODO: y si se crea varias veces a un mismo seguridad, se le envia el correo con la contrseña cambiada.Sin embargo, ya que se envió mensaje de error,
                   TODO: la contraseña sigue siendo la misma generada por primera vez...*/

                //TODO: Cuando se crea un nuevo usuario, cellphone=null

                attr.addFlashAttribute("msg", "Personal de Seguridad registrado exitosamente");
            }
            else{
                attr.addFlashAttribute("msg", "Usuario registrado exitosamente");

                admiRepository.crearUsuario(codigo,nombre,apellido,dni,correo,categoria, suspendido, habilitado);
                usuarioBDRepository.crearUsuarioBD(codigo,nombre,apellido,dni,correo);

            }

            return "redirect:/administrador/";


        }catch (Exception e) {
            e.printStackTrace();
            System.out.println("NO SE PUEDEEE ACTUALIZAAAAAR");
            model.addAttribute("msg", "No se pudo realizar la acción");
            model.addAttribute("listaUsuarios", admiRepository.listaUsuariosAdmin());
            model.addAttribute("listaUsuarios1", admiRepository.findAll());
            model.addAttribute("usuariosDB", admiRepository.UsuariosDB());
            model.addAttribute("listacategorias", admiRepository.CategoriaList());
            return "admin/listaUsuarios";
        }

    }


    @GetMapping("/editUser")
    public String listaUserBD(@ModelAttribute("usuario") Usuario usuario,
                              Model model, @RequestParam("id") int id,
                              RedirectAttributes attr) {
        Optional<Usuario> optUsuario = usuarioRepository.findById(String.valueOf(id));
        System.out.println("LLEGOOOO" + " " +optUsuario);
        if (optUsuario.isPresent()){
            usuario =optUsuario.get();
            Usuario user = optUsuario.get();
            if(user.getCategoria().getId() == 1){
                attr.addFlashAttribute("msg", "No se puede editar a un administrador");
                return "redirect:/administrador/";
            }
            model.addAttribute("usuario", usuario);
            model.addAttribute("listaUsers", usuarioRepository.findAll());
            model.addAttribute("listacategorias", categoriaRepository.findAll());
            return "admin/editar_User";
        }else {
            return "redirect:/administrador/";
        }
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

    @PostMapping("/guardarUser")
    public String editarUsuario(Model model, @RequestParam("id")
                                String codigo,
                                @RequestParam("nombre") @Size(max = 45) @NotNull(message = "Este campo no puede estar nulo")
                                String nombre,
                                @RequestParam("apellido")@Size(max = 45) @NotNull(message = "Este campo no puede estar nulo")
                                String apellido,
                                @RequestParam("dni") @Size(max = 8 , message = "El DNI tiene un máximo de 8 dígitos")
                                String dni,
                                @RequestParam("celular") @Size(max = 9 , message = "El celular tiene un máximo de 9 dígitos")
                                String celular,
                                @RequestParam("correo") @Size(max = 80) @NotNull(message = "Este campo no puede estar nulo")
                                String correo,
                                @RequestParam("categoria") @NotNull(message = "Este campo no puede estar nulo")
                                int categoria,
                                @ModelAttribute("usuario") Usuario usuario,
                                RedirectAttributes attr) {

        System.out.println("PROAABDNO SU LLEGA: " + nombre);



        System.out.println("PROAABDNO SU LLEGA 2122222: " + nombre);
        Usuario beforeusuario=null;
        Optional<Usuario> optuser=usuarioRepository.findById(codigo);
        if (optuser.isPresent()){
            beforeusuario=optuser.get();
        }

        int flag = 0;
        /* Validación de Nombre */
        Matcher matcher = nombrePa.matcher(nombre);
        boolean nombreCompleto = matcher.matches();
        /* Validación de apellido */
        Matcher matcherap = apellidoPa.matcher(apellido);
        boolean apellidoCompleto = matcherap.matches();
        /* Validación de DNI */
        Matcher matcherDNI = dniPa.matcher(dni);
        boolean dnivalid = matcherDNI.find();
        /* Validación de Celular */
        Matcher matcherCelular = celularPa.matcher(celular);
        boolean celularValid = matcherCelular.matches();
        /* Validación de Correo */
        Matcher matchercorreo = emailPa.matcher(correo);
        boolean email = matchercorreo.matches();
        System.out.println("PROAABDNO SU LLEGA 2122222: " + nombre);


        if (!nombreCompleto){
            model.addAttribute("ms1", "El nombre no respeta el Formato requerido");
            flag ++;
        }
        if (nombre.isEmpty() || nombre.length() > 45){
            model.addAttribute("ms1", "Debe ingresar un nombre");
            flag ++;
        }
        if (!apellidoCompleto){
            model.addAttribute("ms2", "El apellido no respeta el Formato requerido");
            flag ++;
        }
        if (apellido.isEmpty() || apellido.length() > 45){
            model.addAttribute("ms2", "Debe ingresar un apellido");
            flag ++;
        }
        if (!dnivalid){
            model.addAttribute("ms3", "El DNI dedbe ser de 8 digitos y/o con formato numérico");
            flag ++;
        }
        if (!celularValid){
            model.addAttribute("ms4", "El Celular debe ser de 9 digitos y/o con formato numérico");
            flag ++;
        }
        if (correo.length() > 80 || !email) {
            model.addAttribute("ms5", "Correo debe respetar el formato @pucp.edu.pe");
            flag ++;
        }

        if (flag !=0){
            System.out.println("LLEGO ERROR??? +  " + flag);
            model.addAttribute("listaUsers", usuarioRepository.findAll());
            model.addAttribute("listacategorias", categoriaRepository.findAll());
            return "admin/editar_User";
        }
        try {
            assert beforeusuario != null;
            int anteriorcat=beforeusuario.getCategoria().getId();
            //El usuario antes era usuarioPUCP y ahora será personal de Seguridad:: suspendido será ==0, habilitado ==1,secret==2,otp==1
            if ( (anteriorcat==3 ||anteriorcat==4 ||anteriorcat==5 || anteriorcat==6) && categoria==2){
                String otp="";

                usuarioBDRepository.actualizarUsuarioBD(nombre,apellido,dni,correo,codigo);

                otp=RandomString.make(16);
                mailService.otpSeguridad(correo,nombre,codigo,otp);
                otp=new BCryptPasswordEncoder().encode(otp);
                admiRepository.usuarioSeguridad(codigo,nombre,apellido,dni,celular,correo,categoria,otp);
                attr.addFlashAttribute("msg", "Usuario actualizado a Seguridad");
                return "redirect:/administrador/";
            }

            //El usuario antes era Seguridad y ahora será usuarioPUCP::
            if ( (anteriorcat==2) ){
                String otp="";

                usuarioBDRepository.actualizarUsuarioBD(nombre,apellido,dni,correo,codigo);
                admiRepository.seguridadUsuario(codigo,nombre,apellido,dni,celular,correo,categoria);
                attr.addFlashAttribute("msg", "Usuario actualizado exitosamente");
                return "redirect:/administrador/";
            }




            attr.addFlashAttribute("msg", "Usuario actualizado exitosamente");

            admiRepository.actualizarUsuario(nombre,apellido,dni,celular,correo,categoria,codigo);
            usuarioBDRepository.actualizarUsuarioBD(nombre,apellido,dni,correo,codigo);
            System.out.println("DETECTANDO ERROROOOOOOOOR");
            System.out.println("asdklasjdlaskjdlaksjda");
            usuarioBDRepository.actualizarUsuarioBD(nombre,apellido,dni,correo,codigo);

            return "redirect:/administrador/";


        }catch (Exception e) {
            e.printStackTrace();

            model.addAttribute("msg", "No se pudo realizar la acción");
            model.addAttribute("listaUsuarios", admiRepository.listaUsuariosAdmin());
            model.addAttribute("listaUsuarios1", admiRepository.findAll());
            model.addAttribute("usuariosDB", admiRepository.UsuariosDB());
            model.addAttribute("listacategorias", admiRepository.CategoriaList());
            return "admin/listaUsuarios";
        }

    }
    @PostMapping("/guardaCrear")
    public String Crear(@RequestParam("nombre") String nombre ,
                           @RequestParam("archivo") MultipartFile logo ,
                           @RequestParam("color") String color ,RedirectAttributes attr, Model model) {
        /* Validación de Correo */
        Matcher matcherImage = emailPa.matcher((CharSequence) logo);
        boolean imagenValid = matcherImage.matches();

        if (logo.isEmpty()) {
            model.addAttribute("msg", "Debe subir un archivo");
            model.addAttribute("listaTipos",incidenciaRepository.listaTipo());
            return "admin/listaIncidentes";
        }
        String nombrelogo=logo.getContentType();
        System.out.println(nombrelogo);
        if (nombrelogo.contains("..")) {
            model.addAttribute("msg", "Debe subir un archivo");
            model.addAttribute("listaTipos",incidenciaRepository.listaTipo());
            return "admin/listaIncidentes";
        }

        if(nombre.isEmpty()){
            model.addAttribute("msg", "El nombre no debe ser vacio");
            model.addAttribute("listaTipos",incidenciaRepository.listaTipo());
            return "admin/listaIncidentes";
        }

        Fotoalmacenada fotoalmacenada = new Fotoalmacenada();
        try {
            fotoalmacenada.setFotoalmacenada(logo.getBytes());
            fotoalmacenada.setTipofoto(logo.getContentType());
            fotoalmacenadaRepository.save(fotoalmacenada);
            System.out.println("he guardado mi imagen");
        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("msg", "Debe subir un archivo");
            model.addAttribute("listaTipos",incidenciaRepository.listaTipo());
            return "admin/listaIncidentes";
        }

        Tipoincidencia tipoincidencia = new Tipoincidencia();
        try {
            tipoincidencia.setNombre(nombre);
            tipoincidencia.setColor(color);
            tipoincidencia.setLogo(fotoalmacenada);
            tipoincidenciaRepository.save(tipoincidencia);
            return "redirect:/administrador/incidencias";
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("No se puede crear ");
            model.addAttribute("msg", "Debe subir un archivo");
            model.addAttribute("listaTipos",incidenciaRepository.listaTipo());
            return "admin/listaIncidentes";
        }



    }


    @PostMapping("/guardarEditar")
    public String editar(  @RequestParam("id") int id,
                                    @RequestParam("nombre") String nombre ,
                                  @RequestParam("archivo") MultipartFile logo ,
                                  @RequestParam("color") String color , RedirectAttributes redirectAttributes, Model model) {
        if (logo.isEmpty()) {
            System.out.println("No recibi la imagen");
            model.addAttribute("msg", "Debe subir un archivo");
            model.addAttribute("listaTipos",incidenciaRepository.listaTipo());
            return "admin/listaIncidentes";
        }
        String nombrelogo=logo.getOriginalFilename();
        if (nombrelogo.contains("..")) {
            model.addAttribute("msg", "El tipo de dato no es el correcto");
            model.addAttribute("listaTipos",incidenciaRepository.listaTipo());
            return "admin/listaIncidentes";
        }
        if(nombre.isEmpty()){
            model.addAttribute("msg", "El nombre no debe ser vacio");
            model.addAttribute("listaTipos",incidenciaRepository.listaTipo());
            return "admin/listaIncidentes";
        }
        Fotoalmacenada fotoalmacenada = new Fotoalmacenada();
        try {
            fotoalmacenada.setFotoalmacenada(logo.getBytes());
            fotoalmacenada.setTipofoto(logo.getContentType());
            fotoalmacenadaRepository.save(fotoalmacenada);

        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("msg", "Debe subir un archivo");
            model.addAttribute("listaTipos",incidenciaRepository.listaTipo());
            return "admin/listaIncidentes";
        }
        Tipoincidencia tipoincidencia;
        if(tipoincidenciaRepository.obtenerTipo(id)!=null){
            tipoincidencia = tipoincidenciaRepository.obtenerTipo(id);
        }else{
            tipoincidencia = new Tipoincidencia();
        }
        try {
            tipoincidencia.setNombre(nombre);
            tipoincidencia.setColor(color);
            tipoincidencia.setLogo(fotoalmacenada);
            tipoincidenciaRepository.save(tipoincidencia);
            return "redirect:/administrador/incidencias";

        }catch (Exception e){
            e.printStackTrace();
            model.addAttribute("msg", "Algun campo esta vacio para editar");
            model.addAttribute("listaTipos",incidenciaRepository.listaTipo());
            return "admin/listaIncidentes";
        }

        }


    @GetMapping("/delete")
    public String borrarTransportista(@RequestParam("id") int id,
                                      RedirectAttributes attr) {

        int cantidadTipo = tipoincidenciaRepository.incidenciaTipo(id);

        if (cantidadTipo==0) {
            tipoincidenciaRepository.deleteById(id);
            attr.addFlashAttribute("msg", "Tipo de incidencia Borrado");
        }else {
            attr.addFlashAttribute("msg","Existe una incidencia con dicho tipo");
        }
        return "redirect:/administrador/incidencias";

    }












}
