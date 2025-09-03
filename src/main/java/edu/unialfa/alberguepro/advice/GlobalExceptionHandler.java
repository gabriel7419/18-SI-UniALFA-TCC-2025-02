package edu.unialfa.alberguepro.advice;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Captura exceções específicas de negócio (ex: estoque insuficiente)
    @ExceptionHandler(IllegalStateException.class)
    public String handleBusinessException(IllegalStateException ex, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        // Adiciona uma mensagem de erro que será exibida na próxima página
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        
        // Retorna o usuário para a página de onde a requisição veio
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }

    // Captura qualquer outra exceção não tratada
    @ExceptionHandler(Exception.class)
    public ModelAndView handleGenericException(HttpServletRequest req, Exception ex) {
        // Cria um objeto para enviar dados para a view de erro
        ModelAndView mav = new ModelAndView();
        mav.addObject("exception", ex);
        mav.addObject("url", req.getRequestURL());
        mav.setViewName("error"); // Aponta para o arquivo templates/error.html
        return mav;
    }
}