package school.sptech;

import java.sql.*;

public class ArtistaMusica{

    private static final String URL = "jdbc:mysql://localhost:3306/hexacore?allowPublicKeyRetrieval=true&useSSL=false";
    private static final String USER = "root";
    private static final String PASSWORD = "142536";

    public void importarArtistasEMusicas() {
        int countArtistas = 0;
        int countMusicas = 0;

        String selectDados = """
                SELECT fk_spotify_top, fk_spotify_youtube, nm_artista, nm_track, tp_album,
                       nm_album, cd_rank, nm_pais, qt_stream, ds_genero
                FROM DadosTratados
                """;

        String selectArtista = """
                SELECT id_artista FROM Artista WHERE nm_artista = ?
                """;

        String insertArtista = """
                INSERT INTO Artista (nm_artista, ds_genero_musical, fk_dados_spotify_top, fk_dados_spotify_youtube)
                VALUES (?, ?, ?, ?)
                """;

        String insertMusica = """
                INSERT INTO Musica (nm_track, nm_musica, tp_album, nm_album, rank_pais, nm_pais, qt_stream,
                                    fk_artista, fk_dados_spotify_top, fk_dados_spotify_youtube)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conexao = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmtSelect = conexao.prepareStatement(selectDados);
             PreparedStatement stmtBuscaArtista = conexao.prepareStatement(selectArtista);
             PreparedStatement stmtInsertArtista = conexao.prepareStatement(insertArtista, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement stmtInsertMusica = conexao.prepareStatement(insertMusica)) {

            conexao.setAutoCommit(false);

            ResultSet rs = stmtSelect.executeQuery();

            while (rs.next()) {
                Integer fkSpotifyTop = rs.getInt("fk_spotify_top");
                Integer fkSpotifyYoutube = rs.getInt("fk_spotify_youtube");
                String nomeArtista = rs.getString("nm_artista");
                String nomeTrack = rs.getString("nm_track");
                String tipoAlbum = rs.getString("tp_album");
                String nomeAlbum = rs.getString("nm_album");
                Integer rank = rs.getInt("cd_rank");
                String pais = rs.getString("nm_pais");
                Integer qtStream = rs.getInt("qt_stream");
                String genero = rs.getString("ds_genero");

                stmtBuscaArtista.setString(1, nomeArtista);
                ResultSet rsArtista = stmtBuscaArtista.executeQuery();

                Integer idArtista;
                if (rsArtista.next()) {
                    idArtista = rsArtista.getInt("id_artista");
                } else {

                    stmtInsertArtista.setString(1, nomeArtista);
                    stmtInsertArtista.setString(2, genero);
                    stmtInsertArtista.setInt(3, fkSpotifyTop);
                    stmtInsertArtista.setInt(4, fkSpotifyYoutube);
                    stmtInsertArtista.executeUpdate();

                    ResultSet chaves = stmtInsertArtista.getGeneratedKeys();
                    chaves.next();
                    idArtista = chaves.getInt(1);
                    countArtistas++;
                }

                stmtInsertMusica.setString(1, nomeTrack);
                stmtInsertMusica.setString(2, nomeTrack);
                stmtInsertMusica.setString(3, tipoAlbum);
                stmtInsertMusica.setString(4, nomeAlbum);
                stmtInsertMusica.setInt(5, rank);
                stmtInsertMusica.setString(6, pais);
                stmtInsertMusica.setInt(7, qtStream);
                stmtInsertMusica.setInt(8, idArtista);
                stmtInsertMusica.setInt(9, fkSpotifyTop);
                stmtInsertMusica.setInt(10, fkSpotifyYoutube);

                stmtInsertMusica.addBatch();
                countMusicas++;
            }

            stmtInsertMusica.executeBatch();
            conexao.commit();

            Main.registrarLog("Artista", "SUCESSO", countArtistas, null);
            Main.registrarLog("Musica", "SUCESSO", countMusicas, null);

            System.out.printf("Inseridos %d artistas e %d músicas com sucesso.%n", countArtistas, countMusicas);

        } catch (SQLException e) {
            Main.registrarLog("Artista/Musica", "FALHA", 0, e.getMessage());
            e.printStackTrace();
            System.err.println(" Falha ao importar artistas e músicas: " + e.getMessage());
        }
    }
}
