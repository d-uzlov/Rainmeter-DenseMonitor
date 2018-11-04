package rxtd.flexmon.templates;

import rxtd.rainmeter.resources.Resource;
import rxtd.rainmeter.resources.VirtualResource;

import java.awt.Color;
import java.nio.file.Paths;

public class Parameters {
    final Color textColor = Color.BLACK;
    //    final Color borderColor = new Color(0, 0, 0, 150);
    final Color borderColor = new Color(100, 100, 100, 255);
    final Color backColor = new Color(255, 255, 255, 180);
    final Color plotBackColor = new Color(0, 0, 0, 180);
    final Color outlineColor = new Color(20, 20, 20, 255);

    final int shadowTranslucency = 110;
    final int weakLineTranslucency = 150;
    final int weakShadowTranslucency = 50;

    final Color positiveDeltaColor = Color.GREEN;
    final Color positiveDeltaShadowColor = new Color(positiveDeltaColor.getRGB() & 0x00FFFFFF | shadowTranslucency << 24, true);
    final Color negativeDeltaColor = Color.RED;
    final Color negativeDeltaShadowColor = new Color(negativeDeltaColor.getRGB() & 0x00FFFFFF | shadowTranslucency << 24, true);
    final Color derivativeLineColor = new Color(100, 100, 100, 255);

    final Color gridLineColor = new Color(150, 150, 150, 50);
    final int gridLineWidth = 1;

    final int skinWidth = 230;
    final int skinBorderWidth = 1;
    final int paddingWidth = 0;
    final int elementInterval = 1;
    final int textPadding = 2;
    final int precision = 4;
    final int smallPrecision = 3;
    final int fontSize = 9;
    final int bigFontSize = 21;
    final int hugeFontSize = 30;

    final int barOutlineWidth = 1;
    final int histoOutlineWidth = 1;
    final int histoBarrierWidth = 1;
    final Color histoBarrierColor = Color.WHITE;

    final int averageFactor = 2;
    final int derivativeSmoothingFactor = 3;

    final double defaultFps = 1.0;

    final int supportQueue = 0;
    final int measureQueue = supportQueue + 10;
    final int managerQueue = measureQueue + 10;
    final int backgroundQueue = managerQueue + 10;
    final int gridQueue = backgroundQueue + 10;
    final int meterQueue = gridQueue + 10;
    final int outlineQueue = meterQueue + 100;

    final int shadowInfoBackQueue = outlineQueue + 1;
    final int shadowInfoQueue = shadowInfoBackQueue + 1;

    final int barHeight = 5;
    final int tinyBarHeight = 4;

    final int tinyHistoHeight = 15;
    final int tinyHistoHorizontalLines = 0;
    final int smallHistoHeight = 30;
    final int smallHistoHorizontalLines = 1;
    final int middleHistoHeight = 45;
    final int middleHistoHorizontalLines = 2;
    final int bigHistoHeight = 60;
    final int bigHistoHorizontalLines = 3;

    final int histoVerticalLines = 9;

    final int derivativeHistoHeight = smallHistoHeight;
    final int derivativeHistoHorizontalLines = 0;

    final boolean useUnixProcessorUsage = true;
    final int firstCoreNumber = 0;

    final boolean useTooltips = true;

    /**
     * Calibri
     * Lato
     * Alte DIN 1451 Mittelschrift
     * Montserrat
     * BebasNeue
     */
    final Resource defaultFont = new VirtualResource("Calibri", Paths.get("font"));
    final Resource bigFontFamily = new VirtualResource("Calibri", Paths.get("font"));
    final int defaultFontWeight = 999;

    final Color[] brightPalette = {
            new Color(0xF71735),

            new Color(0x845EC2),
            new Color(0x7FB800),
            new Color(0xD65DB1),
            new Color(0xF9F871),
            new Color(0x8338EC),
            new Color(0xFFBE0B),
            new Color(0x00C6C2),
            new Color(0xFB5607),

            new Color(0x3A86FF),
            new Color(0xFF9671),
            new Color(0xFF006E),
            new Color(0xFF6F91),
            new Color(0x00A6ED),
            new Color(0x6DFACD),
            new Color(0x039590),
            new Color(0x006E5F),
            new Color(0x008A64),
            new Color(0x4BBC8E),
            new Color(0x22C197),
            new Color(0x9BDE7E),
            new Color(0x78C664),
            new Color(0x7FB800),
            new Color(0xBAC853),
            new Color(0xEFAA41),
            new Color(0xFFC75F),
            new Color(0xEF9748),
            new Color(0xFFB400),
            new Color(0xFF9F1C),
            new Color(0xBB972E),
            new Color(0xF6511D),
            new Color(0xA93800),
            new Color(0xB51461),
            new Color(0xAB0B37),
            new Color(0xE85266),

            new Color(0xFDFFFC),
            new Color(0x41EAD4),
            new Color(0x00F4F3),
            new Color(0x00D5FF),
            new Color(0x00B1FF),
            new Color(0x7D85EB),
            new Color(0xAE51AF),
            new Color(0xDC96FF),
            new Color(0xFF99F3),
            new Color(0xFF94A9),
            new Color(0xF6BFE2),

            new Color(0xD3FBD8),
            new Color(0xDFE0DF),
            new Color(0xBAA89B),
            new Color(0xB07566),
            new Color(0xEC87E4),
            new Color(0xEE54C2),

            new Color(0xFFD3FF),
            new Color(0xFFCAE6),
            new Color(0xFFCBB7),
            new Color(0xFFDD87),
            new Color(0xF9F871),
    };

    final Color[] garkPalette = {
            new Color(0x454655),
            new Color(0x5C4041),
            new Color(0x757687),
            new Color(0x595D7D),
            new Color(0x464362),
            new Color(0x00171F),
            new Color(0x0D2C54),
            new Color(0x571600),
            new Color(0x011627),

            new Color(0x595D7D),
            new Color(0x595D7D),
            new Color(0x595D7D),
    };
}
