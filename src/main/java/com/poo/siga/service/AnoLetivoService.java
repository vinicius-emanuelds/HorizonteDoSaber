package com.poo.siga.service;

import com.poo.siga.dto.anoletivo.AnoLetivoRequest;
import com.poo.siga.dto.anoletivo.AnoLetivoResponse;
import com.poo.siga.model.AnoLetivo;
import com.poo.siga.repository.AnoLetivoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.poo.siga.model.SemanaAvaliacao;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AnoLetivoService {

    private final AnoLetivoRepository anoLetivoRepository;

    // ─────────────────────── Consultas ───────────────────────

    @Transactional(readOnly = true)
    public List<AnoLetivoResponse> listar() {
        return anoLetivoRepository.findAllByOrderByAnoDesc()
                .stream().map(AnoLetivoResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public AnoLetivoResponse buscarPorId(Integer id) {
        return AnoLetivoResponse.from(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public AnoLetivoResponse buscarAtivo() {
        return anoLetivoRepository.findFirstByEncerradoFalseOrderByAnoDesc()
                .map(AnoLetivoResponse::from)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Nenhum ano letivo ativo encontrado"));
    }

    // ─────────────────────── Mutações (ADMIN) ───────────────────────

    @Transactional
    public AnoLetivoResponse criar(AnoLetivoRequest req) {
        if (anoLetivoRepository.findByAno(req.ano()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Já existe um ano letivo cadastrado para o ano " + req.ano());
        }
        if (req.dataEncerramento().isBefore(req.dataInicio())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "A data de encerramento deve ser posterior à data de início");
        }
        AnoLetivo al = new AnoLetivo();
        preencherCampos(al, req);
        return AnoLetivoResponse.from(anoLetivoRepository.save(al));
    }

    @Transactional
    public AnoLetivoResponse atualizar(Integer id, AnoLetivoRequest req) {
        AnoLetivo al = findOrThrow(id);
        if (al.isEncerrado()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Não é possível alterar um ano letivo já encerrado");
        }
        // Verifica conflito de ano com outro registro
        anoLetivoRepository.findByAno(req.ano()).ifPresent(outro -> {
            if (!outro.getId().equals(id)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Já existe um ano letivo cadastrado para o ano " + req.ano());
            }
        });
        if (req.dataEncerramento().isBefore(req.dataInicio())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "A data de encerramento deve ser posterior à data de início");
        }
        preencherCampos(al, req);
        return AnoLetivoResponse.from(anoLetivoRepository.save(al));
    }

    /**
     * Encerra o ano letivo: marca encerrado = true.
     * A partir deste momento, nenhum lançamento de nota ou frequência
     * pode ser feito para turmas deste ano.
     */
    @Transactional
    public AnoLetivoResponse encerrar(Integer id) {
        AnoLetivo al = findOrThrow(id);
        if (al.isEncerrado()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "O ano letivo " + al.getAno() + " já está encerrado");
        }
        al.setEncerrado(true);
        return AnoLetivoResponse.from(anoLetivoRepository.save(al));
    }

    // ─────────────────────── Validação de negócio ───────────────────────

    /**
     * Verifica se o ano letivo de uma turma está aberto para lançamentos.
     * Lança ResponseStatusException 422 caso esteja encerrado.
     *
     * @param anoLetivo ano da turma
     */
    public void validarAnoAberto(Integer anoLetivo) {
        anoLetivoRepository.findByAno(anoLetivo).ifPresent(al -> {
            if (al.isEncerrado()) {
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "O ano letivo " + anoLetivo + " está encerrado. " +
                        "Não é permitido realizar novos lançamentos de nota ou frequência.");
            }
        });
    }

    // ─────────────────────── Utilitários privados ───────────────────────

    // private void preencherCampos(AnoLetivo al, AnoLetivoRequest req) {
    //     al.setAno(req.ano());
    //     al.setDataInicio(req.dataInicio());
    //     al.setDataEncerramento(req.dataEncerramento());
    //     al.setDiasLetivos(req.diasLetivos());
    private void preencherCampos(AnoLetivo anoLetivo, AnoLetivoRequest req) {
        anoLetivo.setAno(req.ano());
        anoLetivo.setDataInicio(req.dataInicio());
        anoLetivo.setDataEncerramento(req.dataEncerramento());
        anoLetivo.setDiasLetivos(req.diasLetivos());
        
        // Se a lista de feriados vier vazia (nova criação ou limpar), tenta buscar da BrasilAPI
        List<LocalDate> feriadosFinais = new ArrayList<>();
        if (req.feriados() != null) {
            feriadosFinais.addAll(req.feriados());
        }
        
        if (feriadosFinais.isEmpty()) {
             try {
                RestTemplate restTemplate = new RestTemplate();
                ResponseEntity<List<Map<String, Object>>> response = restTemplate.exchange(
                    "https://brasilapi.com.br/api/feriados/v1/" + req.ano(),
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {}
                );
                
                if (response.getBody() != null) {
                    for (Map<String, Object> feriado : response.getBody()) {
                        String dataStr = (String) feriado.get("date");
                        if (dataStr != null) {
                            feriadosFinais.add(LocalDate.parse(dataStr));
                        }
                    }
                }
             } catch (Exception e) {
                 // Log error or ignore if API is unavailable, user can add manually later
                 System.err.println("Failed to fetch holidays from BrasilAPI: " + e.getMessage());
             }
        }
        
        // Remove duplicates and sort
        List<LocalDate> feriadosUnicos = feriadosFinais.stream().distinct().sorted().collect(Collectors.toList());
        anoLetivo.setFeriados(feriadosUnicos);

        if (req.semanasAvaliacao() != null) {
            List<SemanaAvaliacao> semanas = req.semanasAvaliacao().stream().map(sReq -> {
                SemanaAvaliacao semanaAvaliacao = new SemanaAvaliacao();
                semanaAvaliacao.setBimestre(sReq.bimestre());
                semanaAvaliacao.setTipo(sReq.tipo());
                semanaAvaliacao.setDataInicio(sReq.dataInicio());
                semanaAvaliacao.setDataFim(sReq.dataFim());
                return semanaAvaliacao;
                // SemanaAvaliacao s = new SemanaAvaliacao();
                // s.setBimestre(sReq.bimestre());
                // s.setTipo(sReq.tipo());
                // s.setDataInicio(sReq.dataInicio());
                // s.setDataFim(sReq.dataFim());
                // return s;
            }).collect(Collectors.toList());
            anoLetivo.setSemanasAvaliacao(semanas);
        } else {
            anoLetivo.setSemanasAvaliacao(new ArrayList<>());
        }
    }

    private AnoLetivo findOrThrow(Integer id) {
        return anoLetivoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Ano letivo não encontrado"));
    }
}
