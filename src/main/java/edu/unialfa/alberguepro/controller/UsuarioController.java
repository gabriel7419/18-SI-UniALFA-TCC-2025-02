package edu.unialfa.alberguepro.controller;

import edu.unialfa.alberguepro.dto.UsuarioDTO;
import edu.unialfa.alberguepro.model.Usuario;
import edu.unialfa.alberguepro.service.UsuarioService;
import edu.unialfa.alberguepro.service.RelatorioUsuarioPatrimonioService;

import net.sf.jasperreports.engine.JRException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import edu.unialfa.alberguepro.repository.UsuarioRepository;

import jakarta.validation.Valid;

import java.io.ByteArrayInputStream;
import java.util.Optional;

@Controller
@RequestMapping("/admin/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private RelatorioUsuarioPatrimonioService relatorioService;

    @GetMapping
    public String listarUsuarios(Model model,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "15") int size,
                                 @RequestParam(defaultValue = "username") String sort,
                                 @RequestParam(defaultValue = "asc") String dir) {
        // Criar ordenação
        org.springframework.data.domain.Sort.Direction direction = dir.equals("desc") ? 
            org.springframework.data.domain.Sort.Direction.DESC : org.springframework.data.domain.Sort.Direction.ASC;
        org.springframework.data.domain.Sort sortObj = org.springframework.data.domain.Sort.by(direction, sort);
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sortObj);
        org.springframework.data.domain.Page<UsuarioDTO> pageResult = usuarioService.findAllDTOPaginado(pageable);
        
        model.addAttribute("usuarios", pageResult.getContent());
        model.addAttribute("page", pageResult);
        model.addAttribute("size", size);
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);
        
        // Adicionar informação do usuário logado
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String usernameLogado = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        model.addAttribute("usernameLogado", usernameLogado);
        model.addAttribute("isAdmin", isAdmin);
        
        return "admin/usuarios/index"; // -> templates/admin/usuarios/index.html
    }

    @GetMapping("/novo")
    public String novoUsuarioForm(Model model) {
        Usuario u = new Usuario();
        u.setRole("USER"); // pré-seleciona "Usuário"
        model.addAttribute("usuario", u);
        return "admin/usuarios/form"; // -> templates/admin/usuarios/form.html
    }

    @PostMapping("/salvar")
    public String salvarUsuario(@Valid Usuario usuario, BindingResult result, Model model) {
        // Validações adicionais
        if (usuario.getUsername() == null || usuario.getUsername().trim().isEmpty()) {
            result.rejectValue("username", "campo.obrigatorio", "O nome de usuário é obrigatório.");
        } else if (usuario.getUsername().length() < 4 || usuario.getUsername().length() > 50) {
            result.rejectValue("username", "tamanho.invalido", "O nome de usuário deve ter entre 4 e 50 caracteres.");
        } else if (!usuario.getUsername().matches("^[A-Za-z0-9_.]+$")) {
            result.rejectValue("username", "formato.invalido", "O nome de usuário deve conter apenas letras, números, ponto (.) e sublinhado (_).");
        } else if (!usuarioService.isUsernameUnique(usuario.getUsername(), usuario.getId())) {
            result.rejectValue("username", "error.usuario", "Este nome de usuário já está em uso.");
        }

        // Validação de senha para novos usuários
        if (usuario.getId() == null) {
            if (usuario.getPassword() == null || usuario.getPassword().trim().isEmpty()) {
                result.rejectValue("password", "campo.obrigatorio", "A senha é obrigatória.");
            } else if (usuario.getPassword().length() < 8) {
                result.rejectValue("password", "tamanho.invalido", "A senha deve ter no mínimo 8 caracteres.");
            }
        }

        // Validação de role
        if (usuario.getRole() == null || usuario.getRole().trim().isEmpty()) {
            result.rejectValue("role", "campo.obrigatorio", "O tipo de usuário é obrigatório.");
        }

        if (result.hasErrors()) {
            model.addAttribute("errorMessage", "Há problemas em um dos campos preenchidos, verifique e corrija.");
            return "admin/usuarios/form";
        }
        usuarioService.salvar(usuario);
        return "redirect:/admin/usuarios";
    }

    @GetMapping("/editar/{id}")
    public String editarUsuarioForm(@PathVariable("id") Long id, Model model) {
        Optional<UsuarioDTO> usuario = usuarioService.findByIdDTO(id);
        if (usuario.isPresent()) {
            model.addAttribute("usuario", usuario.get());
            return "admin/usuarios/form"; // Reutiliza o mesmo formulário de cadastro
        } else {
            return "redirect:/admin/usuarios"; // Redireciona se não encontrar o usuário
        }
    }

    @PostMapping("/excluir/{id}")
    public String excluirUsuario(@PathVariable("id") Long id, RedirectAttributes attributes) {
        try {
            usuarioService.excluir(id);
            attributes.addFlashAttribute("successMessage", "Usuário excluído com sucesso!");
        } catch (Exception e) {
            attributes.addFlashAttribute("errorMessage", "Erro ao excluir usuário: " + e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/toggle/{id}")
    public String toggleAtivo(@PathVariable("id") Long id, RedirectAttributes attributes) {
        try {
            usuarioService.toggleAtivo(id);
            attributes.addFlashAttribute("successMessage", "Status do usuário alterado com sucesso!");
        } catch (Exception e) {
            attributes.addFlashAttribute("errorMessage", "Erro ao alterar status: " + e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    @GetMapping("/alterar-senha/{id}")
    public String alterarSenhaForm(@PathVariable("id") Long id, Model model) {
        Optional<UsuarioDTO> usuario = usuarioService.findByIdDTO(id);
        if (usuario.isPresent()) {
            model.addAttribute("usuario", usuario.get());
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            Long selfId = usuarioRepository.findByUsername(auth.getName()).map(u -> u.getId()).orElse(null);
            boolean requireCurrentPassword = !(isAdmin && selfId != null && !selfId.equals(id));
            model.addAttribute("requireCurrentPassword", requireCurrentPassword);
            return "admin/usuarios/alterar-senha";
        }
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/alterar-senha/{id}")
    public String alterarSenha(@PathVariable("id") Long id,
                               @RequestParam(required = false) String currentPassword,
                               @RequestParam String newPassword,
                               @RequestParam String confirmPassword,
                               RedirectAttributes ra,
                               Model model) {
        if (newPassword == null || !newPassword.equals(confirmPassword)) {
            ra.addFlashAttribute("errorMessage", "Confirmação de senha não confere.");
            return "redirect:/admin/usuarios/alterar-senha/" + id;
        }
        try {
            usuarioService.alterarSenha(id, currentPassword, newPassword);
            ra.addFlashAttribute("successMessage", "Senha alterada com sucesso.");
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/admin/usuarios/alterar-senha/" + id;
        }
        return "redirect:/admin/usuarios/alterar-senha/" + id;
    }
}
