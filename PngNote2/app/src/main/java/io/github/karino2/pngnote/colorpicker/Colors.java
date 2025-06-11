package io.github.karino2.pngnote.colorpicker;

import java.util.ArrayList;
import java.util.List;

public class Colors {
    public static List<MaterialColor> colorList = new ArrayList<MaterialColor>();
    static {
        //red
        colorList.add(new MaterialColor(
                0xFFF44336,
                new int[] {50, 100, 200, 300, 400, 500, 600, 700, 800, 900},
                new int[] {
                    (0xFFFFEBEE),
                    (0xFFFFCDD2),
                    (0xFFEF9A9A),
                    (0xFFE57373),
                    (0xFFEF5350),
                    (0xFFF44336),
                    (0xFFE53935),
                    (0xFFD32F2F),
                    (0xFFC62828),
                    (0xFFB71C1C),
                }
                ));
        //pink
        colorList.add(new MaterialColor(
                0xFFE91E63,
                new int[] {50, 100, 200, 300, 400, 500, 600, 700, 800, 900},
                new int[] {
                    (0xFFFCE4EC),
                    (0xFFF8BBD0),
                    (0xFFF48FB1),
                    (0xFFF06292),
                    (0xFFEC407A),
                    (0xFFE91E63),
                    (0xFFD81B60),
                    (0xFFC2185B),
                    (0xFFAD1457),
                    (0xFF880E4F),
                }
        ));
        //purple
        colorList.add(new MaterialColor(
                0xFF9C27B0,
                new int[] {50, 100, 200, 300, 400, 500, 600, 700, 800, 900},
                new int[] {
                    (0xFFF3E5F5),
                    (0xFFE1BEE7),
                    (0xFFCE93D8),
                    (0xFFBA68C8),
                    (0xFFAB47BC),
                    (0xFF9C27B0),
                    (0xFF8E24AA),
                    (0xFF7B1FA2),
                    (0xFF6A1B9A),
                    (0xFF4A148C),
                }
        ));
        //deepPurple
        colorList.add(new MaterialColor(
                0xFF673AB7,
                new int[] {50, 100, 200, 300, 400, 500, 600, 700, 800, 900},
                new int[] {
                    (0xFFEDE7F6),
                    (0xFFD1C4E9),
                    (0xFFB39DDB),
                    (0xFF9575CD),
                    (0xFF7E57C2),
                    (0xFF673AB7),
                    (0xFF5E35B1),
                    (0xFF512DA8),
                    (0xFF4527A0),
                    (0xFF311B92),
                }
        ));
        //indigo
        colorList.add(new MaterialColor(
                0xFF3F51B5,
                new int[] {50, 100, 200, 300, 400, 500, 600, 700, 800, 900},
                new int[] {
                    (0xFFE8EAF6),
                    (0xFFC5CAE9),
                    (0xFF9FA8DA),
                    (0xFF7986CB),
                    (0xFF5C6BC0),
                    (0xFF3F51B5),
                    (0xFF3949AB),
                    (0xFF303F9F),
                    (0xFF283593),
                    (0xFF1A237E),
                }
        ));
        //blue
        colorList.add(new MaterialColor(
                0xFF2196F3,
                new int[] {50, 100, 200, 300, 400, 500, 600, 700, 800, 900},
                new int[] {
                    (0xFFE3F2FD),
                    (0xFFBBDEFB),
                    (0xFF90CAF9),
                    (0xFF64B5F6),
                    (0xFF42A5F5),
                    (0xFF2196F3),
                    (0xFF1E88E5),
                    (0xFF1976D2),
                    (0xFF1565C0),
                    (0xFF0D47A1),
                }
        ));
        //lightBlue
        colorList.add(new MaterialColor(
                0xFF03A9F4,
                new int[] {50, 100, 200, 300, 400, 500, 600, 700, 800, 900},
                new int[] {
                    (0xFFE1F5FE),
                    (0xFFB3E5FC),
                    (0xFF81D4FA),
                    (0xFF4FC3F7),
                    (0xFF29B6F6),
                    (0xFF03A9F4),
                    (0xFF039BE5),
                    (0xFF0288D1),
                    (0xFF0277BD),
                    (0xFF01579B),
                }
        ));
        //cyan
        colorList.add(new MaterialColor(
                0xFF00BCD4,
                new int[] {50, 100, 200, 300, 400, 500, 600, 700, 800, 900},
                new int[] {
                    (0xFFE0F7FA),
                    (0xFFB2EBF2),
                    (0xFF80DEEA),
                    (0xFF4DD0E1),
                    (0xFF26C6DA),
                    (0xFF00BCD4),
                    (0xFF00ACC1),
                    (0xFF0097A7),
                    (0xFF00838F),
                    (0xFF006064),
                }
        ));
        //teal
        colorList.add(new MaterialColor(
                0xFF009688,
                new int[] {50, 100, 200, 300, 400, 500, 600, 700, 800, 900},
                new int[] {
                    (0xFFE0F2F1),
                    (0xFFB2DFDB),
                    (0xFF80CBC4),
                    (0xFF4DB6AC),
                    (0xFF26A69A),
                    (0xFF009688),
                    (0xFF00897B),
                    (0xFF00796B),
                    (0xFF00695C),
                    (0xFF004D40),
                }
        ));
        //green
        colorList.add(new MaterialColor(
                0xFF4CAF50,
                new int[] {50, 100, 200, 300, 400, 500, 600, 700, 800, 900},
                new int[] {
                    (0xFFE8F5E9),
                    (0xFFC8E6C9),
                    (0xFFA5D6A7),
                    (0xFF81C784),
                    (0xFF66BB6A),
                    (0xFF4CAF50),
                    (0xFF43A047),
                    (0xFF388E3C),
                    (0xFF2E7D32),
                    (0xFF1B5E20),
                }
        ));
        //lightGreen
        colorList.add(new MaterialColor(
                0xFF8BC34A,
                new int[] {50, 100, 200, 300, 400, 500, 600, 700, 800, 900},
                new int[] {
                    (0xFFF1F8E9),
                    (0xFFDCEDC8),
                    (0xFFC5E1A5),
                    (0xFFAED581),
                    (0xFF9CCC65),
                    (0xFF8BC34A),
                    (0xFF7CB342),
                    (0xFF689F38),
                    (0xFF558B2F),
                    (0xFF33691E),
                }
        ));
        //lime
        colorList.add(new MaterialColor(
                0xFFCDDC39,
                new int[] {50, 100, 200, 300, 400, 500, 600, 700, 800, 900},
                new int[] {
                    (0xFFF9FBE7),
                    (0xFFF0F4C3),
                    (0xFFE6EE9C),
                    (0xFFDCE775),
                    (0xFFD4E157),
                    (0xFFCDDC39),
                    (0xFFC0CA33),
                    (0xFFAFB42B),
                    (0xFF9E9D24),
                    (0xFF827717),
                }
        ));
        //yellow
        colorList.add(new MaterialColor(
                0xFFFFEB3B,
                new int[] {50, 100, 200, 300, 400, 500, 600, 700, 800, 900},
                new int[] {
                    (0xFFFFFDE7),
                    (0xFFFFF9C4),
                    (0xFFFFF59D),
                    (0xFFFFF176),
                    (0xFFFFEE58),
                    (0xFFFFEB3B),
                    (0xFFFDD835),
                    (0xFFFBC02D),
                    (0xFFF9A825),
                    (0xFFF57F17),
                }
        ));
        //amber
        colorList.add(new MaterialColor(
                0xFFFFC107,
                new int[] {50, 100, 200, 300, 400, 500, 600, 700, 800, 900},
                new int[] {
                    (0xFFFFF8E1),
                    (0xFFFFECB3),
                    (0xFFFFE082),
                    (0xFFFFD54F),
                    (0xFFFFCA28),
                    (0xFFFFC107),
                    (0xFFFFB300),
                    (0xFFFFA000),
                    (0xFFFF8F00),
                    (0xFFFF6F00),
                }
        ));
        //orange
        colorList.add(new MaterialColor(
                0xFFFF9800,
                new int[] {50, 100, 200, 300, 400, 500, 600, 700, 800, 900},
                new int[] {
                    (0xFFFFF3E0),
                    (0xFFFFE0B2),
                    (0xFFFFCC80),
                    (0xFFFFB74D),
                    (0xFFFFA726),
                    (0xFFFF9800),
                    (0xFFFB8C00),
                    (0xFFF57C00),
                    (0xFFEF6C00),
                    (0xFFE65100),
                }
        ));
        //deepOrange
        colorList.add(new MaterialColor(
                0xFFFF5722,
                new int[] {50, 100, 200, 300, 400, 500, 600, 700, 800, 900},
                new int[] {
                    (0xFFFBE9E7),
                    (0xFFFFCCBC),
                    (0xFFFFAB91),
                    (0xFFFF8A65),
                    (0xFFFF7043),
                    (0xFFFF5722),
                    (0xFFF4511E),
                    (0xFFE64A19),
                    (0xFFD84315),
                    (0xFFBF360C),
                }
        ));
        //brown
        colorList.add(new MaterialColor(
                0xFF795548,
                new int[] {50, 100, 200, 300, 400, 500, 600, 700, 800, 900},
                new int[] {
                    (0xFFEFEBE9),
                    (0xFFD7CCC8),
                    (0xFFBCAAA4),
                    (0xFFA1887F),
                    (0xFF8D6E63),
                    (0xFF795548),
                    (0xFF6D4C41),
                    (0xFF5D4037),
                    (0xFF4E342E),
                    (0xFF3E2723),
                }
        ));
        //grey
        colorList.add(new MaterialColor(
                0xFF9E9E9E,
                new int[] {0, 50, 100, 200, 300, 350, 400, 500, 600, 700, 800, 850, 900, 1000},
                new int[] {
                    (0xFFFFFFFF), //new

                    (0xFFFAFAFA),
                    (0xFFF5F5F5),
                    (0xFFEEEEEE),
                    (0xFFE0E0E0),
                    (0xFFD6D6D6), // only for raised button while pressed in light theme
                    (0xFFBDBDBD),
                    (0xFF9E9E9E),
                    (0xFF757575),
                    (0xFF616161),
                    (0xFF424242),
                    (0xFF303030), // only for background color in dark theme
                    (0xFF212121),

                    (0xFF000000), //new
                }
        ));
        //blueGrey
        colorList.add(new MaterialColor(
                0xFF607D8B,
                new int[] {50, 100, 200, 300, 400, 500, 600, 700, 800, 900},
                new int[] {
                    (0xFFECEFF1),
                    (0xFFCFD8DC),
                    (0xFFB0BEC5),
                    (0xFF90A4AE),
                    (0xFF78909C),
                    (0xFF607D8B),
                    (0xFF546E7A),
                    (0xFF455A64),
                    (0xFF37474F),
                    (0xFF263238),
                }
        ));
    }
}
