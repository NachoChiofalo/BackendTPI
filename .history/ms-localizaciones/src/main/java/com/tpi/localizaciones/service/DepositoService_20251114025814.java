package com.tpi.localizaciones.service;

import com.tpi.localizaciones.entity.Deposito;
import com.tpi.localizaciones.repository.DepositoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DepositoService {

    private final DepositoRepository depositoRepository;

    public List<Deposito> findAll() {
        return depositoRepository.findAll();
    }

    public Optional<Deposito> findById(Integer id) {
        return depositoRepository.findById(id);
    }

    public long count() {
        return depositoRepository.count();
    }

    @Transactional
    public Deposito save(Deposito deposito) {
        return depositoRepository.save(deposito);
    }

    @Transactional
    public Deposito update(Integer id, Deposito deposito) {
        return depositoRepository.findById(id)
                .map(d -> {
                    d.setNombre(deposito.getNombre());
                    d.setUbicacion(deposito.getUbicacion());
                    return depositoRepository.save(d);
                })
                .orElseThrow(() -> new RuntimeException("Depósito no encontrado con id: " + id));
    }
}

