DROP DATABASE IF EXISTS SIRS;
CREATE DATABASE IF NOT EXISTS SIRS;
USE SIRS;

CREATE TABLE family (
    family_id INT NOT NULL AUTO_INCREMENT,
    family_name VARCHAR(50) UNIQUE NOT NULL,
    family_code VARCHAR(50) NOT NULL,
    PRIMARY KEY (family_id),
    UNIQUE(family_name)
) ENGINE=InnoDB ENCRYPTED=YES;

CREATE TABLE user (
    username VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(50) NOT NULL,
    email VARCHAR(50) NOT NULL,
    password VARCHAR(50) NOT NULL,
    family_id INT,
    PRIMARY KEY (username),
    FOREIGN KEY (family_id) REFERENCES family(family_id)
) ENGINE=InnoDB ENCRYPTED=YES;

CREATE TABLE music (
	music_id INT NOT NULL AUTO_INCREMENT,
    music_format VARCHAR(50) NOT NULL,
    artist VARCHAR(50) NOT NULL,
    title VARCHAR(50) UNIQUE NOT NULL,
    audiobase64 TEXT(1000000) NOT NULL,
    previewbase64 TEXT(1000000) NOT NULL,
    PRIMARY KEY (music_id)
) ENGINE=InnoDB ENCRYPTED=YES;

CREATE TABLE has_music (
    username VARCHAR(50) NOT NULL,
    music_id INT NOT NULL,
    PRIMARY KEY (username, music_id),
    FOREIGN KEY (username) REFERENCES user(username),
    FOREIGN KEY (music_id) REFERENCES music(music_id)
) ENGINE=InnoDB ENCRYPTED=YES;

CREATE TABLE genre (
	genre_id INT NOT NULL AUTO_INCREMENT,
	genre_name VARCHAR(50) UNIQUE NOT NULL,
    PRIMARY KEY (genre_id),
    UNIQUE(genre_name)
) ENGINE=InnoDB ENCRYPTED=YES;

CREATE TABLE has_genre (
	has_id INT NOT NULL AUTO_INCREMENT,
    music_id INT NOT NULL,
    genre_id INT NOT NULL,
    PRIMARY KEY (has_id),
    FOREIGN KEY (music_id) REFERENCES music(music_id),
    FOREIGN KEY (genre_id) REFERENCES genre(genre_id)
) ENGINE=InnoDB ENCRYPTED=YES;

CREATE TABLE lyrics (
	lyrics_id INT NOT NULL AUTO_INCREMENT,
    music_id INT NOT NULL,
	lyrics VARCHAR(200) NOT NULL,
    PRIMARY KEY (lyrics_id),
    FOREIGN KEY (music_id) REFERENCES music(music_id)
) ENGINE=InnoDB ENCRYPTED=YES;