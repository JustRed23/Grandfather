package dev.JustRed23.grandfather;

import dev.JustRed23.stonebrick.data.annotation.Directory;
import dev.JustRed23.stonebrick.data.annotation.File;
import dev.JustRed23.stonebrick.data.annotation.FileStructure;

@FileStructure
public class GFS {

    @Directory(path = "stats")
    public static dev.JustRed23.stonebrick.data.Directory stats;

    @File(name = "stats.json", directory = "stats", content = "{}")
    public static dev.JustRed23.stonebrick.data.File statsFile;

    @File(name = "stats.json.tmp", directory = "stats", content = "{}")
    public static dev.JustRed23.stonebrick.data.File statsTemp;

    @File(name = "nodes.json", content = "[]")
    public static dev.JustRed23.stonebrick.data.File nodesFile;
}
