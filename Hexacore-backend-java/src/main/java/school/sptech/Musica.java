package school.sptech;

import java.sql.*;

public class Musica {

    private final Connection conexao;
    // Agregação: música CONHECE artista, mas não depende dele
    private Artista artista;
    public Musica(Connection conexao) {
        this.conexao = conexao;
    }

    private static final String INSERT_MUSICA = """
            INSERT INTO MusicaClient (nm_track, nm_musica, tp_album, nm_album, rank_pais,
                                nm_pais, qt_stream, fk_artista,
                                fk_dados_spotify_top, fk_dados_spotify_youtube)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    public PreparedStatement prepararInsertMusica() throws SQLException {
        return conexao.prepareStatement(INSERT_MUSICA);
    }

    public void adicionarMusicaBatch(
            PreparedStatement stmt,
            String nomeTrack,
            String tipoAlbum,
            String nomeAlbum,
            Integer rank,
            String pais,
            Integer qtStream,
            Integer idArtista,
            Integer fkSpotifyTop,
            Integer fkSpotifyYoutube
    ) throws SQLException {

        stmt.setString(1, nomeTrack);
        stmt.setString(2, nomeTrack);
        stmt.setString(3, tipoAlbum);
        stmt.setString(4, nomeAlbum);
        stmt.setInt(5, rank);
        stmt.setString(6, pais);
        stmt.setInt(7, qtStream);
        stmt.setInt(8, idArtista);
        stmt.setInt(9, fkSpotifyTop);
        stmt.setInt(10, fkSpotifyYoutube);

        stmt.addBatch();
    }

    public Artista getArtista() {
        return artista;
    }

    public void setArtista(Artista artista) {
        this.artista = artista;
    }
}
