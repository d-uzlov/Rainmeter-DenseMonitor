package rxtd.flexmon.templates;

import rxtd.Pair;
import rxtd.rainmeter.Skin;
import rxtd.rainmeter.actions.Action;
import rxtd.rainmeter.actions.ActionChain;
import rxtd.rainmeter.actions.BangUtils;
import rxtd.rainmeter.elements.measures.Calc;
import rxtd.rainmeter.elements.measures.MString;
import rxtd.rainmeter.elements.measures.Measure;
import rxtd.rainmeter.elements.measures.plugins.custom.PerfMonRXTD;
import rxtd.rainmeter.elements.measures.scripts.PlacementManager;
import rxtd.rainmeter.elements.measures.scripts.PlotManager;
import rxtd.rainmeter.elements.meters.Histogram;
import rxtd.rainmeter.elements.meters.Image;
import rxtd.rainmeter.elements.meters.Line;
import rxtd.rainmeter.elements.meters.Meter;
import rxtd.rainmeter.elements.meters.StartPlace;
import rxtd.rainmeter.elements.meters.string.Label;
import rxtd.rainmeter.formulas.Formula;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DisksWidget extends Widget {
    private List<MeasureHolder> measures = new ArrayList<>();
    private List<MeterHolder> meters = new ArrayList<>();
    private List<Point> places = new ArrayList<>();

    public DisksWidget(String name) {
        super(name);
    }

    private void addHeader(String content) {
        IntegerBounds lineBounds = this.layout.nextLine();
        Label nameLabel = new Label("header")
                .setText(content)
                .setStringAlign(Label.Align.CENTER_TOP)
                .setX(lineBounds.x + lineBounds.w / 2)
                .setY(lineBounds.y);
        this.addMeter(nameLabel);
    }

    /**
     * @return bounds of the plot
     */
    protected IntegerBounds addDoublePlot(Measure readCurrent, Measure writeCurrent, IntegerBounds bounds, Color firstColor, Color secondColor, Meter base, int baseX, int baseY, List<String> groups) {
        IntegerBounds histoBounds = bounds.excludeBorder(this.params.histoOutlineWidth, this.params.histoOutlineWidth, this.params.histoOutlineWidth, this.params.histoOutlineWidth);
        this.plotPlaces.add(histoBounds);

        Histogram firstHistogram = new Histogram("readHistogram");
        this.addMeter(firstHistogram);
        firstHistogram.setMeasure(readCurrent)
                .setColor(this.makeShadow(firstColor))
                .setGraphStart(StartPlace.LEFT)
                .setFlip(true);

        Line firstLine = this.addLine(readCurrent, firstColor, histoBounds)
                .setFlip(true);

        Histogram secondHistogram = new Histogram("writeHistogram");
        this.addMeter(secondHistogram);
        secondHistogram.setMeasure(writeCurrent)
                .setColor(this.makeShadow(secondColor))
                .setGraphStart(StartPlace.LEFT);

        Line secondLine = this.addLine(writeCurrent, secondColor, histoBounds);

        if (base != null) {
            int dx = histoBounds.x - baseX;
            int dy = histoBounds.y - baseY;

            firstHistogram
                    .setXRelative(base, dx, false)
                    .setYRelative(base, dy, false)
                    .setW(histoBounds.w)
                    .setH(histoBounds.h);
            firstLine
                    .setXRelative(base, dx, false)
                    .setYRelative(base, dy, false);
            secondHistogram
                    .setXRelative(base, dx, false)
                    .setYRelative(base, dy, false)
                    .setW(histoBounds.w)
                    .setH(histoBounds.h);
            secondLine
                    .setXRelative(base, dx, false)
                    .setYRelative(base, dy, false);
        } else {
            this.setMeterBounds(firstHistogram, histoBounds);
            this.setMeterBounds(secondHistogram, histoBounds);
        }

        if (groups != null) {
            firstHistogram.addGroups(groups);
            secondHistogram.addGroups(groups);
            firstLine.addGroups(groups);
            secondLine.addGroups(groups);
        }

        this.addRectangleOutline(bounds);

        return histoBounds;
    }

    protected void add(boolean correctLoad) {
        Color readColor = Color.GREEN;
        Color writeColor = Color.RED;
        int rightPadding = 46;

        String group = this.createGroup();
        String speedGroup = this.createGroup();

        IntegerBounds nameBounds = this.layout.nextElement(21);
        this.places.add(new Point(nameBounds.x, nameBounds.y));

        Image base = new Image("base");
        this.addMeter(base);
//        base.setX(nameBounds.x).setY(nameBounds.y);

        Label diskNameLabel = new Label("diskName");
        this.addMeter(diskNameLabel);
        diskNameLabel
                .setXRelative(base, this.params.textPadding * 3, false)
                .setYRelative(base, 0, false)
                .setStringAlign(Label.Align.LEFT_TOP)
                .addStyle(this.getLabelStyle())
//                .setText(new Label.TextBuilder().append(diskName))
                .setFontSize((int) Math.round(this.params.fontSize * 1.6))
                .addGroup(group);

        PlotManager totalSpeedMax = new PlotManager("RW_Peak").setLinkedGroup(speedGroup);
        this.addMeasure(totalSpeedMax);

        Calc readCorrected = new Calc("readCorrected")
                .addGroup(speedGroup)
//                .setFormula(new Formula(currentRead))
                .setMaxValue(new Formula(totalSpeedMax))
                .setDynamicVariables(true);
        this.addMeasure(readCorrected);
        Calc writeCorrected = new Calc("writeCorrected")
                .addGroup(speedGroup)
//                .setFormula(new Formula(currentWrite))
                .setMaxValue(new Formula(totalSpeedMax))
                .setDynamicVariables(true);
        this.addMeasure(writeCorrected);

        IntegerBounds firstBounds = this.layout.nextElement(this.params.smallHistoHeight);
        IntegerBounds firstPlotBounds = firstBounds.excludeBorder(0, rightPadding, 0, 0);
        IntegerBounds firstHistoBounds = this.addDoublePlot(readCorrected, writeCorrected, firstPlotBounds, readColor, writeColor, base, nameBounds.x, nameBounds.y, Arrays.asList(group, speedGroup));
        this.addHistoGrid(firstBounds, this.params.smallHistoHorizontalLines, this.params.histoVerticalLines, 0, firstBounds.w - rightPadding);

        totalSpeedMax.setHistWidth(firstHistoBounds.w);

        int firstBoundsXd = firstBounds.x - nameBounds.x;
        int firstBoundsYd = firstBounds.y - nameBounds.y;

        MString helper1 = new MString("totalMaxHelper").addGroup(speedGroup);
        this.addMeasure(helper1);
        Label totalMaxLabel = new Label("totalMax")
                .setText(helper1)
                .setXRelative(base, nameBounds.w - this.params.textPadding, false)
                .setYRelative(base, nameBounds.h + this.params.fontSize / 3, false)
                .setStringAlign(Label.Align.RIGHT_BOTTOM)
                .setStyle(this.getLabelStyle())
                .addGroup(group).addGroup(speedGroup);
        this.addMeter(totalMaxLabel);

        MString helper2 = new MString("currentReadHelper").addGroup(speedGroup);
        this.addMeasure(helper2);
        Label readLabel = new Label("currentRead")
                .setText(helper2)
                .setXRelative(base, firstBoundsXd + firstBounds.w - this.params.textPadding, false)
                .setYRelative(base, firstBoundsYd, false)
                .setStringAlign(Label.Align.RIGHT_TOP)
                .setStyle(this.getLabelStyle())
                .addGroup(group).addGroup(speedGroup);
        this.addMeter(readLabel);

        MString helper3 = new MString("currentWriteHelper").addGroup(speedGroup);
        this.addMeasure(helper3);
        Label writeLabel = new Label("currentWrite")
                .setText(helper3)
                .setXRelative(base, firstBoundsXd + firstBounds.w - this.params.textPadding, false)
                .setYRelative(base, firstBoundsYd + firstBounds.h, false)
                .setStringAlign(Label.Align.RIGHT_BOTTOM)
                .setStyle(this.getLabelStyle())
                .addGroup(group).addGroup(speedGroup);
        this.addMeter(writeLabel);

        totalMaxLabel.setToolTipText("Sum plot peak");
        readLabel.setToolTipText("Read");
        writeLabel.setToolTipText("Write");

        totalMaxLabel.setToolTipHidden(!this.params.useTooltips);
        readLabel.setToolTipHidden(!this.params.useTooltips);
        writeLabel.setToolTipHidden(!this.params.useTooltips);

        Action labelUpdate = new ActionChain()
                .append(BangUtils.setOption(helper2.getName(), "String", this.getFormatBinary().formulaFormatNumber(new Formula(readCorrected, Formula.MeasureParameters.NUMBER_VALUE), this.params.smallPrecision).toString(), null))
                .append(BangUtils.setOption(helper3.getName(), "String", this.getFormatBinary().formulaFormatNumber(new Formula(writeCorrected, Formula.MeasureParameters.NUMBER_VALUE), this.params.smallPrecision).toString(), null))
                .append(BangUtils.setOption(helper1.getName(), "String", this.getFormatBinary().formulaFormatNumber(totalSpeedMax.formulaMax(), this.params.smallPrecision).toString(), null));
        writeCorrected.addInitialAction(labelUpdate);
        writeCorrected.addUpdateAction(labelUpdate);

        String loadGroup = this.createGroup();

        PlotManager loadMax = new PlotManager("loadPeak")
                .setLinkedGroup(loadGroup)
                .setMinimum(0.0);
        this.addMeasure(loadMax);

        Calc loadCorrected = new Calc("loadCorrected")
//                .setFormula(new Formula(currentLoad))
                .setDynamicVariables(true);
        if (correctLoad) {
            loadCorrected.setMaxValue(new Formula(loadMax));
        } else {
            loadCorrected.setMaxValue(100.0);
        }
        this.addMeasure(loadCorrected);

        int secondRightPadding = 55;
        IntegerBounds secondBounds = this.layout.nextElement(this.params.tinyHistoHeight);
        IntegerBounds secondPlotBounds = secondBounds.excludeBorder(0, secondRightPadding, 0, 0);
        {
            IntegerBounds integerBounds = this.addPlot(loadCorrected, secondPlotBounds, Color.YELLOW, base, nameBounds.x, nameBounds.y, Arrays.asList(group, loadGroup));
            this.addHistoGrid(secondBounds, this.params.tinyHistoHorizontalLines, this.params.histoVerticalLines, 0, secondBounds.w - secondRightPadding);

            loadMax.setHistWidth(integerBounds.w);
        }

        int secondBoundsXd = secondBounds.x - nameBounds.x;
        int secondBoundsYd = secondBounds.y - nameBounds.y;

        MString helper4 = new MString("currentLoadHelper").addGroup(loadGroup);
        this.addMeasure(helper4);
        Label currentLoadLabel = new Label("currentLoad");
        this.addMeter(currentLoadLabel);
        currentLoadLabel.setStyle(this.getLabelStyle())
                .setText(helper4)
                .setXRelative(base, secondBoundsXd + secondPlotBounds.w + this.params.textPadding, false)
                .setYRelative(base, secondBoundsYd, false)
                .setStringAlign(Label.Align.LEFT_TOP)
                .addGroup(group).addGroup(loadGroup);

        MString helper5 = new MString("maxLoadHelper").addGroup(loadGroup);
        this.addMeasure(helper5);
        Label maxLoadLabel = new Label("maxLoad");
        this.addMeter(maxLoadLabel);
        maxLoadLabel.setStyle(this.getLabelStyle())
                .setText(helper5)
                .setXRelative(base, secondBoundsXd + secondBounds.w - this.params.textPadding, false)
                .setYRelative(base, secondBoundsYd, false)
                .setStringAlign(Label.Align.RIGHT_TOP)
                .addGroup(group).addGroup(loadGroup);

        if (this.params.useTooltips) {
            currentLoadLabel.setToolTipText("Current");
            maxLoadLabel.setToolTipText("Plot peak");
        }

        labelUpdate = new ActionChain()
                .append(BangUtils.setOption(helper4.getName(), "String", this.getFormatMetric().formulaFormatNumber(new Formula(loadCorrected), this.params.smallPrecision).toString(), null))
                .append(BangUtils.setOption(helper5.getName(), "String", this.getFormatMetric().formulaFormatNumber(loadMax.formulaMax(), this.params.smallPrecision).toString(), null));
        loadCorrected.addInitialAction(labelUpdate);
        loadCorrected.addUpdateAction(labelUpdate);

        this.meters.add(new MeterHolder(base, group, diskNameLabel, totalSpeedMax, readCorrected, writeCorrected, loadMax, loadCorrected));
    }

    public Skin constructLogical(int instancesCount) {
        MeasureProvider provider = new MeasureProvider() {
            @Override
            public String getHeader() {
                return "Logical Disks";
            }

            @Override
            public PerfMonRXTD getParent() {
                PerfMonRXTD parent = new PerfMonRXTD("perfmonPdhParent")
                        .setSyncRawFormatted(false)
                        .setCategory("LogicalDisk").setCounterList("Disk Read Bytes/sec", "Disk Write Bytes/sec", "Avg. Disk Queue Length")
                        .setSortBy(PerfMonRXTD.SortBy.INSTANCE_NAME)
                        .setDisplayName(PerfMonRXTD.DisplayName.LOGICAL_DISK_MOUNT_FOLDER)
                        .setRollup(true)
                        .setWhitelist("*:*");
                DisksWidget.this.skin.add(DisksWidget.this.params.measureQueue, parent);
                return parent;
            }

            @Override
            public Measure getLoad(PerfMonRXTD parent, int i, Measure instanceExists) {
                var currentQueue = parent.createChild("currentQueue")
                        .setType(PerfMonRXTD.ChildType.GET_FORMATTED_COUNTER)
                        .setInstanceIndex(i)
                        .setResultString(PerfMonRXTD.ResultString.ORIGINAL_NAME)
                        .setCounterIndex(2)
                        .setAverageSize(params.averageFactor);
                addMeasure(currentQueue);
                return currentQueue;
            }

            @Override
            public boolean needCorrectLoad() {
                return true;
            }
        };
        return this.construct(provider, instancesCount);
    }

    public Skin constructPhysical(int instancesCount) {
        MeasureProvider provider = new MeasureProvider() {
            @Override
            public String getHeader() {
                return "Physical Disks";
            }

            @Override
            public PerfMonRXTD getParent() {
                PerfMonRXTD parent = new PerfMonRXTD("perfmonPdhParent")
                        .setSyncRawFormatted(false)
                        .setCategory("PhysicalDisk").setCounterList("Disk Read Bytes/sec", "Disk Write Bytes/sec", "% Idle Time")
                        .setSortBy(PerfMonRXTD.SortBy.INSTANCE_NAME)
                        .setWhitelist("*:*");
                DisksWidget.this.skin.add(DisksWidget.this.params.measureQueue, parent);
                return parent;
            }

            @Override
            public Measure getLoad(PerfMonRXTD parent, int i, Measure instanceExists) {
                var currentIdle = parent.createChild("currentIdle")
                        .setType(PerfMonRXTD.ChildType.GET_FORMATTED_COUNTER)
                        .setInstanceIndex(i)
                        .setResultString(PerfMonRXTD.ResultString.ORIGINAL_NAME)
                        .setCounterIndex(2)
                        .setAverageSize(params.averageFactor);
                addMeasure(currentIdle);

                Formula formula = new Formula(100).subtract(new Formula(currentIdle, Formula.MeasureParameters.NUMBER_VALUE))
                        .multiply(new Formula(instanceExists, Formula.MeasureParameters.NUMBER_VALUE).notEqual(new Formula(0)));
                Calc busyTime = new Calc("currentNotIdle").setFormula(formula).setMaxValue(100.0);
                addMeasure(busyTime);
                return busyTime;
            }

            @Override
            public boolean needCorrectLoad() {
                return false;
            }
        };
        return this.construct(provider, instancesCount);
    }

    private Skin construct(MeasureProvider provider, int instancesCount) {
        this.skin.getRainmeterSection().setFPS(1.0);

        this.addHeader(provider.getHeader());

        var parent = provider.getParent();

        String backgroundGroup = this.createGroup();

        PerfMonRXTD.Child instanceCount = parent.createChild("instancesCount").setType(PerfMonRXTD.ChildType.GET_COUNT).setTotal(true);
        Action action = new ActionChain()
                .append(BangUtils.setOptionGroup(backgroundGroup, "notExistingOption", "1", null))
                .append(BangUtils.updateMeterGroup(backgroundGroup, null));
        instanceCount.addChangeAction(action)
                .addInitialAction(action);
        this.addMeasure(instanceCount);

        this.backgroundStart();

        List<Meter> backgroundParts = new ArrayList<>();
        List<IntegerBounds> backgroundHeight = new ArrayList<>();

        for (int i = 0; i < instancesCount; i++) {
            var currentRead = parent.createChild("currentRead")
                    .setType(PerfMonRXTD.ChildType.GET_FORMATTED_COUNTER)
                    .setInstanceIndex(i)
                    .setResultString(PerfMonRXTD.ResultString.ORIGINAL_NAME)
                    .setCounterIndex(0)
                    .setAverageSize(this.params.averageFactor);
            this.skin.add(this.params.measureQueue, currentRead);
            var currentWrite = parent.createChild("currentWrite")
                    .setType(PerfMonRXTD.ChildType.GET_FORMATTED_COUNTER)
                    .setInstanceIndex(i)
                    .setResultString(PerfMonRXTD.ResultString.ORIGINAL_NAME)
                    .setCounterIndex(1)
                    .setAverageSize(this.params.averageFactor);
            this.skin.add(this.params.measureQueue, currentWrite);
            var instanceExists = parent.createChild("instanceExists")
                    .setType(PerfMonRXTD.ChildType.GET_COUNT)
                    .setInstanceIndex(i)
                    .setResultString(PerfMonRXTD.ResultString.ORIGINAL_NAME);
            this.skin.add(this.params.measureQueue, instanceExists);

            var currentLoad = provider.getLoad(parent, i, instanceExists);

            this.add(provider.needCorrectLoad());
            this.measures.add(new MeasureHolder(instanceExists, currentRead, currentWrite, currentLoad));

            Meter style = new Image("style" + i)
                    .addGroup(backgroundGroup);
            this.addMeter(style);
            backgroundParts.add(style);

            Formula notHidden = new Formula(instanceCount, Formula.MeasureParameters.NUMBER_VALUE).moreOrEqual(new Formula(i + 1));

            var meterIntegerPair = this.backgroundPart();
            backgroundHeight.add(meterIntegerPair.value);
            Meter background = meterIntegerPair.key;
            background.addStyle(style)
                    .setY(new Formula(meterIntegerPair.value.y).multiply(notHidden).toString());
            this.outlinePart(meterIntegerPair.value)
                    .addStyle(style)
                    .addGroup(backgroundGroup)
                    .setY(new Formula(meterIntegerPair.value.y).multiply(notHidden).toString());

            List<Meter> grids = this.gridPart(new Pair<>(meterIntegerPair.value, notHidden));
            for (var g : grids) {
                g.addStyle(style);
            }
        }

//        Pair<Meter, IntegerBounds> backgroundEnd = this.backgroundEnd();

        for (int i = 0; i < backgroundParts.size(); i++) {
            Meter b = backgroundParts.get(i);
            b.setHidden(new Formula(instanceCount, Formula.MeasureParameters.NUMBER_VALUE).lessThan(new Formula(i + 1)).toString());
        }
//        backgroundEnd.key.setY()

        PlacementManager placementManager = new PlacementManager();
        this.skin.add(this.params.managerQueue, placementManager);

        List<PlacementManager.ManagedGroup> mg = new ArrayList<>();
        for (var m : this.meters) {
            PlacementManager.ManagedGroup managedGroup = new PlacementManager.ManagedGroup(m.base, m.group);
            managedGroup.meterOptions.add(new Pair<>(m.name, "MeasureName"));
            managedGroup.meterOptions.add(new Pair<>(m.totalMax, "CurValue"));
            managedGroup.meterOptions.add(new Pair<>(m.readCorrected, "Formula"));
            managedGroup.meterOptions.add(new Pair<>(m.writeCorrected, "Formula"));
            managedGroup.meterOptions.add(new Pair<>(m.loadMax, "CurValue"));
            managedGroup.meterOptions.add(new Pair<>(m.loadCorrected, "Formula"));

            mg.add(managedGroup);
        }
        placementManager.setMeters(mg);

        List<PlacementManager.MeasureGroup> msg = new ArrayList<>();
        for (var m : this.measures) {
            PlacementManager.MeasureGroup measureGroup = new PlacementManager.MeasureGroup(m.name);
            measureGroup.values.add(m.name.getName());
            measureGroup.values.add(new Formula(m.read, Formula.MeasureParameters.NUMBER_VALUE).add(new Formula(m.write, Formula.MeasureParameters.NUMBER_VALUE)).toEscapedString());
            measureGroup.values.add(new Formula(m.read, Formula.MeasureParameters.NUMBER_VALUE).toCalcString());
            measureGroup.values.add(new Formula(m.write, Formula.MeasureParameters.NUMBER_VALUE).toCalcString());
            measureGroup.values.add(new Formula(m.load, Formula.MeasureParameters.NUMBER_VALUE).toEscapedString());
            measureGroup.values.add(new Formula(m.load, Formula.MeasureParameters.NUMBER_VALUE).toCalcString());

            msg.add(measureGroup);

            m.name.addChangeAction(placementManager.bangReset());
        }
        placementManager.setMeasures(msg);

        List<Pair<Integer, Integer>> places = new ArrayList<>();
        for (var p : this.places) {
            places.add(new Pair<>(p.x, p.y));
        }
        placementManager.setPositions(places);

        return skin;
    }

    private interface MeasureProvider {
        String getHeader();

        PerfMonRXTD getParent();

        Measure getLoad(PerfMonRXTD parent, int i, Measure instanceExists);

        boolean needCorrectLoad();
    }

    private static class MeasureHolder {
        private final Measure name;
        private final Measure read;
        private final Measure write;
        private final Measure load;

        public MeasureHolder(Measure name, Measure read, Measure write, Measure load) {
            this.name = name;
            this.read = read;
            this.write = write;
            this.load = load;
        }
    }

    private static class MeterHolder {
        private final Meter base;
        private final String group;
        private final Label name;
        private final PlotManager totalMax;
        private final Calc readCorrected;
        private final Calc writeCorrected;
        private final PlotManager loadMax;
        private final Calc loadCorrected;

        private MeterHolder(Meter base, String group, Label name, PlotManager totalMax, Calc readCorrected, Calc writeCorrected, PlotManager loadMax, Calc loadCorrected) {
            this.base = base;
            this.group = group;
            this.name = name;
            this.totalMax = totalMax;
            this.readCorrected = readCorrected;
            this.writeCorrected = writeCorrected;
            this.loadMax = loadMax;
            this.loadCorrected = loadCorrected;
        }
    }
}
