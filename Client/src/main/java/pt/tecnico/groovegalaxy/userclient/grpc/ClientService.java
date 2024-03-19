package pt.tecnico.groovegalaxy.userclient.grpc;
import java.util.Scanner;

import javax.json.Json;

import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import pt.ulisboa.tecnico.groovegalaxy.contract.user.ClientServiceGrpc;
import pt.ulisboa.tecnico.groovegalaxy.contract.user.Client.*;
import pt.tecnico.groovegalaxy.securedocument.*;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;

public class ClientService {
    private Scanner scanner;
    private ClientServiceGrpc.ClientServiceBlockingStub blockingStub;
    private boolean login = false;
    private String name;
    private String username;
    private String password;
    private String key;
    private String familyKey;

    public ClientService(ManagedChannel channel) {
        this.scanner = new Scanner(System.in);
        this.blockingStub = ClientServiceGrpc.newBlockingStub(channel);
    }

    public void parseInput() {
        System.out.println();
        System.out.println("Welcome to GrooveGalaxy :)");
        System.out.println("Here you can listen your favorite songs");
        while(true) {
            System.out.println();
            System.out.println("Please choose an option:");
            System.out.println();
            if(!login)
                System.out.println("    Login your account --------------------------- login");
            else
                System.out.println("    Logout of your account ----------------------- logout");
            System.out.println("    See all songs -------------------------------- getSongs");
            System.out.println("    See all genres ------------------------------- getGenres");
            System.out.println("    See all songs for a specific genre ----------- getSongsByGenre");
            System.out.println("    See all songs for a specific artist ---------- getSongsByArtist");
            System.out.println("    See the information for a specific song ------ getSongInformation");
            System.out.println("    See the lyrics for a specific song ----------- getLyrics");
            if(login){
                System.out.println("    See all of your songs ------------------------ getMySongs");
                System.out.println("    Get a preview from a song -------------------- getPreview");
                System.out.println("    Buy a song ----------------------------------- buySong");
                System.out.println("    Play a song ---------------------------------- play");
                System.out.println("    Play a song from wherever you want ----------- playMiddle");
                System.out.println("    Family plan menu ----------------------------- family");
            }
            System.out.println("    Exit the application ------------------------- exit");
            System.out.println();
            System.out.printf("Insert one command > ");
            String input = scanner.nextLine();
            System.out.println();
            String[] inputArray = input.split(" ");
            String command = inputArray[0];

            if (inputArray.length != 1)
                command = "toomany";

            String[] args = new String[inputArray.length - 1];
            String arg = "";
            for (int i = 1; i < inputArray.length; i++) {
                args[i - 1] = inputArray[i];
                arg += inputArray[i];
                if(i != inputArray.length - 1)
                    arg += " ";
            }
            
            switch (command) {
                case "login":
                    if(login) {
                        System.out.println("Error: Please do logout before logging in.");
                        break;
                    }
                    String lUsername, lPassword;
                    System.out.printf("Enter your username > ");
                    input = scanner.nextLine();
                    lUsername = input;
                    System.out.printf("Enter your password > ");
                    input = scanner.nextLine();
                    lPassword = input;
                    if (lUsername.equals("") || lPassword.equals("")) {
                        System.out.println("Error: Invalid username or password.");
                        break;
                    }
                    login(lUsername, lPassword);
                    break;
                case "logout":
                    logout();
                    break;
                case "getSongs":
                    getSongs();
                    break;
                case "getGenres":
                    getGenres();
                    break;
                case "getSongsByGenre":
                    System.out.printf("Enter the genre > ");
                    input = scanner.nextLine();
                    if (input.equals("")) {
                        System.out.println("Error: Do not enter an empty genre.");
                        break;
                    }
                    getSongsByGenre(input);
                    break;
                case "getSongsByArtist":
                    System.out.printf("Enter the artist > ");
                    input = scanner.nextLine();
                    if (input.equals("")) {
                        System.out.println("Error: Do not enter an empty artist.");
                        break;
                    }
                    getSongsByArtist(input);
                    break;
                case "getSongInformation":
                    System.out.printf("Enter a song name > ");
                    input = scanner.nextLine();
                    if (input.equals("")) {
                        System.out.println("Error: Do not enter an empty song name.");
                        break;
                    }
                    getSongInformation(input);
                    break;
                case "getLyrics":
                    System.out.printf("Enter a song name > ");
                    input = scanner.nextLine();
                    if (input.equals("")) {
                        System.out.println("Error: Do not enter an empty song name.");
                        break;
                    }
                    getLyrics(input);
                    break;
                case "getPreview":
                    if (!login) {
                        System.out.println("Error: Please login before.");
                        break;
                    }
                    System.out.printf("Enter a song name > ");
                    input = scanner.nextLine();
                    if (input.equals("")) {
                        System.out.println("Error: Do not enter an empty song name.");
                        break;
                    }
                    getPreview(input);
                    break;
                case "getMySongs":
                    if (!login) {
                        System.out.println("Error: Please login before.");
                        break;
                    }
                    getMySongs();
                    break;
                case "play":
                    if (!login) {
                        System.out.println("Error: Please login before.");
                        break;
                    }
                    System.out.printf("Enter a song name > ");
                    input = scanner.nextLine();
                    if (input.equals("")) {
                        System.out.println("Error: Do not enter an empty song name.");
                        break;
                    }
                    play(input);
                    break;
                case "playMiddle":
                    if (!login) {
                        System.out.println("Error: Please login before.");
                        break;
                    }
                    System.out.printf("Enter a song name > ");
                    input = scanner.nextLine();
                    if (input.equals("")) {
                        System.out.println("Error: Do not enter an empty song name.");
                        break;
                    }
                    System.out.printf("Enter time to start song > ");
                    String start = scanner.nextLine();
                    if (input.equals("")) {
                        System.out.println("Error: Do not enter an empty song name.");
                        break;
                    }
                    int startInt = Integer.parseInt(start);
                    playMiddle(input, startInt);
                    break;
                case "buySong":
                    System.out.printf("Enter a song name > ");
                    input = scanner.nextLine();
                    if (input.equals("")) {
                        System.out.println("Error: Do not enter an empty song name.");
                        break;
                    }
                    buySong(input);
                    break;
                case "family":
                    if (!login) {
                        System.out.println("Error: Please login before.");
                        break;
                    }
                    while(true){
                        System.out.println("Welcome to the family section!");
                        System.out.println("Please choose an option:");
                        System.out.println();
                        System.out.println("    Create a family --------------- create");                    
                        System.out.println("    Join family ------------------- join");
                        System.out.println("    Destroy family ---------------- destroy");
                        System.out.println("    List your family -------------- list");
                        System.out.println();
                        System.out.println("    Get my family songs ----------- getFamilySongs");
                        System.out.println("    Play family song -------------- play");
                        System.out.println("    Play family song from middle -- playMiddle");
                        System.out.println("    Exit the family section ------- exit");
                        System.out.println();
                        System.out.printf("Enter the option you want > ");
                        input = scanner.nextLine();
                        if (input.equals("")) {
                            System.out.println("Error: Do not enter an empty option.");
                            break;
                        } else if (input.equals("exit"))
                            break;
                        else
                            family(input);
                    }
                    break;
                case "hackme":
                    hackme();
                    break;
                case "exit":
                    System.out.println("exit");
                    logout();
                    return;
                case "toomany":
                    System.out.println("Error: Please insert only the name of the command.");
                    break;
                default:
                    System.out.println("Error: Invalid command.");
                    break;
            }
        }
    }

    public void login(String username, String password){
		try {
            byte[] passwordByte = password.getBytes();
            String key = "./keys/" + username + ".key";
            JsonObject encryptedPassword = Protect.protect(passwordByte, key, false);
            String encryptedPasswordString = encryptedPassword.toString();
			LoginRequest request = LoginRequest.newBuilder().setUsername(username).setPassword(encryptedPasswordString).build();
			LoginResponse response = blockingStub.login(request);
            if(response.getSuccess()) {
                this.login = true;
                this.name = response.getName();
                this.username = username;
                this.password = password;
                this.key = "./keys/" + username + ".key";
                if(response.getFamilyName()!="")
                    this.familyKey = "./keys/" + response.getFamilyName() + ".key";
                System.out.println("Welcome " + this.name);
            } else {
                System.out.println("Invalid credentials");
            }
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public void logout() {
        if(login = true) {
            login = false;
            username = "";
            password = "";
            System.out.println("Logged out");
        }
        else
            System.out.println("No user logged in");
	}

    public void getSongs() {
        try {
            GetSongsRequest request = GetSongsRequest.newBuilder().build();
            GetSongsResponse response = blockingStub.getSongs(request);

            String responseString = response.getSongs();
            JsonObject songs = JsonParser.parseString(responseString).getAsJsonObject();

            songs = JsonParser.parseString(new String(Unprotect.unprotect(songs, "keys/global.key", false))).getAsJsonObject();
            printSongs(songs);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void getGenres() {
        try {
            GetGenresRequest request = GetGenresRequest.newBuilder().build();
            GetGenresResponse response = blockingStub.getGenres(request);

            String responseString = response.getGenres();
            JsonObject genres = JsonParser.parseString(responseString).getAsJsonObject();

            genres = JsonParser.parseString(new String(Unprotect.unprotect(genres, "keys/global.key", false))).getAsJsonObject();

            JsonArray genresArray = genres.getAsJsonArray("genres");
            for (int i = 0; i < genresArray.size(); i++) {
                System.out.println(" - " + genresArray.get(i).getAsString());
            }
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void getSongsByGenre(String genre) {
        try {
            GetSongsByGenreRequest request = GetSongsByGenreRequest.newBuilder().setGenre(genre).build();
            GetSongsByGenreResponse response = blockingStub.getSongsByGenre(request);

            String responseString = response.getSongs();
            JsonObject songs = JsonParser.parseString(responseString).getAsJsonObject();

            songs = JsonParser.parseString(new String(Unprotect.unprotect(songs, "keys/global.key", false))).getAsJsonObject();
            printSongs(songs);
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void getSongsByArtist(String artist) {
        try {
            GetSongsByArtistRequest request = GetSongsByArtistRequest.newBuilder().setArtist(artist).build();
            GetSongsByArtistResponse response = blockingStub.getSongsByArtist(request);

            String responseString = response.getSongs();
            // convert string to json
            JsonObject songs = JsonParser.parseString(responseString).getAsJsonObject();

            songs = JsonParser.parseString(new String(Unprotect.unprotect(songs, "keys/global.key", false))).getAsJsonObject();
            printSongs(songs);
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void getSongInformation(String song_name) {
        try{
            GetSongInformationRequest request = GetSongInformationRequest.newBuilder().setSong(song_name).build();
            GetSongInformationResponse response = blockingStub.getSongInformation(request);

            String responseString = response.getSong();
            JsonObject song = JsonParser.parseString(responseString).getAsJsonObject();

            song = JsonParser.parseString(new String(Unprotect.unprotect(song, "keys/global.key", false))).getAsJsonObject();
            printSongs(song);
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void getLyrics(String song) {
        try{
            GetLyricsRequest request = GetLyricsRequest.newBuilder().setSong(song).build();
            GetLyricsResponse response = blockingStub.getLyrics(request);
            
            String responseString = response.getLyrics();
            // convert string to json
            JsonObject lyrics = JsonParser.parseString(responseString).getAsJsonObject();

            lyrics = JsonParser.parseString(new String(Unprotect.unprotect(lyrics, "keys/global.key", false))).getAsJsonObject();
            JsonArray lyriscsArray = lyrics.getAsJsonArray("lyrics");
            System.out.println("Lyrics:");
            for (int i = 0; i < lyriscsArray.size(); i++) {
                System.out.println("\t\t" + lyriscsArray.get(i).getAsString());
            }
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void getPreview(String song) {
        try{
            byte[] passwordByte = this.password.getBytes();
            JsonObject encryptedPassword = Protect.protect(passwordByte, this.key, false);
            String encryptedPasswordString = encryptedPassword.toString();

            GetPreviewRequest request = GetPreviewRequest.newBuilder().setSong(song).setUsername(this.username).setPassword(encryptedPasswordString).build();
            GetPreviewResponse response = blockingStub.getPreview(request);
            
            String responseString = response.getPreview();
            JsonObject preview = JsonParser.parseString(responseString).getAsJsonObject();

            preview = JsonParser.parseString(new String(Unprotect.unprotect(preview, this.key, false))).getAsJsonObject();
            String previewString = preview.get("audio").getAsString();

            byte[] audioByte = Base64.getDecoder().decode(previewString);
            File audioFile = saveAudioToFile(audioByte);
            
            openMediaPlayer(audioFile);
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void getMySongs() {
        try {
            byte[] passwordByte = this.password.getBytes();
            JsonObject encryptedPassword = Protect.protect(passwordByte, this.key, false);
            String encryptedPasswordString = encryptedPassword.toString();
            GetMySongsRequest request = GetMySongsRequest.newBuilder().setUsername(this.username).setPassword(encryptedPasswordString).build();
            GetMySongsResponse response = blockingStub.getMySongs(request);

            String responseString = response.getSongs();
            JsonObject songs = JsonParser.parseString(responseString).getAsJsonObject();

            songs = JsonParser.parseString(new String(Unprotect.unprotect(songs, this.key, false))).getAsJsonObject();
            printSongs(songs);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void play(String song) {
        try{
            byte[] passwordByte = this.password.getBytes();
            JsonObject encryptedPassword = Protect.protect(passwordByte, this.key, false);
            String encryptedPasswordString = encryptedPassword.toString();

            PlayRequest request = PlayRequest.newBuilder().setSong(song).setUsername(this.username).setPassword(encryptedPasswordString).build();
            PlayResponse response = blockingStub.play(request);
            
            String responseString = response.getSong();
            JsonObject audio = JsonParser.parseString(responseString).getAsJsonObject();

            String audioString = new String(Unprotect.unprotect(audio, this.key, false));
            
            byte[] audioByte = Base64.getDecoder().decode(audioString);
            File audioFile = saveAudioToFile(audioByte);
            
            openMediaPlayer(audioFile);
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void playMiddle(String song, int start) {
        try{
            byte[] passwordByte = this.password.getBytes();
            JsonObject encryptedPassword = Protect.protect(passwordByte, this.key, false);
            String encryptedPasswordString = encryptedPassword.toString();

            PlayRequest request = PlayRequest.newBuilder().setSong(song).setUsername(this.username).setPassword(encryptedPasswordString).build();
            PlayResponse response = blockingStub.play(request);
            
            String responseString = response.getSong();
            JsonObject audio = JsonParser.parseString(responseString).getAsJsonObject();
        
            String audioString = new String(Unprotect.unprotectBlock(audio, this.key, false, start));
            System.out.println(audioString);


        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void buySong(String song) {
        try{
            byte[] passwordByte = this.password.getBytes();
            JsonObject encryptedPassword = Protect.protect(passwordByte, this.key, false);
            String encryptedPasswordString = encryptedPassword.toString();

            BuySongRequest request = BuySongRequest.newBuilder().setUsername(this.username).setPassword(encryptedPasswordString).setSong(song).build();
            BuySongResponse response = blockingStub.buySong(request);
            
            if(response.getSuccess())
                System.out.println("Song purchased successfuly.\n");
            else
                System.out.println("Error: Song not purchased.\n");
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void family(String option) {
        String input;
        String input2;
        switch (option) {
            case "create":
                System.out.printf("Enter the name of the family you want to create > ");
                input = scanner.nextLine();
                System.out.printf("Enter the family code > ");
                input2 = scanner.nextLine();
                if (input.equals("")) {
                    System.out.println("Error: Do not enter an empty family name.");
                    break;
                }
                createFamily(input, input2);
                break;
            case "join":
                System.out.printf("Enter the name of the family > ");
                input = scanner.nextLine();
                System.out.printf("Enter the code of the family > ");
                input2 = scanner.nextLine();
                if (input.equals("")) {
                    System.out.println("Error: Do not enter an empty username.");
                    break;
                }
                joinFamily(input, input2);
                break;
            case "destroy":
                System.out.printf("Are you sure? [Y/n] ");
                input = scanner.nextLine();
                if(input.equals("Y") || input.equals("y")){
                    System.out.printf("Insert the family code > ");
                    input = scanner.nextLine();
                    destroyFamily(input);
                } else {
                    System.out.println("You did not destroy the family.");
                }
                break;
            case "list":
                listFamily();
                break;
            case "getFamilySongs":
                getFamilySongs();
                break;
            case "play":
                System.out.printf("Enter a song name > ");
                input = scanner.nextLine();
                if (input.equals("")) {
                    System.out.println("Error: Do not enter an empty song name.");
                    break;
                }
                playFamilySong(input);
                break;
            case "playMiddle":
                System.out.printf("Enter a song name > ");
                String song = scanner.nextLine();
                if (song.equals("")) {
                    System.out.println("Error: Do not enter an empty song name.");
                    break;
                }
                System.out.printf("Enter time to start song > ");
                String start = scanner.nextLine();
                int startInt = Integer.parseInt(start);
                playMiddleFamilySong(song, startInt);
                break;
            default:
                System.out.println("Error: Invalid option.");
                break;
        }
    }

    public void createFamily(String familyName, String familyCode) {
        try{
            byte[] passwordByte = this.password.getBytes();
            JsonObject encryptedPassword = Protect.protect(passwordByte, this.key, false);
            String encryptedPasswordString = encryptedPassword.toString();

            byte[] familyCodeByte = familyCode.getBytes();
            JsonObject encryptedFamilyCode = Protect.protect(familyCodeByte, this.key, false);
            String encryptedFamilyCodeString = encryptedFamilyCode.toString();
            
            CreateFamilyRequest request = CreateFamilyRequest.newBuilder().setUsername(this.username).setPassword(encryptedPasswordString).setFamilyName(familyName).setFamilyCode(encryptedFamilyCodeString).build();
            CreateFamilyResponse response = blockingStub.createFamily(request);
            if(response.getSuccess()) {
                String encryptedFamilyKey = response.getFamilyKey();
                JsonObject familyKey = JsonParser.parseString(encryptedFamilyKey).getAsJsonObject();  
                byte[] familyKeyBytes = Unprotect.unprotect(familyKey, this.key, false);
                
                String filename = "./keys/" + familyName + ".key";
                Protect.writeKey(filename, familyKeyBytes);
                this.familyKey = filename;

                System.out.println("Family created successfuly.\n");
            } else
                System.out.println("Error: Family not created.\n");
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void joinFamily(String familyName, String familyCode) {
        try{
            byte[] passwordByte = this.password.getBytes();
            JsonObject encryptedPassword = Protect.protect(passwordByte, this.key, false);
            String encryptedPasswordString = encryptedPassword.toString();

            byte[] familyCodeByte = familyCode.getBytes();
            JsonObject encryptedFamilyCode = Protect.protect(familyCodeByte, this.key, false);
            String encryptedFamilyCodeString = encryptedFamilyCode.toString();

            JoinFamilyRequest request = JoinFamilyRequest.newBuilder().setUsername(this.username).setPassword(encryptedPasswordString).setFamilyName(familyName).setFamilyCode(encryptedFamilyCodeString).build();
            JoinFamilyResponse response = blockingStub.joinFamily(request);

            if(response.getSuccess()){
                String encryptedFamilyKey = response.getFamilyKey();
                JsonObject familyKey = JsonParser.parseString(encryptedFamilyKey).getAsJsonObject();  
                byte[] familyKeyBytes = Unprotect.unprotect(familyKey, this.key, false);
                
                String filename = "./keys/" + familyName + ".key";
                Protect.writeKey(filename, familyKeyBytes);
                this.familyKey = filename;
                System.out.println("You are now part of the family <3.\n");
            } else
                System.out.println("Error: Cannot add you to the family.\n"); 
            } catch(Exception e) {
                System.out.println(e.getMessage());
        }
    }

    public void destroyFamily(String familyCode) {
        try {
            byte[] passwordByte = this.password.getBytes();
            String encryptedPassword = Protect.protect(passwordByte, this.key, false).toString();
            String encryptedFamilyCode = Protect.protect(familyCode.getBytes(), this.familyKey, false).toString();

            DestroyFamilyRequest request = DestroyFamilyRequest.newBuilder().setUsername(this.username).setPassword(encryptedPassword).setFamilyCode(encryptedFamilyCode).build();
            DestroyFamilyResponse response = blockingStub.destroyFamily(request);

            if(response.getSuccess())
                System.out.println("Family destroyed successfuly.\n");
            else
                System.out.println("Error: Family not destroyed.\n");
        
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void listFamily() {
        try{
            byte[] passwordByte = this.password.getBytes();
            JsonObject encryptedPassword = Protect.protect(passwordByte, this.key, false);
            String encryptedPasswordString = encryptedPassword.toString();

            ListFamilyRequest request = ListFamilyRequest.newBuilder().setUsername(this.username).setPassword(encryptedPasswordString).build();
            ListFamilyResponse response = blockingStub.listFamily(request);

            String responseString = response.getFamily();
            
            String familyName = response.getFamilyName();

            System.out.println();
            System.out.println("Family name: " + familyName);
            
            JsonObject family = JsonParser.parseString(responseString).getAsJsonObject();

            family = JsonParser.parseString(new String(Unprotect.unprotect(family, this.familyKey, false))).getAsJsonObject();
            JsonArray familyArray = family.getAsJsonArray("family");
            System.out.println("Members:");
            for (int i = 0; i < familyArray.size(); i++) {
                System.out.println("\t" + familyArray.get(i).getAsString());
            }
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void getFamilySongs() {
        try{
            byte[] passwordByte = this.password.getBytes();
            JsonObject encryptedPassword = Protect.protect(passwordByte, this.key, false);
            String encryptedPasswordString = encryptedPassword.toString();

            GetFamilySongsRequest request = GetFamilySongsRequest.newBuilder().setUsername(this.username).setPassword(encryptedPasswordString).build();
            GetFamilySongsResponse response = blockingStub.getFamilySongs(request);
            
            String responseString = response.getSongs();
            JsonObject songs = JsonParser.parseString(responseString).getAsJsonObject();

            songs = JsonParser.parseString(new String(Unprotect.unprotect(songs, this.familyKey, false))).getAsJsonObject();
            printSongs(songs);
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void playFamilySong(String song) {
        try{
            byte[] passwordByte = this.password.getBytes();
            JsonObject encryptedPassword = Protect.protect(passwordByte, this.key, false);
            String encryptedPasswordString = encryptedPassword.toString();

            PlayFamilySongRequest request = PlayFamilySongRequest.newBuilder().setUsername(this.username).setPassword(encryptedPasswordString).setSong(song).build();
            PlayFamilySongResponse response = blockingStub.playFamilySong(request);
            
            String responseString = response.getSong();
            JsonObject audio = JsonParser.parseString(responseString).getAsJsonObject();

            String audioString = new String(Unprotect.unprotect(audio, this.familyKey, false));
            
            byte[] audioByte = Base64.getDecoder().decode(audioString);
            File audioFile = saveAudioToFile(audioByte);
            
            openMediaPlayer(audioFile);
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public void playMiddleFamilySong(String song, int start){
        try{
            byte[] passwordByte = this.password.getBytes();
            JsonObject encryptedPassword = Protect.protect(passwordByte, this.key, false);
            String encryptedPasswordString = encryptedPassword.toString();

            PlayFamilySongRequest request = PlayFamilySongRequest.newBuilder().setUsername(this.username).setPassword(encryptedPasswordString).setSong(song).build();
            PlayFamilySongResponse response = blockingStub.playFamilySong(request);
            
            String responseString = response.getSong();
            JsonObject audio = JsonParser.parseString(responseString).getAsJsonObject();
        
            String audioString = new String(Unprotect.unprotectBlock(audio, this.familyKey, false, start));
            System.out.println(audioString);
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }


    
    //---------------------------------------------------------------------------------
    //                                    Auxiliar                                     
    //---------------------------------------------------------------------------------

    public void printSongs(JsonObject songs){
        JsonArray songsArray = songs.getAsJsonArray("songs");
        for (int i = 0; i < songsArray.size(); i++) {
            JsonObject song = songsArray.get(i).getAsJsonObject();
            JsonObject mediaInfo = song.get("media").getAsJsonObject().get("mediaInfo").getAsJsonObject();
            String title = mediaInfo.get("title").getAsString();
            String artist = mediaInfo.get("artist").getAsString();
            String format = mediaInfo.get("format").getAsString();
            JsonArray genres = mediaInfo.getAsJsonArray("genre");
            System.out.println("-----------------------------------------------");
            System.out.println("Title: " + title + "\nArtist: " + artist + "\nFormat: " + format);
            System.out.printf("Genres:");
            for (int j = 0; j < genres.size(); j++) {
                System.out.printf(" " + genres.get(j).getAsString());
            }
            System.out.println("\n-----------------------------------------------\n");
        }
    }

    private static File saveAudioToFile(byte[] audioData) {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("temp_audio", ".mp3");
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                fos.write(audioData);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tempFile;
    }

    private static void openMediaPlayer(File audioFile) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                desktop.open(audioFile);
            } else {
                System.out.println("Desktop not supported, cannot open media player.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //---------------------------------------------------------------------------------
    //                                 Demonstration                                     
    //---------------------------------------------------------------------------------

    public void hackme(){
        try {
            HackmeRequest request = HackmeRequest.newBuilder().build();
            HackmeResponse response = blockingStub.hackme(request);

            String responseString = response.getContent();
            JsonObject content = JsonParser.parseString(responseString).getAsJsonObject();
            JsonObject content2 = content.deepCopy();

            JsonParser.parseString(new String(Unprotect.unprotect(content, "keys/global.key", false))).getAsJsonObject();
            JsonParser.parseString(new String(Unprotect.unprotect(content2, "keys/global.key", false))).getAsJsonObject();

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
