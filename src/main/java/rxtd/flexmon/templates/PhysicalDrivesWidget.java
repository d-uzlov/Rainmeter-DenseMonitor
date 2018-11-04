package rxtd.flexmon.templates;

import rxtd.rainmeter.Skin;
import rxtd.rainmeter.elements.measures.plugins.custom.PerfMonRXTD;

import java.awt.Color;

public class PhysicalDrivesWidget extends Widget {
    private final Color readLineColor = new Color(0, 251, 128);
    private final Color readShadowColor = new Color(readLineColor.getRGB() & 0xFFFFFF | params.shadowTranslucency << 24, true);

    private final Color writeLineColor = new Color(205, 0, 0);
    private final Color writeShadowColor = new Color(writeLineColor.getRGB() & 0xFFFFFF | params.shadowTranslucency << 24, true);

    public PhysicalDrivesWidget() {
        super("PhysicalDrives");
    }

    public Skin construct() {
        PerfMonRXTD parent = new PerfMonRXTD("perfmonPdhParent")
                .setSyncRawFormatted(false)
                .setCategory("PhysicalDisk")
                .setCounterList("Disk Read Bytes/sec", "Disk Write Bytes/sec")
                .setWhitelist("_Total");
        this.addMeasure(parent);


        PerfMonRXTD.Child currentRead = parent.createChild("currentRead")
                .setType(PerfMonRXTD.ChildType.GET_FORMATTED_COUNTER).setCounterIndex(0).setInstanceIndex(0).setAverageSize(this.params.averageFactor);
        this.addMeasure(currentRead);
        PerfMonRXTD.Child totalRead = parent.createChild("totalRead")
                .setType(PerfMonRXTD.ChildType.GET_RAW_COUNTER).setCounterIndex(0).setInstanceIndex(0).setAverageSize(this.params.averageFactor);
        this.addMeasure(totalRead);

        this.addDynamicSpeedStatisticsModule(currentRead, totalRead, "Read", this.readLineColor, this.readShadowColor);

        PerfMonRXTD.Child currentWrite = parent.createChild("currentWrite")
                .setType(PerfMonRXTD.ChildType.GET_FORMATTED_COUNTER).setCounterIndex(1).setInstanceIndex(0).setAverageSize(this.params.averageFactor);
        this.addMeasure(currentWrite);
        PerfMonRXTD.Child totalWrite = parent.createChild("totalWrite")
                .setType(PerfMonRXTD.ChildType.GET_RAW_COUNTER).setCounterIndex(1).setInstanceIndex(0).setAverageSize(this.params.averageFactor);
        this.addMeasure(totalWrite);

        this.addDynamicSpeedStatisticsModule(currentWrite, totalWrite, "Write", this.writeLineColor, this.writeShadowColor);

        this.backgroundSolid();
        this.outlineSolid();
        this.gridSolid();

        return skin;
    }
}
