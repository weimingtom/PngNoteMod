package io.github.karino2.pngnote.colorpicker;

import java.util.HashMap;
import java.util.Map;

public class MaterialColor {
    public int primaryValue;
    public int[] keys;
    public int[] colors;

    public MaterialColor(int primaryValue, int[] keys, int[] colors) {
        this.primaryValue = primaryValue;
        this.keys = keys;
        this.colors = colors;
    }
}
