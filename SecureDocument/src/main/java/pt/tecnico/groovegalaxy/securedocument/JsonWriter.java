package pt.tecnico.groovegalaxy.securedocument;

import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.*;

/**
 * Example of JSON writer.
 */
public class JsonWriter {
    public static void main(String[] args) throws IOException {
        // Check arguments
        if (args.length < 10) {
            System.err.println("Argument(s) missing!");
            System.err.printf("Usage: java %s file%n", JsonWriter.class.getName());
            return;
        }
        final String filename = args[0];
        final String name = args[1];
        final String format = args[2];
        final String artist = args[3];
        final String title = args[4]; // pode ter espacos
        final int number_of_genres = Integer.parseInt(args[5]); // pode ter espacos
        final int number_of_lyrics = Integer.parseInt(args[6 + number_of_genres]); // pode ter espaços
        final String audioBase64 = args[7 + number_of_genres + number_of_lyrics];

        // Create bank statement JSON object
        JsonObject jsonObject = new JsonObject();
        JsonObject mediaObject = new JsonObject();
        JsonObject mediaInfoObject = new JsonObject();
        mediaInfoObject.addProperty("owner", name);
        mediaInfoObject.addProperty("format", format);
        mediaInfoObject.addProperty("artist", artist);
        mediaInfoObject.addProperty("title", title);
        JsonArray genreArray = new JsonArray();
        for (int i = 0; i < number_of_genres; i++) {
            genreArray.add(args[6 + i]);
        }
        mediaInfoObject.add("genre", genreArray);
        mediaObject.add("mediaInfo", mediaInfoObject);
        
        JsonObject mediaContentObject = new JsonObject();
        JsonArray lyricsArray = new JsonArray();
        for (int i = 0; i < number_of_lyrics; i++) {
            lyricsArray.add(args[7 + number_of_genres + i]);
        }
        mediaContentObject.add("lyrics", lyricsArray);
        mediaContentObject.addProperty("audioBase64", audioBase64);
        mediaObject.add("mediaContent", mediaContentObject);
        jsonObject.add("media", mediaObject);

        // Write JSON object to file
        try (FileWriter fileWriter = new FileWriter(filename)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(jsonObject, fileWriter);
        }
    }
}
