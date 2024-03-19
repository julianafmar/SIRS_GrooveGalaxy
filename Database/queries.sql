-- Retrieve all songs

SELECT * FROM music;

-- Retrieve all genres

SELECT * FROM genre;

-- Retrieve all songs by genre with genre_name

SELECT * FROM music WHERE music_id IN (SELECT music_id FROM has_genre WHERE genre_id=(SELECT genre_id FROM genre WHERE genre_name = 'Rock'));

-- Retrieve all genres by song  

SELECT * FROM genre WHERE genre_id IN (SELECT genre_id FROM has_genre WHERE music_id = 1);

-- Retrieve lyrics for a song

SELECT * FROM lyrics WHERE music_id = 1;