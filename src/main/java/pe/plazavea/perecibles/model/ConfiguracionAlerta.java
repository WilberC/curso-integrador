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
@Table(name = "configuracion")
public class ConfiguracionAlerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idConfig;

    @Column(nullable = false)
    private int diasCriticos = 1;

    @Column(nullable = false)
    private int diasAdvertencia = 3;

    @Column(nullable = false)
    private int diasAvisoAnticipado = 7;

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
        return diasAdvertencia;
    }

    public void setDiasAlertaAmarilla(int diasAlertaAmarilla) {
        this.diasAdvertencia = diasAlertaAmarilla;
    }

    public int getDiasAlertaRoja() {
        return diasCriticos;
    }

    public void setDiasAlertaRoja(int diasAlertaRoja) {
        this.diasCriticos = diasAlertaRoja;
    }

    public int getDiasCriticos() {
        return diasCriticos;
    }

    public void setDiasCriticos(int diasCriticos) {
        this.diasCriticos = diasCriticos;
    }

    public int getDiasAdvertencia() {
        return diasAdvertencia;
    }

    public void setDiasAdvertencia(int diasAdvertencia) {
        this.diasAdvertencia = diasAdvertencia;
    }

    public int getDiasAvisoAnticipado() {
        return diasAvisoAnticipado;
    }

    public void setDiasAvisoAnticipado(int diasAvisoAnticipado) {
        this.diasAvisoAnticipado = diasAvisoAnticipado;
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
