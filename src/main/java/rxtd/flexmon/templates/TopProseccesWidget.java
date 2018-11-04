package rxtd.flexmon.templates;

import rxtd.rainmeter.Skin;
import rxtd.rainmeter.SkinUtils;
import rxtd.rainmeter.actions.Action;
import rxtd.rainmeter.actions.ActionChain;
import rxtd.rainmeter.actions.BangUtils;
import rxtd.rainmeter.elements.measures.Calc;
import rxtd.rainmeter.elements.measures.MString;
import rxtd.rainmeter.elements.measures.Measure;
import rxtd.rainmeter.elements.measures.Uptime;
import rxtd.rainmeter.elements.measures.plugins.ActionTimer;
import rxtd.rainmeter.elements.measures.plugins.custom.PerfMonRXTD;
import rxtd.rainmeter.elements.meters.Image;
import rxtd.rainmeter.elements.meters.MeterBase;
import rxtd.rainmeter.elements.meters.shape.Shape;
import rxtd.rainmeter.elements.meters.shape.shapetypes.ShapeLine;
import rxtd.rainmeter.elements.meters.shape.shapetypes.ShapeRectangle;
import rxtd.rainmeter.elements.meters.shape.shapetypes.modifiers.attribute.Fill;
import rxtd.rainmeter.elements.meters.shape.shapetypes.modifiers.attribute.Stroke;
import rxtd.rainmeter.elements.meters.shape.shapetypes.modifiers.attribute.StrokeWidth;
import rxtd.rainmeter.elements.meters.string.Label;
import rxtd.rainmeter.formulas.Formula;
import rxtd.rainmeter.resources.Resource;
import rxtd.rainmeter.resources.ResourceFactory;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;

public class TopProseccesWidget extends Widget {
    private double baseOffsetMultiplier = 5;
    private int nameOffset = 6;
    private String updateGroup = this.createGroup();

    public TopProseccesWidget() {
        super("Top Processes");
    }

    private IntegerBounds addHeader(String string) {
        IntegerBounds lineBounds = this.layout.nextLine();
        Label nameLabel = new Label("header")
                .setText(string)
                .setStringAlign(Label.Align.CENTER_TOP)
                .setX(lineBounds.x + lineBounds.w / 2)
                .setY(lineBounds.y);
        this.skin.add(this.params.meterQueue, nameLabel);

        return lineBounds;
    }

    private void addCpuLine(Measure name, Measure current, Measure cpuTime, int index) {
        IntegerBounds lineBounds = this.layout.nextLine();

        Label valueLabel = new Label("currentCpuLoadLabel" + index)
                .setText(new Formula(current, 2, null, false, null).toString())
                .setX((int) Math.round(lineBounds.x + this.params.fontSize * this.baseOffsetMultiplier))
                .setY(lineBounds.y)
                .setStringAlign(Label.Align.RIGHT_TOP)
                .setDynamicVariables(true)
                .setStyle(this.getLabelStyle());
        this.addMeter(valueLabel);

        Label nameLabel = new Label("nameLabel" + index)
                .setX((int) Math.round(lineBounds.x + this.params.fontSize * this.baseOffsetMultiplier + this.nameOffset))
                .setY(lineBounds.y)
                .setW((int) Math.round(lineBounds.w - (this.params.fontSize * (this.baseOffsetMultiplier * 2.5) + this.nameOffset)))
                .setStringAlign(Label.Align.LEFT_TOP)
                .setStyle(this.getLabelStyle())
                .setText(name)
                .setClipString(Label.ClipString.ENABLED);
        this.addMeter(nameLabel);

        Uptime cpuTimeFormatted = new Uptime("cputTimeFormatHelper" + index)
                .setAddDaysToHours(false)
//                .setAddDaysToHours(true)
                .setSecondsValue(new Formula(cpuTime, Formula.MeasureParameters.NUMBER_VALUE))
                .setDynamicVariables(true)
                .setFormat(new Uptime.FormatBuilder()
                        .append(Uptime.FormatCode.DAYS, 0)
                        .append(":").append(Uptime.FormatCode.HOURS, 2)
                        .append(":").append(Uptime.FormatCode.MINUTES, 2)
                        .append(":").append(Uptime.FormatCode.SECONDS, 2))
                .setRegExpSubstitute(true)
                .addSubstitute("^0:00:", "")
                .addSubstitute("^0:", "");
        this.addMeasure(cpuTimeFormatted);


        Label cpuTimeLabel = new Label("cpuTimeLabel" + index)
                .setText(cpuTimeFormatted)
                .setX((int) Math.round(lineBounds.x + lineBounds.w - this.params.textPadding)).setY(lineBounds.y)
                .setStringAlign(Label.Align.RIGHT_TOP)
                .setStyle(this.getLabelStyle());
        this.skin.add(this.params.meterQueue, cpuTimeLabel);
    }

    private void addMemoryLine(Measure name, Measure ramApprox, Measure workingSet, int index) {
        IntegerBounds lineBounds = this.layout.nextLine();

        Calc calc = new Calc("format_helper")
                .addGroup(updateGroup);
        this.addMeasure(calc);

        MString helper1 = new MString("approxLabelHelper")
                .addGroup(updateGroup);
        MString helper2 = new MString("workingSetLabelHelper")
                .addGroup(updateGroup);

        Action formatApprox = BangUtils.setOption(helper1.getName(), "String", this.getFormatBinaryRaw().formulaFormatNumber(new Formula(ramApprox, Formula.MeasureParameters.NUMBER_VALUE), this.params.precision).toString(), null);
        Action formatWorkingSet = BangUtils.setOption(helper2.getName(), "String", this.getFormatBinaryRaw().formulaFormatNumber(new Formula(workingSet, Formula.MeasureParameters.NUMBER_VALUE), this.params.precision).toString(), null);

        calc.addInitialAction(formatApprox).addUpdateAction(formatApprox);
        calc.addInitialAction(formatWorkingSet).addUpdateAction(formatWorkingSet);

        this.addMeasure(helper1);
        Label approxLabel = new Label("ramLabel")
                .setText(helper1)
                .setX((int) Math.round(lineBounds.x + this.params.fontSize * this.baseOffsetMultiplier))
                .setY(lineBounds.y)
                .setStringAlign(Label.Align.RIGHT_TOP)
                .setDynamicVariables(true)
                .setStyle(this.getLabelStyle());
        this.addMeter(approxLabel);

        Label nameLabel = new Label("nameLabel" + index)
                .setX((int) Math.round(lineBounds.x + this.params.fontSize * this.baseOffsetMultiplier + this.nameOffset))
                .setY(lineBounds.y)
                .setW((int) Math.round(lineBounds.w - (this.params.fontSize * (this.baseOffsetMultiplier * 2) + this.nameOffset)))
                .setStringAlign(Label.Align.LEFT_TOP)
                .setStyle(this.getLabelStyle())
                .setText(name)
                .setClipString(Label.ClipString.ENABLED);
        this.addMeter(nameLabel);

        this.addMeasure(helper2);
        Label privateWorkingSetLabel = new Label("commitLabel")
                .setText(helper2)
                .setX((int) Math.round(lineBounds.x + lineBounds.w - this.params.textPadding))
                .setY(lineBounds.y)
                .setStringAlign(Label.Align.RIGHT_TOP)
                .setStyle(this.getLabelStyle());
        this.addMeter(privateWorkingSetLabel);
    }

    private void addIOLine(Measure name, Measure current, Measure total, int index) {
        IntegerBounds lineBounds = this.layout.nextLine();

        MString helper1 = new MString("valueLabelHelper")
                .addGroup(updateGroup);
        MString helper3 = new MString("totalHelper")
                .addGroup(updateGroup);

        Calc calc = new Calc("format_helper")
                .addGroup(updateGroup);
        this.addMeasure(calc);

        Action formatCurrent = BangUtils.setOption(helper1.getName(), "String", this.getFormatBinaryRaw().formulaFormatNumber(new Formula(current, Formula.MeasureParameters.NUMBER_VALUE), this.params.precision).toString(), null);
        Action formatTotal = BangUtils.setOption(helper3.getName(), "String", this.getFormatBinaryRaw().formulaFormatNumber(new Formula(total, Formula.MeasureParameters.NUMBER_VALUE), this.params.precision).toString(), null);
        calc.addInitialAction(formatCurrent).addUpdateAction(formatCurrent);
        calc.addInitialAction(formatTotal).addUpdateAction(formatTotal);

        this.addMeasure(helper1);
        Label valueLabel = new Label("currentLabel" + index)
                .setX((int) Math.round(lineBounds.x + this.params.fontSize * this.baseOffsetMultiplier))
                .setY(lineBounds.y)
                .setStringAlign(Label.Align.RIGHT_TOP)
                .setStyle(this.getLabelStyle())
                .setText(helper1);
        this.addMeter(valueLabel);

        Label nameLabel = new Label("nameLabel" + index)
                .setX((int) Math.round(lineBounds.x + this.params.fontSize * this.baseOffsetMultiplier + this.nameOffset))
                .setY(lineBounds.y)
                .setW((int) Math.round(lineBounds.w - (this.params.fontSize * (this.baseOffsetMultiplier * 2) + this.nameOffset)))
                .setStringAlign(Label.Align.LEFT_TOP)
                .setStyle(this.getLabelStyle())
                .setText(name)
                .setClipString(Label.ClipString.ENABLED);
        this.addMeter(nameLabel);

        this.addMeasure(helper3);
        Label totalLabel = new Label("totalLabel" + index)
                .setX((int) Math.round(lineBounds.x + lineBounds.w - this.params.textPadding))
                .setY(lineBounds.y)
                .setStringAlign(Label.Align.RIGHT_TOP)
                .setStyle(this.getLabelStyle())
                .setText(helper3);
        this.addMeter(totalLabel);
    }

    private void addVerticalLine(int startY, int endY, int x, Color color) {
        ShapeLine line = new ShapeLine(x + 0.5, startY + 0.5, x + 0.5, endY + 0.5);
        line.addModifier(new Stroke(color)).addModifier(new StrokeWidth(1));
        this.mask.add(line);
    }

    private int addBreaker(Color color, int height) {
        IntegerBounds bounds = this.layout.nextElement(height);
        ShapeLine line = new ShapeLine(bounds.x + 0.5, bounds.y + 0.5, bounds.x + bounds.w + 0.5, bounds.y + 0.5);
        line.addModifier(new StrokeWidth(height)).addModifier(new Stroke(color));
        this.mask.add(line);
        return bounds.y;
    }

    private void addSwitches(PerfMonRXTD parent, int offsetStep, IntegerBounds bounds, List<SwitchInfo> variants, int startPosition) {
        Shape shape = new Shape("switchesBackground");
        this.addMeter(shape);
        int size = 11;

        String group = this.createGroup();
        String imageGroup = this.createGroup();

        int upperBound = bounds.y + 1;
        Resource symbolFont = new ResourceFactory().jarFont("/rxtd/flexmon/fonts/Rhomus_Omnilots.ttf", "Rhomus Omnilots");

        for (int i = 0; i < variants.size(); i++) {
            var t = variants.get(i);
            IntegerBounds ib = new IntegerBounds(bounds.x + bounds.w - (size + 2) * (variants.size() - i), upperBound, size, size);
            ShapeRectangle rectangle = new ShapeRectangle(ib.x, ib.y, ib.w, ib.h);
            rectangle.addModifier(new StrokeWidth(0)).addModifier(new Fill(t.color));
            shape.addShape(rectangle);

            Shape shape2 = new Shape("selectFrame" + i);
            this.addMeter(shape2);
            int offset = 1;
            ShapeRectangle actionOutline = ShapeRectangle.sharpOutline(ib.x - offset, ib.y - offset, ib.w + offset * 2, ib.h + offset * 2, 1, new Color(100, 100, 100));
            shape2.addShape(actionOutline)
                    .addGroup(group);
            if (i != startPosition) {
                shape2.setHidden(true);
            }

            Label label = new Label("selectLetter" + i);
            this.addMeter(label);
            label.setX(ib.x + 2).setY(ib.y).setText(t.letter.toString())
                    .setFontSize(size * 3 / 4);

            Image image = new Image("selectCover" + i);
            this.addMeter(image);

            Action action = new ActionChain()
                    .append(t.action)
                    .append(BangUtils.showMeterGroup(imageGroup, null))
                    .append(BangUtils.hideMeter(image.getName(), null))
                    .append(BangUtils.hideMeterGroup(group, null))
                    .append(BangUtils.showMeter(shape2.getName(), null))
                    .append(BangUtils.updateMeasure("*", null))
                    .append(BangUtils.updateMeter("*", null))
                    .append(BangUtils.redraw(null));


            image.setX(ib.x).setY(ib.y).setW(ib.w).setH(ib.h)
                    .setSolidColor(new Color(0, 0, 0, 1))
                    .setMouseAction(MeterBase.MouseButton.LEFT, MeterBase.MouseAction.UP, action)
                    .setToolTipText(t.tooltip)
                    .addGroup(imageGroup);

            if (i == startPosition) {
                image.setHidden(true);
            }
        }

        // stop/resume
        {
            IntegerBounds ib = new IntegerBounds(bounds.x + bounds.w - (size + 2) * (variants.size() + 1), upperBound, size, size);
            ShapeRectangle rectangle = new ShapeRectangle(ib.x, ib.y, ib.w, ib.h);
            rectangle.addModifier(new StrokeWidth(0)).addModifier(new Fill(new Color(0, 0, 0, 100)));
            shape.addShape(rectangle);

            Label labelStop = new Label("stop");
            this.addMeter(labelStop);
            labelStop
                    .setX(ib.x + 1)
                    .setY(ib.y - 0)
                    .setText("\u2759\u200A\u2759")
                    .setFontSize((int) Math.round(size * 0.7))
                    .setFontFace(symbolFont);

            Label labelResume = new Label("resume");
            this.addMeter(labelResume);
            labelResume
                    .setX(ib.x + 2)
                    .setY(ib.y + 1)
                    .setText(":") // \u25BA but ':' because font creators are fucking stupid
                    .setFontSize((int) Math.round(size * 0.7))
                    .setFontFace(symbolFont)
                    .setHidden(true);

            Image imageStop = new Image("stopCover");
            Image imageResume = new Image("resumeCover");

            this.addMeter(imageStop);
            imageStop
                    .setSolidColor(new Color(0, 0, 0, 1))
                    .setMouseAction(MeterBase.MouseButton.LEFT, MeterBase.MouseAction.UP, new ActionChain()
                            .append(parent.bangStop())
                            .append(BangUtils.hideMeter(labelStop.getName(), null))
                            .append(BangUtils.hideMeter(imageStop.getName(), null))
                            .append(BangUtils.showMeter(labelResume.getName(), null))
                            .append(BangUtils.showMeter(imageResume.getName(), null))
                            .append(BangUtils.redraw(null)))
                    .setToolTipText("Pause");
            this.setMeterBounds(imageStop, ib);

            this.addMeter(imageResume);
            imageResume
                    .setSolidColor(new Color(0, 0, 0, 1))
                    .setMouseAction(MeterBase.MouseButton.LEFT, MeterBase.MouseAction.UP, new ActionChain()
                            .append(parent.bangResume())
                            .append(BangUtils.showMeter(labelStop.getName(), null))
                            .append(BangUtils.showMeter(imageStop.getName(), null))
                            .append(BangUtils.hideMeter(labelResume.getName(), null))
                            .append(BangUtils.hideMeter(imageResume.getName(), null))
                            .append(BangUtils.redraw(null)))
                    .setToolTipText("Resume")
                    .setHidden(true);
            this.setMeterBounds(imageResume, ib);
        }

        {
            // prev
            IntegerBounds ib = new IntegerBounds(bounds.x + 2 + (size + 2) * 0, upperBound, size, size);
            ShapeRectangle rectangle = new ShapeRectangle(ib.x, ib.y, ib.w, ib.h);
            rectangle.addModifier(new StrokeWidth(0)).addModifier(new Fill(new Color(0, 0, 0, 100)));
            shape.addShape(rectangle);

            Label label = new Label("prev");
            this.addMeter(label);
            label
                    .setX(ib.x + 1)
                    .setY(ib.y - 1)
                    .setText("\u23f4")
                    .setFontSize((int) Math.round(size * 0.75))
                    .setFontFace(symbolFont);

            Image image = new Image("prevCover");
            this.addMeter(image);
            image
                    .setSolidColor(new Color(0, 0, 0, 1))
                    .setMouseAction(MeterBase.MouseButton.LEFT, MeterBase.MouseAction.UP, new ActionChain()
                            .append(parent.bangSetIndexOffset("-" + offsetStep))
                            .append(BangUtils.updateMeasureGroup(updateGroup, null))
                            .append(BangUtils.updateMeter("*", null))
                            .append(BangUtils.redraw(null)));
            this.setMeterBounds(image, ib);
        }
        {
            // next
            IntegerBounds ib = new IntegerBounds(bounds.x + 2 + (size + 2) * 1, upperBound, size, size);
            ShapeRectangle rectangle = new ShapeRectangle(ib.x, ib.y, ib.w, ib.h);
            rectangle.addModifier(new StrokeWidth(0)).addModifier(new Fill(new Color(0, 0, 0, 100)));
            shape.addShape(rectangle);

            Label label = new Label("next");
            this.addMeter(label);
            label
                    .setX(ib.x)
                    .setY(ib.y - 1)
                    .setText("\u23f5")
                    .setFontSize((int) Math.round(size * 0.75))
                    .setFontFace(symbolFont);

            Image image = new Image("nextCover");
            this.addMeter(image);
            image
                    .setSolidColor(new Color(0, 0, 0, 1))
                    .setMouseAction(MeterBase.MouseButton.LEFT, MeterBase.MouseAction.UP, new ActionChain()
                            .append(parent.bangSetIndexOffset("+" + offsetStep))
                            .append(BangUtils.updateMeasureGroup(updateGroup, null))
                            .append(BangUtils.updateMeter("*", null))
                            .append(BangUtils.redraw(null)));
            this.setMeterBounds(image, ib);
        }
    }

    public Skin constructProcessor(int instancesCount) {
        this.skin.getRainmeterSection().setFPS(1 / 2.0);

        IntegerBounds headerBounds = this.addHeader("Top " + instancesCount + " CPU, % usage");
        int upperBound = this.addBreaker(Color.BLACK, 1);

        PerfMonRXTD parent = new PerfMonRXTD("perfmonPdhParent")
                .wrap("Plugins/PerfMonRxtd/v1.2.0")
                .setSyncRawFormatted(false)
                .setLimitIndexOffset(true)
                .setCategory("Process")
                .setCounterList("% Processor Time")
                .setExpressionList("cr0 * " + SkinUtils.print(1 / Math.pow(10, 7)))
                .setSortBy(PerfMonRXTD.SortBy.FORMATTED_COUNTER)
                .setRollup(true)
                .setSortRollupFunction(PerfMonRXTD.RollupFunction.SUM)
                .setBlacklist(Arrays.asList("Idle", "_Total"));
        this.skin.add(this.params.measureQueue, parent);

        ActionTimer actionTimer = new ActionTimer("initialUpdateTimer");
        this.addMeasure(actionTimer);
        parent.addInitialAction(actionTimer.bangExecute(new ActionTimer.ActionList().addElement(new ActionTimer.Wait(100)).addElement(new ActionTimer.ExecuteAction(new ActionTimer.NamedAction(null,
                new ActionChain()
                        .append(BangUtils.updateMeasure(parent.getName(), null))
                        .append(BangUtils.updateMeasureGroup(this.updateGroup, null))
                        .append(BangUtils.updateMeter("*", null))
                        .append(BangUtils.redraw(null))
        )))));

        for (int i = 0; i < instancesCount; i++) {
            var current = parent.createChild("current_" + i)
                    .setType(PerfMonRXTD.ChildType.GET_FORMATTED_COUNTER)
                    .setInstanceIndex(i)
                    .setResultString(PerfMonRXTD.ResultString.DISPLAY_NAME)
                    .addGroup(this.updateGroup);
            this.addMeasure(current);
            var totalCpu = parent.createChild("cpuTime_" + i)
                    .setType(PerfMonRXTD.ChildType.GET_EXPRESSION)
                    .setInstanceIndex(i)
                    .setResultString(PerfMonRXTD.ResultString.NUMBER)
                    .addGroup(this.updateGroup);
            this.addMeasure(totalCpu);
            this.addCpuLine(current, current, totalCpu, i);
            this.addBreaker(new Color(100, 100, 100, 150), 1);
        }

        this.addSwitches(parent, instancesCount, headerBounds, Arrays.asList(
                new SwitchInfo('U', "Usage", new Color(100, 0, 0, 100), BangUtils.setOption(parent.getName(), "SortBy", PerfMonRXTD.SortBy.FORMATTED_COUNTER.toString(), null)),
                new SwitchInfo('T', "Time", new Color(0, 0, 100, 100), BangUtils.setOption(parent.getName(), "SortBy", PerfMonRXTD.SortBy.RAW_COUNTER.toString(), null))
        ), 0);

        IntegerBounds lastBounds = this.layout.getCurrentBounds();
        this.addVerticalLine(upperBound, lastBounds.y + lastBounds.h, (int) Math.round(lastBounds.x + this.params.fontSize * this.baseOffsetMultiplier + this.nameOffset / 2.0), new Color(100, 100, 100, 100));

        this.backgroundSolid();
        this.outlineSolid();
        this.gridSolid();

        return this.skin;
    }

    public Skin constructMemory(int instancesCount) {
        // https://superuser.com/questions/895168/how-to-measure-total-ram-usage-of-a-program-under-windows
        this.skin.getRainmeterSection().setFPS(1 / 2.0);

        IntegerBounds headerBounds = this.addHeader("Top " + instancesCount + " memory, Bytes");
        int upperBound = this.addBreaker(Color.BLACK, 1);

        PerfMonRXTD parent = new PerfMonRXTD("perfmonPdhParent")
                .wrap("Plugins/PerfMonRxtd/v1.2.0")
                .setSyncRawFormatted(false)
                .setLimitIndexOffset(true)
                .setCategory("Process")
                .setCounterList("Working Set", "Working Set - Private", "Page File Bytes")
                .setExpressionList("CounterRaw00 - CounterRaw01")
                .setRollupExpressionList("CounterRaw01Sum + Expression00Max")
                .setSortBy(PerfMonRXTD.SortBy.ROLLUP_EXPRESSION)
                .setSortIndex(0)
                .setRollup(true)
                .setBlacklist("_Total", "Memory Compression");

        this.skin.add(this.params.measureQueue, parent);
        for (int i = 0; i < instancesCount; i++) {
            var ramApprox = parent.createChild("ramApprox_" + i)
                    .setType(PerfMonRXTD.ChildType.GET_ROLLUP_EXPRESSION)
                    .setCounterIndex(0)
                    .setInstanceIndex(i)
                    .setResultString(PerfMonRXTD.ResultString.DISPLAY_NAME)
                    .addGroup(this.updateGroup);
            this.addMeasure(ramApprox);
            var pagefileSum = parent.createChild("pagefile_" + i)
                    .setType(PerfMonRXTD.ChildType.GET_RAW_COUNTER)
                    .setRollupFunction(PerfMonRXTD.RollupFunction.SUM)
                    .setCounterIndex(2)
                    .setInstanceIndex(i)
                    .setResultString(PerfMonRXTD.ResultString.DISPLAY_NAME)
                    .addGroup(this.updateGroup);
            this.addMeasure(pagefileSum);
//            this.addMemoryLine(ramApprox, ramApprox, workingSetSum, pagefileSum);
            this.addMemoryLine(ramApprox, ramApprox, pagefileSum, i);
            this.addBreaker(new Color(100, 100, 100, 150), 1);
        }

        IntegerBounds lastBounds = this.layout.getCurrentBounds();
        this.addVerticalLine(upperBound, lastBounds.y + lastBounds.h, (int) Math.round(lastBounds.x + this.params.fontSize * this.baseOffsetMultiplier + this.nameOffset / 2.0), new Color(100, 100, 100, 100));

        this.addSwitches(parent, instancesCount, headerBounds, Arrays.asList(
                new SwitchInfo('R', "Ram Approx", new Color(100, 0, 0, 100), new ActionChain()
                        .append(BangUtils.setOption(parent.getName(), "SortBy", PerfMonRXTD.SortBy.ROLLUP_EXPRESSION.toString(), null))
                        .append(BangUtils.setOption(parent.getName(), "SortIndex", "0", null))),
                new SwitchInfo('C', "Commit Approx", new Color(0, 0, 100, 100), new ActionChain()
                        .append(BangUtils.setOption(parent.getName(), "SortBy", PerfMonRXTD.SortBy.RAW_COUNTER.toString(), null))
                        .append(BangUtils.setOption(parent.getName(), "SortIndex", "2", null)))
        ), 0);

        this.backgroundSolid();
        this.outlineSolid();
        this.gridSolid();

        return this.skin;
    }

    public Skin constructIO(int instancesCount) {
        this.skin.getRainmeterSection().setFPS(1 / 2.0);

        IntegerBounds headerBounds = this.addHeader("Top " + instancesCount + " IO, B/s");
        int upperBound = this.addBreaker(Color.BLACK, 1);

        PerfMonRXTD parent = new PerfMonRXTD("perfmonPdhParent")
                .wrap("Plugins/PerfMonRxtd/v1.2.0")
                .setSyncRawFormatted(false)
                .setLimitIndexOffset(true)
                .setCategory("Process")
                .setCounterList("IO Data Bytes/sec")
                .setSortBy(PerfMonRXTD.SortBy.FORMATTED_COUNTER)
                .setRollup(true)
                .setSortRollupFunction(PerfMonRXTD.RollupFunction.SUM)
                .setBlacklist("_Total");

        ActionTimer actionTimer = new ActionTimer("initialUpdateTimer");
        this.addMeasure(actionTimer);
        parent.addInitialAction(actionTimer.bangExecute(new ActionTimer.ActionList().addElement(new ActionTimer.Wait(100)).addElement(new ActionTimer.ExecuteAction(new ActionTimer.NamedAction(null,
                new ActionChain()
                        .append(BangUtils.updateMeasure(parent.getName(), null))
                        .append(BangUtils.updateMeasureGroup(this.updateGroup, null))
                        .append(BangUtils.updateMeter("*", null))
                        .append(BangUtils.redraw(null))
        )))));

        this.addMeasure(parent);
        for (int i = 0; i < instancesCount; i++) {
            var current = parent.createChild("current_" + i)
                    .setType(PerfMonRXTD.ChildType.GET_FORMATTED_COUNTER)
                    .setInstanceIndex(i)
                    .setResultString(PerfMonRXTD.ResultString.DISPLAY_NAME)
                    .setRollupFunction(PerfMonRXTD.RollupFunction.SUM)
                    .addGroup(this.updateGroup);
            this.addMeasure(current);
            var total = parent.createChild("total_" + i)
                    .setType(PerfMonRXTD.ChildType.GET_RAW_COUNTER)
                    .setInstanceIndex(i)
                    .setResultString(PerfMonRXTD.ResultString.DISPLAY_NAME)
                    .setRollupFunction(PerfMonRXTD.RollupFunction.SUM)
                    .addGroup(this.updateGroup);
            this.addMeasure(total);
            this.addIOLine(current, current, total, i);
            this.addBreaker(new Color(100, 100, 100, 150), 1);
        }

        this.addSwitches(parent, instancesCount, headerBounds, Arrays.asList(
                new SwitchInfo('R', "Rate", new Color(100, 0, 0, 100), BangUtils.setOption(parent.getName(), "SortBy", PerfMonRXTD.SortBy.FORMATTED_COUNTER.toString(), null)),
                new SwitchInfo('T', "Total", new Color(0, 0, 100, 100), BangUtils.setOption(parent.getName(), "SortBy", PerfMonRXTD.SortBy.RAW_COUNTER.toString(), null))
        ), 0);

        IntegerBounds lastBounds = this.layout.getCurrentBounds();
        this.addVerticalLine(upperBound, lastBounds.y + lastBounds.h, (int) Math.round(lastBounds.x + this.params.fontSize * this.baseOffsetMultiplier + this.nameOffset / 2.0), new Color(100, 100, 100, 100));

        this.backgroundSolid();
        this.outlineSolid();
        this.gridSolid();

        return this.skin;
    }

    private static class SwitchInfo {
        final Character letter;
        final String tooltip;
        final Color color;
        final Action action;

        private SwitchInfo(Character letter, String tooltip, Color color, Action action) {
            this.letter = letter;
            this.tooltip = tooltip;
            this.color = color;
            this.action = action;
        }
    }
}
