package com.fiap.clyvocaresc.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.fiap.clyvocaresc.dto.request.CityRequestDTO;
import com.fiap.clyvocaresc.dto.response.CityResponseDTO;
import com.fiap.clyvocaresc.entity.City;
import com.fiap.clyvocaresc.exception.ResourceNotFoundException;
import com.fiap.clyvocaresc.repository.CityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
/**
 * Gerencia o cadastro de cidades, usado como referência geográfica por Owner, Clinic
 * e futuramente pelo módulo de alertas regionais. É uma tabela de catálogo puro, sem
 * regra de negócio além da validação de campos — a única responsabilidade real aqui
 * é permitir que Owner/Clinic apontem pra um local consistente em vez de texto livre,
 * o que viabiliza agregações futuras tipo "todos os tutores de São Paulo".
 * <p>
 * Endpoints necessários: GET /api/cities, GET /api/cities/{id}, POST /api/cities,
 * PUT /api/cities/{id}, DELETE /api/cities/{id}.
 */
@Service
@RequiredArgsConstructor
public class CityService {
    private final CityRepository cityRepository;

    /** Lista todas as cidades cadastradas, sem paginação (volume baixo, cadastro administrativo). */
    @Transactional(readOnly = true)
    public List<CityResponseDTO> findAll() {
        return cityRepository.findAll().stream().map(this::toResponse).toList();
    }

    /** Busca uma cidade específica por id; lança 404 (via ResourceNotFoundException) se não existir. */
    @Transactional(readOnly = true)
    public CityResponseDTO findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    /** Cria uma nova cidade a partir do DTO validado e retorna o registro já persistido. */
    @Transactional
    public CityResponseDTO create(CityRequestDTO dto) {
        City city = new City();
        apply(city, dto);
        return toResponse(cityRepository.save(city));
    }

    /** Atualiza os dados de uma cidade existente; falha se o id não existir. */
    @Transactional
    public CityResponseDTO update(Long id, CityRequestDTO dto) {
        City city = getOrThrow(id);
        apply(city, dto);
        return toResponse(cityRepository.save(city));
    }

    /** Remove uma cidade do catálogo; falha se o id não existir. */
    @Transactional
    public void delete(Long id) {
        cityRepository.delete(getOrThrow(id));
    }

    /** Busca interna com tratamento de "não encontrado" centralizado, evitando repetição nos métodos públicos. */
    @Transactional
    private City getOrThrow(Long id) {
        return cityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cidade não encontrada com id " + id));
    }

    /** Copia os campos do DTO de entrada pra dentro da entidade (usado em create e update). */
    @Transactional
    private void apply(City city, CityRequestDTO dto) {
        city.setName(dto.name());
        city.setState(dto.state());
        city.setRegion(dto.region());
    }

    /** Converte a entidade JPA em DTO de saída, isolando o cliente da API da estrutura interna do banco. */
    @Transactional
    private CityResponseDTO toResponse(City city) {
        return new CityResponseDTO(city.getId(), city.getName(), city.getState(), city.getRegion());
    }
}
