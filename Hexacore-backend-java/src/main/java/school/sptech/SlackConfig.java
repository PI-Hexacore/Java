package school.sptech;

public class SlackConfig {
    private final boolean ativo;
    private final boolean receberPais;
    private final boolean receberMusica;
    private final boolean receberArtista;

    public SlackConfig(boolean ativo, boolean receberPais, boolean receberMusica, boolean receberArtista) {
        this.ativo = ativo;
        this.receberPais = receberPais;
        this.receberMusica = receberMusica;
        this.receberArtista = receberArtista;
    }

    public boolean isAtivo() { return ativo; }
    public boolean isReceberPais() { return receberPais; }
    public boolean isReceberMusica() { return receberMusica; }
    public boolean isReceberArtista() { return receberArtista; }
}