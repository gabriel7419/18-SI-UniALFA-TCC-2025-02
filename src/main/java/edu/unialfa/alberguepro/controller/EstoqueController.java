package edu.unialfa.alberguepro.controller;

import edu.unialfa.alberguepro.model.Produto;
import edu.unialfa.alberguepro.model.Unidade;
import edu.unialfa.alberguepro.repository.ProdutoRepository;
import edu.unialfa.alberguepro.repository.UnidadeRepository;
import edu.unialfa.alberguepro.repository.ProdutoSpecification;
import edu.unialfa.alberguepro.service.EstoqueService;
import edu.unialfa.alberguepro.service.RelatorioService;
import edu.unialfa.alberguepro.repository.MovimentacaoEstoqueRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import jakarta.validation.Valid;
import net.sf.jasperreports.engine.JRException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/estoque")
public class EstoqueController {

    private static final Logger log = LoggerFactory.getLogger(EstoqueController.class);

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private UnidadeRepository unidadeRepository;

    @Autowired
    private EstoqueService estoqueService;

    @Autowired
    private RelatorioService relatorioService;

    private void carregarUnidades(Model model) {
        List<Unidade> unidades = unidadeRepository.findAll();
        model.addAttribute("unidades", unidades);
    }

    @GetMapping({"/", ""}) 
    public String listarProdutos(Model model,
        @RequestParam(required = false) String nome,
        @RequestParam(required = false) String tipo,
        @RequestParam(required = false) Long unidadeId,
        @RequestParam(required = false) Integer diasVencimento,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "15") int size,
        @RequestParam(defaultValue = "nome") String sort,
        @RequestParam(defaultValue = "asc") String dir) {
        
        org.springframework.data.domain.Page<Produto> pageResult;
        
        // Criar ordenação
        org.springframework.data.domain.Sort.Direction direction = dir.equals("desc") ? 
            org.springframework.data.domain.Sort.Direction.DESC : org.springframework.data.domain.Sort.Direction.ASC;
        org.springframework.data.domain.Sort sortObj = org.springframework.data.domain.Sort.by(direction, sort);
        
        // Se o filtro de vencimento está ativo, usar lógica específica
        if (diasVencimento != null && diasVencimento > 0) {
            List<Produto> produtos = estoqueService.buscarProdutosProximosVencimento(diasVencimento);
            
            // Aplicar filtros adicionais se necessário
            if (nome != null && !nome.isEmpty()) {
                produtos = produtos.stream()
                    .filter(p -> p.getNome().toLowerCase().contains(nome.toLowerCase()))
                    .toList();
            }
            if (tipo != null && !tipo.isEmpty()) {
                produtos = produtos.stream()
                    .filter(p -> tipo.equals(p.getTipo()))
                    .toList();
            }
            if (unidadeId != null && unidadeId > 0) {
                produtos = produtos.stream()
                    .filter(p -> p.getUnidade() != null && p.getUnidade().getId().equals(unidadeId))
                    .toList();
            }
            
            // Paginar manualmente a lista
            int start = Math.min(page * size, produtos.size());
            int end = Math.min(start + size, produtos.size());
            List<Produto> pageContent = produtos.subList(start, end);
            pageResult = new org.springframework.data.domain.PageImpl<>(pageContent, 
                org.springframework.data.domain.PageRequest.of(page, size, sortObj), produtos.size());
        } else {
            // Lógica normal de filtros com paginação
            Specification<Produto> spec = Specification.where(null);

            if (nome != null && !nome.isEmpty()) {
                spec = spec.and(ProdutoSpecification.comNome(nome));
            }

            if (tipo != null && !tipo.isEmpty()) {
                spec = spec.and(ProdutoSpecification.comTipo(tipo));
            }

            Unidade unidade = null;
            if (unidadeId != null && unidadeId > 0) {
                unidade = unidadeRepository.findById(unidadeId).orElse(null);
                if (unidade != null) {
                    spec = spec.and(ProdutoSpecification.comUnidade(unidade));
                }
            }

            org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sortObj);
            pageResult = produtoRepository.findAll(spec, pageable);
        }

        Unidade unidade = null;
        if (unidadeId != null && unidadeId > 0) {
            unidade = unidadeRepository.findById(unidadeId).orElse(null);
        }

        model.addAttribute("produtos", pageResult.getContent());
        model.addAttribute("page", pageResult);
        model.addAttribute("nome", nome);
        model.addAttribute("tipo", tipo);
        model.addAttribute("unidade", unidade);
        model.addAttribute("diasVencimento", diasVencimento);
        model.addAttribute("size", size);
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);
        carregarUnidades(model);

        return "estoque/index";
    }

    @GetMapping("/novo")
    public String novoProdutoForm(Model model) {
        model.addAttribute("produto", new Produto());
        carregarUnidades(model);
        return "estoque/form";
    }

    @PostMapping("/salvar")
    public String salvarProduto(@Valid Produto produto, BindingResult result, Model model) {
        // Validação de unicidade que requer acesso ao banco
        if (produto.getNome() != null && produto.getTipo() != null) {
            if (!estoqueService.isNomeAndTipoUnique(produto.getNome(), produto.getTipo(), produto.getId())) {
                result.rejectValue("nome", "error.produto", "Já existe um produto com este nome e tipo.");
            }
        }

        // Validação customizada: data de vencimento obrigatória se não for produto não perecível
        if (produto.getNaoPerecivel() == null || !produto.getNaoPerecivel()) {
            if (produto.getDataDeVencimento() == null) {
                result.rejectValue("dataDeVencimento", "error.produto", "A data de vencimento é obrigatória para produtos perecíveis.");
            }
        }

        // Verifica todos os erros de validação
        if (result.hasErrors()) {
            carregarUnidades(model);
            model.addAttribute("errorMessage", "Há problemas em um dos campos preenchidos, verifique e corrija.");
            return "estoque/form";
        }

        // Se a validação passou, o 'unidadeId' existe. 
        Optional<Unidade> unidadeOptional = unidadeRepository.findById(produto.getUnidadeId());
        if (unidadeOptional.isEmpty()) {    
            result.rejectValue("unidadeId", "error.produto", "Unidade selecionada é inválida.");
            carregarUnidades(model);
            model.addAttribute("errorMessage", "Há problemas em um dos campos preenchidos, verifique e corrija.");
            return "estoque/form";
        }
        produto.setUnidade(unidadeOptional.get());
        
        estoqueService.salvar(produto); // Alterado para usar o serviço

        return "redirect:/estoque";
    }

    @GetMapping("/editar/{id}")
    public String editarProdutoForm(@PathVariable("id") Long id, Model model) {
        Optional<Produto> produto = produtoRepository.findById(id);
        if (produto.isPresent()) {
            model.addAttribute("produto", produto.get());
            carregarUnidades(model);
            return "estoque/form";
        } else {
            return "redirect:/estoque";
        }
    }

    @GetMapping("/baixa")
    public String darBaixaForm(@RequestParam(value = "filtro", required = false) String filtro,
        @RequestParam(value = "tipo", required = false) String tipo,
        Model model) {
        List<Produto> produtos;
        if ((filtro != null && !filtro.isEmpty()) || (tipo != null && !tipo.isEmpty())) {
            produtos = produtoRepository.findByNomeContainingIgnoreCaseAndTipoContainingIgnoreCase(filtro, tipo);
        } else {
            produtos = produtoRepository.findAll();
        }
        model.addAttribute("produtos", produtos);
        model.addAttribute("filtro", filtro);
        model.addAttribute("tipo", tipo);
        return "estoque/baixa";
    }

    @PostMapping("/dar-baixa")
    public String processarBaixaIndividual(@RequestParam("produtoId") Long produtoId, 
            @RequestParam("quantidade") Integer quantidade,
            RedirectAttributes redirectAttributes) {
        try {
            estoqueService.darBaixa(produtoId, quantidade);
            redirectAttributes.addFlashAttribute("successMessage", "Baixa realizada com sucesso!");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/estoque/baixa";
    }

    @PostMapping("/excluir/{id}")
    public String excluirProduto(@PathVariable("id") Long id) {
        estoqueService.excluir(id); // Alterado para usar o serviço
        return "redirect:/estoque";
    }

    @GetMapping("/relatorio/pdf")
    public ResponseEntity<InputStreamResource> gerarRelatorioPdf(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) Long unidadeId) throws JRException {

        Specification<Produto> spec = Specification.where(null);
        if (nome != null && !nome.isEmpty()) {
            spec = spec.and(ProdutoSpecification.comNome(nome));
        }
        if (tipo != null && !tipo.isEmpty()) {
            spec = spec.and(ProdutoSpecification.comTipo(tipo));
        }
        Unidade unidade = null;
        if (unidadeId != null && unidadeId > 0) { 
            unidade = unidadeRepository.findById(unidadeId).orElse(null);
            if (unidade != null) {
                spec = spec.and(ProdutoSpecification.comUnidade(unidade));
            }
        }

        List<Produto> produtos = produtoRepository.findAll(spec);
        ByteArrayInputStream bis = relatorioService.gerarRelatorioPdf(produtos);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=estoque.pdf");

        return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }

    @GetMapping("/relatorio/excel")
    public ResponseEntity<InputStreamResource> gerarRelatorioExcel(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) Long unidadeId) throws Exception {

        Specification<Produto> spec = Specification.where(null);
        if (nome != null && !nome.isEmpty()) {
            spec = spec.and(ProdutoSpecification.comNome(nome));
        }
        if (tipo != null && !tipo.isEmpty()) {
            spec = spec.and(ProdutoSpecification.comTipo(tipo));
        }
        Unidade unidade = null;
        if (unidadeId != null && unidadeId > 0) { 
            unidade = unidadeRepository.findById(unidadeId).orElse(null);
            if (unidade != null) {
                spec = spec.and(ProdutoSpecification.comUnidade(unidade));
            }
        }

        List<Produto> produtos = produtoRepository.findAll(spec);
        ByteArrayInputStream bis = relatorioService.gerarRelatorioExcel(produtos);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=estoque.xlsx");

        return ResponseEntity.ok().headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(bis));
    }

    @Autowired
    private MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;

    @GetMapping("/historico")
    public String verHistorico(Model model,
        @RequestParam(required = false) String nomeProduto,
        @RequestParam(required = false) String tipo) {
        
        Specification<edu.unialfa.alberguepro.model.MovimentacaoEstoque> spec = Specification.where(null);

        if (nomeProduto != null && !nomeProduto.isEmpty()) {
            spec = spec.and(edu.unialfa.alberguepro.repository.MovimentacaoEstoqueSpecification.comProdutoNome(nomeProduto));
        }

        if (tipo != null && !tipo.isEmpty()) {
            try {
                edu.unialfa.alberguepro.model.MovimentacaoEstoque.TipoMovimentacao tipoEnum = 
                    edu.unialfa.alberguepro.model.MovimentacaoEstoque.TipoMovimentacao.valueOf(tipo);
                spec = spec.and(edu.unialfa.alberguepro.repository.MovimentacaoEstoqueSpecification.comTipo(tipoEnum));
            } catch (IllegalArgumentException e) {
                // Ignora tipo inválido
            }
        }

        model.addAttribute("movimentacoes", movimentacaoEstoqueRepository.findAll(spec, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "dataMovimentacao")));
        model.addAttribute("nomeProduto", nomeProduto);
        model.addAttribute("tipo", tipo);
        
        return "estoque/historico";
    }

    @GetMapping("/historico/relatorio/pdf")
    public ResponseEntity<InputStreamResource> gerarRelatorioMovimentacaoPdf(
            @RequestParam(required = false) String nomeProduto,
            @RequestParam(required = false) String tipo) throws JRException {
        
        Specification<edu.unialfa.alberguepro.model.MovimentacaoEstoque> spec = Specification.where(null);

        if (nomeProduto != null && !nomeProduto.isEmpty()) {
            spec = spec.and(edu.unialfa.alberguepro.repository.MovimentacaoEstoqueSpecification.comProdutoNome(nomeProduto));
        }

        if (tipo != null && !tipo.isEmpty()) {
            try {
                edu.unialfa.alberguepro.model.MovimentacaoEstoque.TipoMovimentacao tipoEnum = 
                    edu.unialfa.alberguepro.model.MovimentacaoEstoque.TipoMovimentacao.valueOf(tipo);
                spec = spec.and(edu.unialfa.alberguepro.repository.MovimentacaoEstoqueSpecification.comTipo(tipoEnum));
            } catch (IllegalArgumentException e) {
                // Ignora tipo inválido
            }
        }

        List<edu.unialfa.alberguepro.model.MovimentacaoEstoque> movimentacoes = movimentacaoEstoqueRepository.findAll(spec, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "dataMovimentacao"));
        ByteArrayInputStream bis = relatorioService.gerarRelatorioMovimentacaoPdf(movimentacoes);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=movimentacao_estoque.pdf");

        return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }

    @GetMapping("/historico/relatorio/excel")
    public ResponseEntity<InputStreamResource> gerarRelatorioMovimentacaoExcel(
            @RequestParam(required = false) String nomeProduto,
            @RequestParam(required = false) String tipo) throws Exception {
        
        Specification<edu.unialfa.alberguepro.model.MovimentacaoEstoque> spec = Specification.where(null);

        if (nomeProduto != null && !nomeProduto.isEmpty()) {
            spec = spec.and(edu.unialfa.alberguepro.repository.MovimentacaoEstoqueSpecification.comProdutoNome(nomeProduto));
        }

        if (tipo != null && !tipo.isEmpty()) {
            try {
                edu.unialfa.alberguepro.model.MovimentacaoEstoque.TipoMovimentacao tipoEnum = 
                    edu.unialfa.alberguepro.model.MovimentacaoEstoque.TipoMovimentacao.valueOf(tipo);
                spec = spec.and(edu.unialfa.alberguepro.repository.MovimentacaoEstoqueSpecification.comTipo(tipoEnum));
            } catch (IllegalArgumentException e) {
                // Ignora tipo inválido
            }
        }

        List<edu.unialfa.alberguepro.model.MovimentacaoEstoque> movimentacoes = movimentacaoEstoqueRepository.findAll(spec, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "dataMovimentacao"));
        ByteArrayInputStream bis = relatorioService.gerarRelatorioMovimentacaoExcel(movimentacoes);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=movimentacao_estoque.xlsx");

        return ResponseEntity.ok().headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(bis));
    }

    @GetMapping("/relatorio/vencimento/pdf")
    public ResponseEntity<InputStreamResource> gerarRelatorioVencimentoPdf(
            @RequestParam(defaultValue = "30") Integer dias) throws JRException {
        
        List<Produto> produtos = estoqueService.buscarProdutosProximosVencimento(dias);
        ByteArrayInputStream bis = relatorioService.gerarRelatorioVencimentoPdf(produtos, dias);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=produtos_vencimento.pdf");

        return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(bis));
    }

    @GetMapping("/relatorio/vencimento/excel")
    public ResponseEntity<InputStreamResource> gerarRelatorioVencimentoExcel(
            @RequestParam(defaultValue = "30") Integer dias) throws Exception {
        
        List<Produto> produtos = estoqueService.buscarProdutosProximosVencimento(dias);
        ByteArrayInputStream bis = relatorioService.gerarRelatorioVencimentoExcel(produtos, dias);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=produtos_vencimento.xlsx");

        return ResponseEntity.ok().headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(bis));
    }

    @GetMapping("/relatorio/estrategico-pdf")
    public ResponseEntity<byte[]> relatorioEstrategicoPdf() {
        try {
            List<Produto> todosProdutos = produtoRepository.findAll();
            
            // Produtos com estoque baixo
            List<Produto> produtosBaixoEstoque = todosProdutos.stream()
                    .filter(p -> p.getQuantidade() <= 10)
                    .sorted(Comparator.comparing(Produto::getQuantidade))
                    .collect(Collectors.toList());
            
            // Totais gerais
            long totalProdutos = todosProdutos.size();
            long totalItens = todosProdutos.stream().mapToLong(Produto::getQuantidade).sum();
            long produtosEmEstoque = todosProdutos.stream().filter(p -> p.getQuantidade() > 0).count();
            long produtosZerados = totalProdutos - produtosEmEstoque;
            
            // Preparar dados
            List<Map<String, Object>> dados = new java.util.ArrayList<>();
            for (Produto produto : produtosBaixoEstoque) {
                Map<String, Object> item = new java.util.HashMap<>();
                item.put("nome", produto.getNome());
                item.put("tipo", produto.getTipo());
                item.put("quantidade", produto.getQuantidade());
                item.put("unidade", produto.getUnidade() != null ? produto.getUnidade().getNome() : "-");
                
                String situacao;
                if (produto.getQuantidade() == 0) {
                    situacao = "ZERADO";
                } else if (produto.getQuantidade() <= 5) {
                    situacao = "CRÍTICO";
                } else {
                    situacao = "BAIXO";
                }
                item.put("situacao", situacao);
                dados.add(item);
            }
            
            // Carregar template
            InputStream jrxmlStream = getClass().getResourceAsStream("/relatorios/relatorio_estoque_estrategico.jrxml");
            if (jrxmlStream == null) {
                throw new RuntimeException("Template JRXML não encontrado: /relatorios/relatorio_estoque_estrategico.jrxml");
            }
            net.sf.jasperreports.engine.JasperReport jasperReport = 
                    net.sf.jasperreports.engine.JasperCompileManager.compileReport(jrxmlStream);
            
            // Parâmetros
            Map<String, Object> parametros = new java.util.HashMap<>();
            parametros.put("TOTAL_PRODUTOS", totalProdutos);
            parametros.put("TOTAL_ITENS", totalItens);
            parametros.put("PRODUTOS_EM_ESTOQUE", produtosEmEstoque);
            parametros.put("PRODUTOS_ZERADOS", produtosZerados);
            
            // DataSource
            net.sf.jasperreports.engine.data.JRBeanCollectionDataSource dataSource = 
                    new net.sf.jasperreports.engine.data.JRBeanCollectionDataSource(dados);
            
            // Preencher
            net.sf.jasperreports.engine.JasperPrint jasperPrint = 
                    net.sf.jasperreports.engine.JasperFillManager.fillReport(jasperReport, parametros, dataSource);
            
            // Exportar
            byte[] pdf = net.sf.jasperreports.engine.JasperExportManager.exportReportToPdf(jasperPrint);
            
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
            headers.setContentDisposition(org.springframework.http.ContentDisposition.inline()
                    .filename("relatorio-estoque-estrategico.pdf")
                    .build());
            
            return new ResponseEntity<>(pdf, headers, org.springframework.http.HttpStatus.OK);
            
        } catch (Exception e) {
            log.error("Erro ao gerar relatório estratégico de estoque", e);
            e.printStackTrace();
            return ResponseEntity.status(500).body(("Erro: " + e.getMessage()).getBytes());
        }
    }

    @GetMapping("/relatorio/estrategico-excel")
    @ResponseBody
    public ResponseEntity<org.springframework.core.io.Resource> relatorioEstrategicoExcel() {
        try {
            List<Produto> todosProdutos = produtoRepository.findAll();
            
            org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook();
            
            // Sheet 1: Resumo Geral
            org.apache.poi.ss.usermodel.Sheet sheetResumo = workbook.createSheet("Resumo Geral");
            criarSheetResumo(sheetResumo, todosProdutos, workbook);
            
            // Sheet 2: Estoque Baixo
            org.apache.poi.ss.usermodel.Sheet sheetBaixo = workbook.createSheet("Estoque Baixo");
            List<Produto> produtosBaixo = todosProdutos.stream()
                    .filter(p -> p.getQuantidade() <= 10)
                    .sorted(Comparator.comparing(Produto::getQuantidade))
                    .collect(Collectors.toList());
            criarSheetProdutos(sheetBaixo, produtosBaixo, "Produtos com Estoque Baixo", workbook);
            
            // Sheet 3: Próximos ao Vencimento
            org.apache.poi.ss.usermodel.Sheet sheetVencimento = workbook.createSheet("Próximos Vencimento");
            LocalDate dataLimite = LocalDate.now().plusDays(30);
            List<Produto> produtosVencimento = todosProdutos.stream()
                    .filter(p -> p.getDataDeVencimento() != null && 
                                !p.getDataDeVencimento().isAfter(dataLimite))
                    .sorted(Comparator.comparing(Produto::getDataDeVencimento))
                    .collect(Collectors.toList());
            criarSheetProdutos(sheetVencimento, produtosVencimento, "Produtos Próximos ao Vencimento", workbook);
            
            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();
            
            org.springframework.core.io.ByteArrayResource resource = 
                    new org.springframework.core.io.ByteArrayResource(outputStream.toByteArray());
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio-estoque-estrategico.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);
                    
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
    
    private void criarSheetResumo(org.apache.poi.ss.usermodel.Sheet sheet, List<Produto> produtos, 
                                   org.apache.poi.ss.usermodel.Workbook workbook) {
        org.apache.poi.ss.usermodel.CellStyle headerStyle = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        
        int rowNum = 0;
        org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
        org.apache.poi.ss.usermodel.Cell cell = row.createCell(0);
        cell.setCellValue("RESUMO GERAL DO ESTOQUE");
        cell.setCellStyle(headerStyle);
        
        rowNum++;
        row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue("Total de Produtos:");
        row.createCell(1).setCellValue(produtos.size());
        
        row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue("Total de Itens:");
        row.createCell(1).setCellValue(produtos.stream().mapToLong(Produto::getQuantidade).sum());
        
        row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue("Produtos em Estoque:");
        row.createCell(1).setCellValue(produtos.stream().filter(p -> p.getQuantidade() > 0).count());
        
        row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue("Produtos Zerados:");
        row.createCell(1).setCellValue(produtos.stream().filter(p -> p.getQuantidade() == 0).count());
        
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }
    
    private void criarSheetProdutos(org.apache.poi.ss.usermodel.Sheet sheet, List<Produto> produtos, 
                                     String titulo, org.apache.poi.ss.usermodel.Workbook workbook) {
        org.apache.poi.ss.usermodel.CellStyle headerStyle = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(org.apache.poi.ss.usermodel.IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);
        
        int rowNum = 0;
        org.apache.poi.ss.usermodel.Row headerRow = sheet.createRow(rowNum++);
        String[] columns = {"Nome", "Tipo", "Quantidade", "Unidade", "Vencimento"};
        for (int i = 0; i < columns.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }
        
        for (Produto produto : produtos) {
            org.apache.poi.ss.usermodel.Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(produto.getNome());
            row.createCell(1).setCellValue(produto.getTipo());
            row.createCell(2).setCellValue(produto.getQuantidade());
            row.createCell(3).setCellValue(produto.getUnidade() != null ? produto.getUnidade().getNome() : "");
            row.createCell(4).setCellValue(produto.getDataDeVencimento() != null ? 
                    produto.getDataDeVencimento().toString() : "");
        }
        
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }
}