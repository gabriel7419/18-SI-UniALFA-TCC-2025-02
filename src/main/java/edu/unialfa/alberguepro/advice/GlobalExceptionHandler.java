package edu.unialfa.alberguepro.advice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Adiciona um logger para registrar os erros no console
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Captura exceções específicas de negócio (ex: estoque insuficiente)
    @ExceptionHandler(IllegalStateException.class)
    public String handleBusinessException(IllegalStateException ex, RedirectAttributes redirectAttributes, HttpServletRequest request) {
        // Adiciona uma mensagem de erro que será exibida na próxima página
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        
        // Retorna o usuário para a página de onde a requisição veio
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }

    // Captura exceções de recurso não encontrado (404)
    @ExceptionHandler(NoResourceFoundException.class)
    public ModelAndView handleNoResourceFound(HttpServletRequest req, NoResourceFoundException ex) {
        // Loga como AVISO (WARN) pois geralmente não é um erro crítico da aplicação
        logger.warn("Recurso não encontrado para a URL: {}. Mensagem: {}", req.getRequestURL(), ex.getMessage());

        ModelAndView mav = new ModelAndView();
        mav.addObject("exception", ex);
        mav.addObject("url", req.getRequestURL());
        mav.setViewName("error"); // Aponta para a página de erro genérica
        return mav;
    }

    // Captura qualquer outra exceção não tratada
    @ExceptionHandler(Exception.class)
    public ModelAndView handleGenericException(HttpServletRequest req, Exception ex) {
        // Loga o erro completo no console para facilitar a depuração
        logger.error("Request: " + req.getRequestURL() + " raised " + ex.getClass().getName(), ex);

        // Cria um objeto para enviar dados para a view de erro
        ModelAndView mav = new ModelAndView();
        mav.addObject("exception", ex);
        mav.addObject("url", req.getRequestURL());
        mav.setViewName("error"); // Aponta para o arquivo templates/error.html
        return mav;
    }
}