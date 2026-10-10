package com.six_m.uniform.domain.itemLote;

import com.six_m.uniform.shared.enums.Sexo;
import com.six_m.uniform.shared.enums.Tamanho;

import java.util.UUID;

record ChaveItemLote(UUID tipoUniformeId, Tamanho tamanho, Sexo sexo) {}