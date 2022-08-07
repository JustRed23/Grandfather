package dev.JustRed23.grandfather;

import dev.JustRed23.stonebrick.data.annotation.Directory;
import dev.JustRed23.stonebrick.data.annotation.File;
import dev.JustRed23.stonebrick.data.annotation.FileStructure;

@FileStructure
public class GFS {

    @Directory(path = "config")
    public static dev.JustRed23.stonebrick.data.Directory configDir;

    @Directory(path = "data")
    public static dev.JustRed23.stonebrick.data.Directory dataDir;

    @Directory(path = "data/logs")
    public static dev.JustRed23.stonebrick.data.Directory logsDir;

    @File(name = "bot.cfg", directory = "config")
    public static dev.JustRed23.stonebrick.data.File configFile;

    @File(name = "data.db", directory = "data")
    public static dev.JustRed23.stonebrick.data.File dbFile;

    @File(name = "log.txt", directory = "logs")
    public static dev.JustRed23.stonebrick.data.File logFile;
}
