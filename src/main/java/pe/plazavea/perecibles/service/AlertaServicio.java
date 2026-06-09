package pe.plazavea.perecibles.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.plazavea.perecibles.enums.EstadoAlerta;
import pe.plazavea.perecibles.enums.EstadoLote;
import pe.plazavea.perecibles.enums.TipoAlerta;
import pe.plazavea.perecibles.model.Alerta;
import pe.plazavea.perecibles.model.ConfiguracionAlerta;
import pe.plazavea.perecibles.model.Lote;
import pe.plazavea.perecibles.model.Usuario;
import pe.plazavea.perecibles.repository.AlertaRepository;
import pe.plazavea.perecibles.repository.ConfiguracionAlertaRepository;
import pe.plazavea.perecibles.repository.LoteRepository;

@Service
public class AlertaServicio {

    private final AlertaRepository alertaRepository;
    private final LoteRepository loteRepository;
    private final ConfiguracionAlertaRepository configRepository;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public AlertaServicio(
            AlertaRepository alertaRepository,
            LoteRepository loteRepository,
            ConfiguracionAlertaRepository configRepository
    ) {
        this.alertaRepository = alertaRepository;
        this.loteRepository = loteRepository;
        this.configRepository = configRepository;
    }

    public void iniciarScheduler() {
        scheduler.scheduleAtFixedRate(this::generarAlertasSafely, 0, 1, TimeUnit.HOURS);
    }

    public ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    @Transactional
    public void generarAlertas() {
        ConfiguracionAlerta config = configRepository.findFirstByActivoTrue().orElseGet(this::configPorDefecto);
        List<Lote> lotes = loteRepository.findByEstadoNot(EstadoLote.RETIRADO);

        for (Lote lote : lotes) {
            long dias = lote.getDiasParaVencer();
            if (dias < 0) {
                lote.setEstado(EstadoLote.VENCIDO);
                upsertAlerta(lote, TipoAlerta.VENCIDO, (int) dias);
            } else if (dias <= config.getDiasCriticos()) {
                lote.setEstado(EstadoLote.PROXIMO_VENCER);
                upsertAlerta(lote, TipoAlerta.CRITICA, (int) dias);
            } else if (dias <= config.getDiasAdvertencia()) {
                lote.setEstado(EstadoLote.PROXIMO_VENCER);
                upsertAlerta(lote, TipoAlerta.PROXIMO_VENCER, (int) dias);
            } else if (dias <= config.getDiasAvisoAnticipado()) {
                lote.setEstado(EstadoLote.DISPONIBLE);
                upsertAlerta(lote, TipoAlerta.AVISO_ANTICIPADO, (int) dias);
            }
        }
    }

    public List<Alerta> obtenerPendientes() {
        return alertaRepository.findByEstado(EstadoAlerta.PENDIENTE);
    }

    public List<Alerta> obtenerTodas() {
        return alertaRepository.findAll();
    }

    @Transactional
    public void atenderAlerta(Integer idAlerta, Usuario usuario) {
        Alerta alerta = alertaRepository.findById(idAlerta)
                .orElseThrow(() -> new RuntimeException("Alerta no encontrada"));
        alerta.setEstado(EstadoAlerta.ATENDIDA);
        alerta.setUsuarioAtiende(usuario);
        alertaRepository.save(alerta);
    }

    @Transactional
    public void ignorarAlerta(Integer idAlerta) {
        Alerta alerta = alertaRepository.findById(idAlerta)
                .orElseThrow(() -> new RuntimeException("Alerta no encontrada"));
        alerta.setEstado(EstadoAlerta.IGNORADA);
        alertaRepository.save(alerta);
    }

    private void upsertAlerta(Lote lote, TipoAlerta tipo, int dias) {
        Optional<Alerta> existing = alertaRepository.findByLoteIdLoteAndEstado(
                lote.getIdLote(),
                EstadoAlerta.PENDIENTE
        );
        Alerta alerta = existing.orElseGet(Alerta::new);
        alerta.setLote(lote);
        alerta.setTipoAlerta(tipo);
        alerta.setDiasParaVencer(dias);
        alerta.setFechaGeneracion(LocalDateTime.now());
        alerta.setEstado(EstadoAlerta.PENDIENTE);
        alertaRepository.save(alerta);
    }

    private void generarAlertasSafely() {
        try {
            generarAlertas();
        } catch (RuntimeException ignored) {
            // The UI surfaces database errors during user-initiated operations.
        }
    }

    private ConfiguracionAlerta configPorDefecto() {
        ConfiguracionAlerta config = new ConfiguracionAlerta();
        config.setDiasCriticos(1);
        config.setDiasAdvertencia(3);
        config.setDiasAvisoAnticipado(7);
        return config;
    }
}
