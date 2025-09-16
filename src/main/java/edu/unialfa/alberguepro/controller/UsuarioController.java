package edu.unialfa.alberguepro.controller;

import edu.unialfa.alberguepro.model.Usuario;
import edu.unialfa.alberguepro.repository.UsuarioRepository;
import edu.unialfa.alberguepro.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/admin/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public String listarUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioRepository.findAll());
        return "admin/usuarios/index"; // -> templates/admin/usuarios/index.html
    }

    @GetMapping("/novo")
    public String novoUsuarioForm(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "admin/usuarios/form"; // -> templates/admin/usuarios/form.html
    }

    @PostMapping("/salvar")
    public String salvarUsuario(@Valid Usuario usuario, BindingResult result, RedirectAttributes attributes) {
        if (result.hasErrors()) {
            return "admin/usuarios/form";
        }
        usuarioService.salvar(usuario);
        attributes.addFlashAttribute("successMessage", "Usuário salvo com sucesso!");
        return "redirect:/admin/usuarios";
    }

    @GetMapping("/editar/{id}")
    public String editarUsuarioForm(@PathVariable("id") Long id, Model model) {
        Optional<Usuario> usuario = usuarioRepository.findById(id);
        if (usuario.isPresent()) {
            model.addAttribute("usuario", usuario.get());
            return "admin/usuarios/form"; // Reutiliza o mesmo formulário de cadastro
        } else {
            return "redirect:/admin/usuarios"; // Redireciona se não encontrar o usuário
        }
    }

        @PostMapping("/excluir/{id}")
    public String excluirUsuario(@PathVariable("id") Long id) {
        usuarioService.excluir(id);
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/toggle/{id}")
    public String toggleAtivo(@PathVariable("id") Long id) {
        usuarioService.toggleAtivo(id);
        return "redirect:/admin/usuarios";
    }
}