package pe.plazavea.perecibles.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "configuracion_alerta")
public class ConfiguracionAlerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idConfig;

    @Column(nullable = false)
    private int diasAlertaAmarilla = 7;

    @Column(nullable = false)
    private int diasAlertaRoja = 2;

    @Column(nullable = false)
    private boolean activo = true;

    @OneToOne
    @JoinColumn(name = "id_usuario_config")
    private Usuario usuarioConfig;

    public ConfiguracionAlerta() {
    }

    public Integer getIdConfig() {
        return idConfig;
    }

    public void setIdConfig(Integer idConfig) {
        this.idConfig = idConfig;
    }

    public int getDiasAlertaAmarilla() {
        return diasAlertaAmarilla;
    }

    public void setDiasAlertaAmarilla(int diasAlertaAmarilla) {
        this.diasAlertaAmarilla = diasAlertaAmarilla;
    }

    public int getDiasAlertaRoja() {
        return diasAlertaRoja;
    }

    public void setDiasAlertaRoja(int diasAlertaRoja) {
        this.diasAlertaRoja = diasAlertaRoja;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public Usuario getUsuarioConfig() {
        return usuarioConfig;
    }

    public void setUsuarioConfig(Usuario usuarioConfig) {
        this.usuarioConfig = usuarioConfig;
    }
}
