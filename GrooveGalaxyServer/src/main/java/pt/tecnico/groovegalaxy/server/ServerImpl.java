package pt.tecnico.groovegalaxy.server;

import pt.ulisboa.tecnico.groovegalaxy.contract.user.Client.*;
import io.grpc.stub.StreamObserver;
import pt.tecnico.groovegalaxy.securedocument.*;
import io.grpc.Status;
import io.grpc.InternalConfigSelector.Result;
import pt.ulisboa.tecnico.groovegalaxy.contract.user.ClientServiceGrpc;

import java.util.List;

import javax.json.Json;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.security.Key;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class ServerImpl extends ClientServiceGrpc.ClientServiceImplBase {
    
    private Connection connection;
    private boolean verbose;

    public ServerImpl(Connection connection, boolean verbose) {
        this.connection = connection;
        this.verbose = verbose;
    }

    public void login(LoginRequest request, StreamObserver<LoginResponse> responseObserver) {
        try {

            if(verbose) {
                System.out.println("Received login request");
                System.out.println("Encrypted password: " + request.getPassword());
            }

            String username = request.getUsername();
            JsonObject encryptedPassword = JsonParser.parseString(request.getPassword()).getAsJsonObject();
            String key = "keys/" + username + ".key";
            String password = new String(Unprotect.unprotect(encryptedPassword , key, false));

            if(verbose)
                System.out.println("Decrypted password: " + password);

            String query = "SELECT * FROM user WHERE username=? AND password=?";
            PreparedStatement pstmt = this.connection.prepareStatement(query);
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet results = pstmt.executeQuery();

            LoginResponse response;

            if (results.next()) {
                String name = results.getString("name");
                query = "SELECT family_name FROM family WHERE family_id=?";
                pstmt = this.connection.prepareStatement(query);    
                pstmt.setString(1, results.getString("family_id"));
                ResultSet results2 = pstmt.executeQuery();
                String familyName = "";
                if(results2.next()){
                    familyName = results2.getString("family_name");
                }
                response = LoginResponse.newBuilder().setSuccess(true).setName(name).setFamilyName(familyName).build();
            } else{
                response = LoginResponse.newBuilder().setSuccess(false).build();
            }
            results.close();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void getSongs(GetSongsRequest request, StreamObserver<GetSongsResponse> responseObserver) {
        try {
            if(verbose)
                System.out.println("Received get songs request");
            String query = "SELECT * FROM music";
            Statement instruction = this.connection.createStatement();
            ResultSet results = instruction.executeQuery(query);
            JsonObject encrypted = getSongsAux(results, "keys/global.key");
            instruction.close();

            if(verbose)
                System.out.println("Encrypted songs: " + encrypted.toString());
            
            instruction.close();
            
            GetSongsResponse response = GetSongsResponse.newBuilder().setSongs(encrypted.toString()).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void getGenres(GetGenresRequest request, StreamObserver<GetGenresResponse> responseObserver) {
        try {
            if(verbose)
                System.out.println("Received get genres request");
                
            String query = "SELECT * FROM genre";
            Statement instruction = this.connection.createStatement();
            ResultSet results = instruction.executeQuery(query);
            JsonObject genres = new JsonObject();
            JsonArray genresArray = new JsonArray();

            while (results.next()) {
                String genre = results.getString("genre_name");
                genresArray.add(genre);
            }
            genres.add("genres", genresArray);

            if(verbose)
                    System.out.println("Decrypted genres: " + genres.toString());

            byte[] genresByte = genres.toString().getBytes();
            String encrypted = Protect.protect(genresByte, "keys/global.key", false).toString();

            if(verbose)
                System.out.println("Encrypted genres: " + encrypted.toString());
        
            GetGenresResponse response = GetGenresResponse.newBuilder().setGenres(encrypted).build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void getSongsByGenre(GetSongsByGenreRequest request, StreamObserver<GetSongsByGenreResponse> responseObserver) {
        try{
            if(verbose)
                System.out.println("Received get songs by genre request");

            String query = "SELECT * FROM music WHERE music_id IN (SELECT music_id FROM has_genre WHERE genre_id=(SELECT genre_id FROM genre WHERE genre_name = ?))";

            PreparedStatement pstmt = this.connection.prepareStatement(query);
            pstmt.setString(1, request.getGenre());
            ResultSet results = pstmt.executeQuery();
            JsonObject encrypted = getSongsAux(results, "keys/global.key");
            
            if(verbose)
                System.out.println("Encrypted songs: " + encrypted.toString());

            GetSongsByGenreResponse response = GetSongsByGenreResponse.newBuilder().setSongs(encrypted.toString()).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getSongsByArtist(GetSongsByArtistRequest request, StreamObserver<GetSongsByArtistResponse> responseObserver) {
        try{
            if(verbose)
                System.out.println("Received get songs by artist request");

            String query = "SELECT * FROM music WHERE artist=?";
            PreparedStatement pstmt = this.connection.prepareStatement(query);
            pstmt.setString(1, request.getArtist());
            ResultSet results = pstmt.executeQuery();
            JsonObject encrypted = getSongsAux(results, "keys/global.key");
            
            if(verbose)
                System.out.println("Encrypted songs: " + encrypted.toString());

            GetSongsByArtistResponse response = GetSongsByArtistResponse.newBuilder().setSongs(encrypted.toString()).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getSongInformation(GetSongInformationRequest request, StreamObserver<GetSongInformationResponse> responseObserver) {
        try{
            if(verbose)
                System.out.println("Received get song information request");

            String query = "SELECT * FROM music WHERE title=?";
            PreparedStatement pstmt = this.connection.prepareStatement(query);
            pstmt.setString(1, request.getSong());
            ResultSet results = pstmt.executeQuery();
            JsonObject encrypted = getSongsAux(results, "keys/global.key");

            if(verbose)
                System.out.println("Encrypted song: " + encrypted.toString());

            GetSongInformationResponse response = GetSongInformationResponse.newBuilder().setSong(encrypted.toString()).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getLyrics(GetLyricsRequest request, StreamObserver<GetLyricsResponse> responseObserver) {
        try {
            if(verbose)
                System.out.println("Received get lyrics request");

            String query = "SELECT * FROM lyrics WHERE music_id = (SELECT music_id FROM music WHERE title=?)";

            PreparedStatement pstmt = this.connection.prepareStatement(query);
            pstmt.setString(1, request.getSong());
            ResultSet results = pstmt.executeQuery();
            JsonObject lyrics = new JsonObject();
            JsonArray lyricsArray = new JsonArray();

            while (results.next()) {
                String genre = results.getString("lyrics");
                lyricsArray.add(genre);
            }
            lyrics.add("lyrics", lyricsArray);

            if(verbose)
                System.out.println("Decrypted lyrics: " + lyrics.toString());

            byte[] lyricsByte = lyrics.toString().getBytes();
            String encrypted = Protect.protect(lyricsByte, "keys/global.key", verbose).toString();

            if(verbose)
                System.out.println("Encrypted lyrics: " + encrypted.toString());
        
            GetLyricsResponse response = GetLyricsResponse.newBuilder().setLyrics(encrypted).build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getPreview(GetPreviewRequest request, StreamObserver<GetPreviewResponse> responseObserver) {
        try {
            if(verbose)
                System.out.println("Received get preview request");
            if(checkUser(request.getUsername(), request.getPassword())){
                String query = "SELECT previewbase64 FROM music WHERE title=?";
                PreparedStatement pstmt = this.connection.prepareStatement(query);
                pstmt.setString(1, request.getSong());
                ResultSet results = pstmt.executeQuery();
                String preview = "";
                if (results.next()) {
                    preview = results.getString("previewbase64");
                }
                JsonObject previewObject = new JsonObject();
                previewObject.addProperty("audio", preview);

                if(verbose)
                    System.out.println("Decrypted song: " + previewObject.toString());

                byte[] previewByte = previewObject.toString().getBytes();

                JsonObject encrypted = Protect.protect(previewByte, "keys/" + request.getUsername() + ".key" , false);

                if(verbose)
                    System.out.println("Encrypted song: " + encrypted.toString());

                GetPreviewResponse response = GetPreviewResponse.newBuilder().setPreview(encrypted.toString()).build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Invalid username or password").asRuntimeException());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getMySongs(GetMySongsRequest request, StreamObserver<GetMySongsResponse> responseObserver) {
        try {
            if(verbose)
                System.out.println("Received get my songs request");
            if(checkUser(request.getUsername(), request.getPassword())){
                String query = "SELECT * FROM music WHERE music_id IN (SELECT music_id FROM has_music WHERE username=?)";
                PreparedStatement pstmt = this.connection.prepareStatement(query);
                pstmt.setString(1, request.getUsername());
                ResultSet results = pstmt.executeQuery();
                JsonObject encrypted = getSongsAux(results, "./keys/" + request.getUsername() + ".key");

                if(verbose)
                    System.out.println("Encrypted songs: " + encrypted.toString());
                
                GetMySongsResponse response = GetMySongsResponse.newBuilder().setSongs(encrypted.toString()).build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Invalid username or password").asRuntimeException());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void play(PlayRequest request, StreamObserver<PlayResponse> responseObserver) {
        try {
            if(verbose)
                System.out.println("Received play request");            
            if(checkUser(request.getUsername(), request.getPassword())) {
                if(hasSong(request.getUsername(), request.getSong())) {
                    String query = "SELECT audiobase64 FROM music WHERE title=?";
                    PreparedStatement pstmt = this.connection.prepareStatement(query);
                    pstmt.setString(1, request.getSong());
                    ResultSet results = pstmt.executeQuery();
                    String audio = "";
                    if (results.next()) {
                        audio = results.getString("audiobase64");
                    }

                    byte[] audioByte = audio.getBytes();
                    String key = "./keys/" + request.getUsername() + ".key";
                    JsonObject encrypted = Protect.protect(audioByte, key, false);

                    if(verbose)
                        System.out.println("Encrypted song: " + encrypted.toString());

                    PlayResponse response = PlayResponse.newBuilder().setSong(encrypted.toString()).build();
                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
                } else
                    responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Family does not have song").asRuntimeException());
            } else
                responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Invalid username or password").asRuntimeException());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void buySong(BuySongRequest request, StreamObserver<BuySongResponse> responseObserver) {
        try {
            if(verbose)
                System.out.println("Received buy song request");
            if(checkUser(request.getUsername(), request.getPassword())){
                boolean success = false;
                String query = "SELECT * FROM has_music WHERE username=? AND music_id=(SELECT music_id FROM music WHERE title=?)";
                PreparedStatement pstmt = this.connection.prepareStatement(query);
                pstmt.setString(1, request.getUsername());
                pstmt.setString(2, request.getSong());
                ResultSet results = pstmt.executeQuery();

                if(!results.next()){
                    query = "INSERT INTO has_music (username, music_id) VALUES (?, (SELECT music_id FROM music WHERE title=?))";
                    pstmt = this.connection.prepareStatement(query);
                    pstmt.setString(1, request.getUsername());
                    pstmt.setString(2, request.getSong());
                    pstmt.executeUpdate();
                    success = true;
                }

                BuySongResponse response = BuySongResponse.newBuilder().setSuccess(success).build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Invalid username or password").asRuntimeException());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createFamily(CreateFamilyRequest request, StreamObserver<CreateFamilyResponse> responseObserver) {
        try {
            if(verbose)
                System.out.println("Received create family request");
            if(checkUser(request.getUsername(), request.getPassword())){
                String query = "SELECT * FROM family WHERE family_name=?";
                PreparedStatement pstmt = this.connection.prepareStatement(query);
                pstmt.setString(1, request.getFamilyName());
                ResultSet results = pstmt.executeQuery();

                CreateFamilyResponse response;

                if(!results.next()){
                    JsonObject encryptedFamilyCode = JsonParser.parseString(request.getFamilyCode()).getAsJsonObject();
                    if(verbose)
                        System.out.println("Encrypted family code: " + encryptedFamilyCode.toString());
                    String familyCode = new String(Unprotect.unprotect(encryptedFamilyCode , "keys/" + request.getUsername() + ".key", false));
                    if(verbose)
                        System.out.println("Decrypted family code: " + familyCode.toString());
                    query = "INSERT INTO family (family_name, family_code) VALUES (?, ?)";
                    pstmt = this.connection.prepareStatement(query);
                    pstmt.setString(1, request.getFamilyName());
                    pstmt.setString(2, familyCode);
                    pstmt.executeUpdate();
                    query = "UPDATE user SET family_id=(SELECT family_id FROM family WHERE family_name=?) WHERE username=?";
                    pstmt = this.connection.prepareStatement(query);
                    pstmt.setString(1, request.getFamilyName());
                    pstmt.setString(2, request.getUsername());
                    pstmt.executeUpdate();

                    // generate family key
                    String filename = "keys/" + request.getFamilyName() + ".key";
                    byte[] familyKey = Protect.generateKey(filename);
                    String key = "keys/" + request.getUsername() + ".key";
                    String encryptedFamilyKey = Protect.protect(familyKey, key, false).toString();
                    response = CreateFamilyResponse.newBuilder().setSuccess(true).setFamilyKey(encryptedFamilyKey).build();
                } else { 
                    response = CreateFamilyResponse.newBuilder().setSuccess(false).build();
                }

                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Invalid username or password").asRuntimeException());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void joinFamily(JoinFamilyRequest request, StreamObserver<JoinFamilyResponse> responseObserver) {
        try {
            if(verbose)
                System.out.println("Received join family request");
            if(checkUser(request.getUsername(), request.getPassword())){
                JsonObject encryptedFamilyCode = JsonParser.parseString(request.getFamilyCode()).getAsJsonObject();
                
                if(verbose)
                    System.out.println("Encrypted family code: " + encryptedFamilyCode.toString());
                
                String familyCode = new String(Unprotect.unprotect(encryptedFamilyCode , "keys/" + request.getUsername() + ".key", false));
                
                if(verbose)
                    System.out.println("Decrypted family code: " + familyCode.toString());

                String query = "SELECT * FROM family WHERE family_name=? AND family_code=?";
                PreparedStatement pstmt = this.connection.prepareStatement(query);
                pstmt.setString(1, request.getFamilyName());
                pstmt.setString(2, familyCode);  
                ResultSet results = pstmt.executeQuery();
                JoinFamilyResponse response;

                if(results.next()){
                    query = "UPDATE user SET family_id=? WHERE username=?";
                    pstmt = this.connection.prepareStatement(query);
                    pstmt.setString(1, results.getString("family_id"));
                    pstmt.setString(2, request.getUsername());
                    pstmt.executeUpdate();

                    // get family key
                    String filename = "keys/" + request.getFamilyName() + ".key";
                    byte[] familyKey = Protect.readKey(filename, false).getEncoded();
                    String key = "keys/" + request.getUsername() + ".key";
                    String encryptedFamilyKey = Protect.protect(familyKey, key, false).toString();
                    response = JoinFamilyResponse.newBuilder().setSuccess(true).setFamilyKey(encryptedFamilyKey).build();

                } else {
                    response = JoinFamilyResponse.newBuilder().setSuccess(false).build();
                }

                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Invalid username or password").asRuntimeException());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void destroyFamily(DestroyFamilyRequest request, StreamObserver<DestroyFamilyResponse> responseObserver) {
        try {
            if(verbose)
                System.out.println("Received destroy family request");
            if(checkUser(request.getUsername(), request.getPassword())){
                boolean success = false;
                
                String query = "SELECT * FROM family WHERE family_id=(SELECT family_id FROM user WHERE username=?)";
                PreparedStatement pstmt = this.connection.prepareStatement(query);
                pstmt.setString(1, request.getUsername());
                ResultSet results = pstmt.executeQuery();
                results.next();
                String familyName = results.getString("family_name");

                JsonObject encryptedFamilyCode = JsonParser.parseString(request.getFamilyCode()).getAsJsonObject();

                if(verbose)
                    System.out.println("Encrypted family code: " + encryptedFamilyCode.toString());

                String familyCode = new String(Unprotect.unprotect(encryptedFamilyCode , "keys/" + familyName + ".key", false));

                if(verbose)
                    System.out.println("Decrypted family code: " + familyCode.toString());

                query = "SELECT * FROM family WHERE family_id=(SELECT family_id FROM user WHERE username=?) AND family_code=?";
                pstmt = this.connection.prepareStatement(query);
                pstmt.setString(1, request.getUsername());
                pstmt.setString(2, familyCode);
                results = pstmt.executeQuery();

                if(results.next()){
                    query = "UPDATE user SET family_id=NULL WHERE family_id=?";
                    pstmt = this.connection.prepareStatement(query);
                    pstmt.setString(1, results.getString("family_id"));
                    pstmt.executeUpdate();
                    query = "DELETE FROM family WHERE family_id=?";
                    pstmt = this.connection.prepareStatement(query);
                    pstmt.setString(1, results.getString("family_id"));
                    pstmt.executeUpdate();
                    success = true;
                }

                DestroyFamilyResponse response = DestroyFamilyResponse.newBuilder().setSuccess(success).build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Invalid username or password").asRuntimeException());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void listFamily(ListFamilyRequest request, StreamObserver<ListFamilyResponse> responseObserver) {
        try {
            if(verbose)
                System.out.println("Received list family request");
            if(checkUser(request.getUsername(), request.getPassword())){
                // get family name
                String query = "SELECT family_name FROM family WHERE family_id=(SELECT family_id FROM user WHERE username=?)";
                PreparedStatement pstmt = this.connection.prepareStatement(query);
                pstmt.setString(1, request.getUsername());
                ResultSet results2 = pstmt.executeQuery();
                String familyName = "";
                ListFamilyResponse response;
                if(results2.next()){
                    familyName = results2.getString("family_name");

                    query = "SELECT name FROM user WHERE family_id=(SELECT family_id FROM user WHERE username=?)";
                    pstmt = this.connection.prepareStatement(query);
                    pstmt.setString(1, request.getUsername());
                    ResultSet results = pstmt.executeQuery();
                    JsonObject family = new JsonObject();
                    JsonArray familyArray = new JsonArray();

                    while (results.next()) {
                        String userName = results.getString("name");
                        familyArray.add(userName);
                    }
                    family.add("family", familyArray);

                    if(verbose)
                        System.out.println("Decrypted family: " + family.toString());

                    byte[] familyByte = family.toString().getBytes();
                    String key = "./keys/" + familyName + ".key";
                    String encrypted = Protect.protect(familyByte, key, false).toString();

                    if(verbose)
                        System.out.println("Encrypted family: " + family.toString());

                    response = ListFamilyResponse.newBuilder().setFamily(encrypted).setFamilyName(familyName).build();

                } else {
                    response = ListFamilyResponse.newBuilder().setFamily("").build();
                }
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Invalid username or password").asRuntimeException());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void getFamilySongs(GetFamilySongsRequest request, StreamObserver<GetFamilySongsResponse> responseObserver) {
        try {
            if(verbose)
                System.out.println("Received get family songs request");
            if(checkUser(request.getUsername(), request.getPassword())){
                String query = "SELECT family_name FROM family WHERE family_id=(SELECT family_id FROM user WHERE username=?)";
                PreparedStatement pstmt = this.connection.prepareStatement(query);
                pstmt.setString(1, request.getUsername());
                ResultSet results2 = pstmt.executeQuery();
                String familyName = "";
                GetFamilySongsResponse response;
                if(results2.next()){
                    familyName = results2.getString("family_name");

                    query = "SELECT * FROM music WHERE music_id IN (SELECT music_id FROM has_music WHERE username IN (SELECT username FROM user WHERE family_id=(SELECT family_id FROM user WHERE username=?)))";
                    pstmt = this.connection.prepareStatement(query);
                    pstmt.setString(1, request.getUsername());
                    ResultSet results = pstmt.executeQuery();
                    JsonObject encrypted = getSongsAux(results, "./keys/" + familyName + ".key");

                    if(verbose)
                        System.out.println("Encrypted songs: " + encrypted.toString());
                    
                    System.out.println("Sending songs to client");
                    response = GetFamilySongsResponse.newBuilder().setSongs(encrypted.toString()).build();
                } else{
                    response = GetFamilySongsResponse.newBuilder().setSongs("").build();
                }
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } else {
                responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Invalid username or password").asRuntimeException());
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playFamilySong(PlayFamilySongRequest request, StreamObserver<PlayFamilySongResponse> responseObserver) {
        try {
            if(verbose)
                System.out.println("Received play family song request");
            if(checkUser(request.getUsername(), request.getPassword())){
                if(hasFamilySong(request.getUsername(), request.getSong())){
                    String query = "SELECT family_name FROM family WHERE family_id=(SELECT family_id FROM user WHERE username=?)";
                    PreparedStatement pstmt = this.connection.prepareStatement(query);
                    pstmt.setString(1, request.getUsername());
                    ResultSet results2 = pstmt.executeQuery();
                    String familyName = "";
                    if(results2.next()){
                        familyName = results2.getString("family_name");

                        query = "SELECT audiobase64 FROM music WHERE title=?";
                        pstmt = this.connection.prepareStatement(query);
                        pstmt.setString(1, request.getSong());
                        ResultSet results = pstmt.executeQuery();
                        String audio = "";
                        if (results.next()) {
                            audio = results.getString("audiobase64");
                        }

                        if(verbose)
                            System.out.println("Decrypted song: " + audio);

                        byte[] audioByte = audio.getBytes();
                        String key = "./keys/" + familyName + ".key";
                        JsonObject encrypted = Protect.protect(audioByte, key, false);

                        if(verbose)
                            System.out.println("Encrypted song: " + encrypted.toString());

                        PlayFamilySongResponse response = PlayFamilySongResponse.newBuilder().setSong(encrypted.toString()).build();
                        responseObserver.onNext(response);
                        responseObserver.onCompleted();
                    }
                    PlayFamilySongResponse response = PlayFamilySongResponse.newBuilder().setSong("").build();
                } else {
                    responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("User does not have song").asRuntimeException());
                }
            } else {
                responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Invalid username or password").asRuntimeException());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //---------------------------------------------------------------------------------
    //                                    Auxiliar                                     
    //---------------------------------------------------------------------------------

    private JsonObject getSongsAux(ResultSet results, String key){
        try {
            JsonObject songs = new JsonObject();
            JsonArray songsArray = new JsonArray();

            while (results.next()) {
                int music_id = results.getInt("music_id");
                String format = results.getString("music_format");
                String artist = results.getString("artist");
                String title = results.getString("title");
                
                String query = "SELECT * FROM genre WHERE genre_id IN (SELECT genre_id FROM has_genre WHERE music_id = " + music_id + ")";
                
                Statement instruction2 = this.connection.createStatement();
                ResultSet results2 = instruction2.executeQuery(query);
                List<String> genres = new ArrayList<String>();
                while (results2.next()) {
                    String genre = results2.getString("genre_name");
                    genres.add(genre);
                }
                results2.close();
                instruction2.close();
                JsonObject song = createSongJson(artist, title, format, genres);
                songsArray.add(song);
            }
            songs.add("songs", songsArray);
            results.close();

            byte[] songsByte = songs.toString().getBytes();

            if(verbose)
                System.out.println("Decrypted songs: " + songs.toString());

            return Protect.protect(songsByte, key, false);

        } catch(Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    private static JsonObject createSongJson(String artist, String title, String format, List<String> genre){
        JsonObject jsonObject = new JsonObject();
        JsonObject mediaObject = new JsonObject();
        JsonObject mediaInfoObject = new JsonObject();
        mediaInfoObject.addProperty("format", format);
        mediaInfoObject.addProperty("artist", artist);
        mediaInfoObject.addProperty("title", title);
        Gson gson = new Gson();
        String jsonString = gson.toJson(genre);
        JsonArray genreArray = gson.fromJson(jsonString, JsonArray.class);
        
        mediaInfoObject.add("genre", genreArray);
        mediaObject.add("mediaInfo", mediaInfoObject);
        
        jsonObject.add("media", mediaObject);

        return jsonObject;
    }

    private boolean checkUser(String username, String password) throws Exception{
        JsonObject encryptedPassword = JsonParser.parseString(password).getAsJsonObject();
        password = new String(Unprotect.unprotect(encryptedPassword , "keys/" + username + ".key", false));
        String query = "SELECT * FROM user WHERE username=? AND password=?";
        PreparedStatement pstmt = this.connection.prepareStatement(query);
        pstmt.setString(1, username);
        pstmt.setString(2, password);
        ResultSet results = pstmt.executeQuery();

        if (results.next()) {
            return true;
        } else{
            return false;
        }
    }

    private boolean hasSong(String username, String song) throws Exception{
        String query = "SELECT * FROM has_music WHERE username=? AND music_id=(SELECT music_id FROM music WHERE title=?)";
        PreparedStatement pstmt = this.connection.prepareStatement(query);
        pstmt.setString(1, username);
        pstmt.setString(2, song);
        ResultSet results = pstmt.executeQuery();

        if (results.next()) {
            return true;
        } else{
            return false;
        }
    }
    
    private boolean hasFamilySong(String username, String song){
        try {
            String query = "SELECT family_name FROM family WHERE family_id=(SELECT family_id FROM user WHERE username=?)";
            PreparedStatement pstmt = this.connection.prepareStatement(query);
            pstmt.setString(1, username);
            ResultSet results2 = pstmt.executeQuery();
            if(results2.next()){
                query = "SELECT * FROM music WHERE title=? AND music_id IN (SELECT music_id FROM has_music WHERE username IN (SELECT username FROM user WHERE family_id=(SELECT family_id FROM user WHERE username=?)))";
                pstmt = this.connection.prepareStatement(query);
                pstmt.setString(1, song);
                pstmt.setString(2, username);
                ResultSet results = pstmt.executeQuery();

                if (results.next()) {
                    return true;
                } else{
                    return false;
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //---------------------------------------------------------------------------------
    //                                 Demonstration                                     
    //---------------------------------------------------------------------------------

    public void hackme(HackmeRequest request, StreamObserver<HackmeResponse> responseObserver) {
        try {
            String query = "SELECT * FROM genre";
            Statement instruction = this.connection.createStatement();
            ResultSet results = instruction.executeQuery(query);
            JsonObject genres = new JsonObject();
            JsonArray genresArray = new JsonArray();

            while (results.next()) {
                String genre = results.getString("genre_name");
                genresArray.add(genre);
            }
            genres.add("genres", genresArray);

            byte[] genresByte = genres.toString().getBytes();

            String encrypted = Protect.protect(genresByte, "keys/global.key", false).toString();
        
            HackmeResponse response = HackmeResponse.newBuilder().setContent(encrypted).build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

} 