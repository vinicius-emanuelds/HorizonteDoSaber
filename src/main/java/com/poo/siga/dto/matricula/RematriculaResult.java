package com.poo.siga.dto.matricula;

import java.util.List;

public record RematriculaResult(
    int total,
    int matriculados,
    int ignorados,
    List<String> ignoradosNomes
) {}
