package io.github.karino2.pngnote.colorpicker;

public class FileMeta {
    public final static String NONE = "None";
    public final static String LINED = "Lined";
    public final static String GRAPH = "Graph";
    public final static String DOTTED = "Dotted";


    public String name;
    public int drawable;

    public FileMeta(String name, int drawable) {
        this.name = name;
        this.drawable = drawable;
    }
}
