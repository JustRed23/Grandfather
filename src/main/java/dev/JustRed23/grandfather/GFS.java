package dev.JustRed23.grandfather;

import dev.JustRed23.stonebrick.data.annotation.Directory;
import dev.JustRed23.stonebrick.data.annotation.FileStructure;

@FileStructure
public class GFS {

    @Directory(path = "stats")
    public static dev.JustRed23.stonebrick.data.Directory stats;
}
