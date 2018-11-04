package rxtd.flexmon.templates;

import rxtd.Pair;
import rxtd.rainmeter.Skin;
import rxtd.rainmeter.SkinUtils;
import rxtd.rainmeter.actions.Action;
import rxtd.rainmeter.actions.ActionChain;
import rxtd.rainmeter.actions.BangUtils;
import rxtd.rainmeter.elements.MetadataSection;
import rxtd.rainmeter.elements.RainmeterSection;
import rxtd.rainmeter.elements.measures.Calc;
import rxtd.rainmeter.elements.measures.MString;
import rxtd.rainmeter.elements.measures.Measure;
import rxtd.rainmeter.elements.measures.plugins.custom.FrostedGlass;
import rxtd.rainmeter.elements.measures.scripts.FixedPrecisionFormat;
import rxtd.rainmeter.elements.measures.scripts.HistoDelta;
import rxtd.rainmeter.elements.measures.scripts.PlotManager;
import rxtd.rainmeter.elements.meters.Bar;
import rxtd.rainmeter.elements.meters.Histogram;
import rxtd.rainmeter.elements.meters.Line;
import rxtd.rainmeter.elements.meters.Meter;
import rxtd.rainmeter.elements.meters.Orientation;
import rxtd.rainmeter.elements.meters.StartPlace;
import rxtd.rainmeter.elements.meters.shape.Shape;
import rxtd.rainmeter.elements.meters.shape.shapetypes.Combine;
import rxtd.rainmeter.elements.meters.shape.shapetypes.ShapeElement;
import rxtd.rainmeter.elements.meters.shape.shapetypes.ShapeLine;
import rxtd.rainmeter.elements.meters.shape.shapetypes.ShapeRectangle;
import rxtd.rainmeter.elements.meters.shape.shapetypes.modifiers.attribute.Fill;
import rxtd.rainmeter.elements.meters.shape.shapetypes.modifiers.attribute.Stroke;
import rxtd.rainmeter.elements.meters.shape.shapetypes.modifiers.attribute.StrokeWidth;
import rxtd.rainmeter.elements.meters.string.Label;
import rxtd.rainmeter.formulas.Formula;
import rxtd.rainmeter.variables.Variable;
import rxtd.rainmeter.variables.Variables;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Widget {
    private static boolean mode4_3 = true;
    protected final Parameters params = new Parameters();
    protected final Skin skin;
    protected final Layout layout;
    protected final List<ShapeElement> mask = new ArrayList<>();
    protected final List<IntegerBounds> plotPlaces = new ArrayList<>();
    private final List<Pair<IntegerBounds, List<Integer>>> grid = new ArrayList<>();
    private final List<IntegerBounds> outline = new ArrayList<>();
    private final AtomicInteger uid = new AtomicInteger(0);
    protected Label labelStyle = null;
    private FixedPrecisionFormat formatBinaryRaw = null;
    private FixedPrecisionFormat formatBinary = null;
    private FixedPrecisionFormat formatMetric = null;
    private FixedPrecisionFormat formatPercent = null;
    private int savedBoundsHeight = 0;

    public Widget(String name) {
        this.skin = Skin.create(name);

        this.layout = new Layout(this.params.skinWidth, this.params.skinBorderWidth, this.params.paddingWidth, this.params.elementInterval, this.params.fontSize);

        this.init(name);
    }

    public Widget(String name, Layout layout) {
        this.skin = Skin.create(name);

        this.layout = layout;

        this.init(name);
    }

    public static void setMode(String version) {
        if ("4.3".equals(version)) {
            mode4_3 = true;
        } else if ("4.2".equals(version)) {
            mode4_3 = false;
        } else {
            throw new IllegalArgumentException();
        }
    }

    private void init(String name) {
        MetadataSection metadata = new MetadataSection();
        metadata.setAuthor("rxtd");
        metadata.setSkinName(name);
        skin.setMetadata(metadata);

        RainmeterSection rainmeter = new RainmeterSection();
        rainmeter.setFPS(this.params.defaultFps);
        skin.setRainmeterSection(rainmeter);

        FrostedGlass blur = new FrostedGlass("blurBehind");
        skin.add(0, blur);
        blur.disableUpdate();
    }

    protected void addMeasure(Measure measure) {
        this.skin.add(this.params.measureQueue, measure);
    }

    protected void addMeter(Meter meter) {
        this.skin.add(this.params.meterQueue, meter);
    }

    protected Color makeSaturatedColor(Color color) {
        double max = Math.max(color.getRed(), Math.max(color.getBlue(), color.getGreen()));
        int red = (int) Math.round(color.getRed() / max * 255);
        int green = (int) Math.round(color.getGreen() / max * 255);
        int blue = (int) Math.round(color.getBlue() / max * 255);
        return new Color(red, green, blue);
    }

    protected Color makeShadow(Color color) {
        if (color == null) return null;
        return new Color(color.getRGB() & 0xFFFFFF | this.params.shadowTranslucency << 24, true);
    }

    protected Color makeWeakLine(Color color) {
        return new Color(color.getRGB() & 0xFFFFFF | this.params.weakLineTranslucency << 24, true);
    }

    protected Color makeWeakShadow(Color color) {
        return new Color(color.getRGB() & 0xFFFFFF | this.params.weakShadowTranslucency << 24, true);
    }

    protected FixedPrecisionFormat getFormatBinaryRaw() {
        if (this.formatBinaryRaw == null) {
            this.formatBinaryRaw = new FixedPrecisionFormat();
            this.skin.add(this.params.supportQueue, this.formatBinaryRaw);
        }
        return formatBinaryRaw;
    }

    protected FixedPrecisionFormat getFormatBinary() {
        if (this.formatBinary == null) {
            this.formatBinary = new FixedPrecisionFormat("formatBinary").setPostfix("B");
            this.skin.add(this.params.supportQueue, this.formatBinary);
        }
        return this.formatBinary;
    }

    protected FixedPrecisionFormat getFormatMetric() {
        if (this.formatMetric == null) {
            this.formatMetric = new FixedPrecisionFormat().setDivisor(1000);
            this.skin.add(this.params.supportQueue, this.formatMetric);
        }
        return this.formatMetric;
    }

    protected FixedPrecisionFormat getFormatPercent() {
        if (this.formatPercent == null) {
            this.formatPercent = new FixedPrecisionFormat()
                    .setBeforeSuffix("")
                    .setSuffixes("")
                    .setPostfix("%");
            this.skin.add(this.params.supportQueue, this.formatPercent);
        }
        return this.formatPercent;
    }

    protected Label getLabelStyle() {
        if (this.labelStyle != null) {
            return this.labelStyle;
        }
        this.labelStyle = new Label("commonLabelStyle");
        this.skin.add(this.params.supportQueue, this.labelStyle);

        this.labelStyle
                .setFontColor(this.params.textColor)
                .setFontFace(this.params.defaultFont)
                .setFontSize(this.params.fontSize)
                .setFontWeight(this.params.defaultFontWeight);

        return this.labelStyle;
    }

    protected void addDerivative(Measure current, Color outlineColor, boolean createDeltaLabel) {
        IntegerBounds labelBounds = this.layout.nextLine();

        int halfHeight = this.params.derivativeHistoHeight / 2;
        int height = halfHeight * 2 - 1;

        IntegerBounds bounds = this.layout.nextElement(height);
        IntegerBounds histoBounds = bounds.excludeBorder(this.params.histoOutlineWidth, this.params.histoOutlineWidth, 0, 0);
        this.plotPlaces.add(histoBounds);

        String group = this.createGroup();

        Variable previousValue = Variables.create("prevVal", new Formula(current).toString());
        skin.add(previousValue);
        current.addInitialAction(BangUtils.setVariable(previousValue.getName(), new Formula(current).toString(), null));

        Calc derivative = new Calc("derivative");
        this.addMeasure(derivative);
        derivative.setFormula(new Formula(current).subtract(new Formula(previousValue)))
                .setUpdateAction(BangUtils.setVariable(previousValue.getName(), new Formula(current).toString(), null))
                .setAverageSize(this.params.derivativeSmoothingFactor);

        Calc inverted = new Calc("invertedDerivative");
        this.addMeasure(inverted);
        inverted.setFormula(new Formula(derivative).inverse());

        PlotManager plotManager = new PlotManager("derivativePeak").setFormula(new Formula(derivative)).setHistWidth(histoBounds.w).setLinkedGroup(group);
        this.addMeasure(plotManager);
        plotManager.setUseAbsValue(true);
        derivative.setMaxValue(new Formula(plotManager)).setDynamicVariables(true);
        inverted.setMaxValue(new Formula(plotManager)).setDynamicVariables(true);

        List<Label> labels;
        if (createDeltaLabel) {
            labels = this.addSpreadHeaderLabels(4, labelBounds, 0.0, 0.8);
            Label histoDeltaLabel = labels.get(3);
            this.addDelta(current, histoDeltaLabel, histoBounds.w);

            if (this.params.useTooltips) {
                labels.get(3).setToolTipText("Plot delta");
            }
        } else {
            labels = this.addSpreadHeaderLabels(3, labelBounds, 0.0, 0.8);
        }

        MString helper1 = new MString("derivativeCurrentHelper");
        this.addMeasure(helper1);
        Label derivativeCurrentLabel = labels.get(0).setText(helper1);

        MString helper2 = new MString("derivativeHistoMaxAbsHelper");
        this.addMeasure(helper2);
        Label derivativeHistoMaxAbsLabel = labels.get(1).setText(helper2);

        MString helper3 = new MString("derivativeAllTimeMaxAbsHelper");
        this.addMeasure(helper3);
        Label derivativeAllTimeMaxAbsLabel = labels.get(2).setText(helper3);

        if (this.params.useTooltips) {
            derivativeCurrentLabel.setToolTipText("Current");
            derivativeHistoMaxAbsLabel.setToolTipText("Plot peak");
            derivativeAllTimeMaxAbsLabel.setToolTipText("Total peak");
        }

        Action labelUpdate = new ActionChain()
                .append(BangUtils.setOption(helper1.getName(), "String", this.getFormatBinary().formulaFormatNumber(new Formula(derivative), this.params.precision).toString(), null))
                .append(BangUtils.setOption(helper2.getName(), "String", this.getFormatBinary().formulaFormatNumber(plotManager.formulaMax(), this.params.precision).toString(), null))
                .append(BangUtils.setOption(helper3.getName(), "String", this.getFormatBinary().formulaFormatNumber(plotManager.formulaAllTimeMax(), this.params.precision).toString(), null));
        plotManager.addInitialAction(labelUpdate);
        plotManager.addUpdateAction(labelUpdate);

        if (mode4_3) {
            IntegerBounds integerBoundsPositive = new IntegerBounds(histoBounds.x, histoBounds.y, histoBounds.w, halfHeight);
            Histogram histogramPositive = new Histogram("derivativePositiveHisto");
            skin.add(this.params.meterQueue, histogramPositive);
            histogramPositive.setMeasure(derivative).setColor(this.params.positiveDeltaShadowColor).setGraphStart(StartPlace.LEFT).setAntiAlias(true);
            this.setMeterBounds(histogramPositive, integerBoundsPositive);

            Line linePositive = new Line("derivativePositiveLine");
            skin.add(this.params.meterQueue, linePositive);
            linePositive.addLine(derivative, this.params.positiveDeltaColor).setGraphStart(StartPlace.LEFT).setAntiAlias(true);
            this.setMeterBounds(linePositive, integerBoundsPositive);


            IntegerBounds integerBoundsNegative = new IntegerBounds(histoBounds.x, histoBounds.y + halfHeight - 1, histoBounds.w, halfHeight);
            Histogram histogramNegative = new Histogram("derivativeNegativeHisto");
            skin.add(this.params.meterQueue, histogramNegative);
            histogramNegative.setMeasure(inverted).setColor(this.params.negativeDeltaShadowColor).setGraphStart(StartPlace.LEFT).setAntiAlias(true).setFlip(true);
            this.setMeterBounds(histogramNegative, integerBoundsNegative);

            Line lineNegative = new Line("derivativeNegativeLine");
            skin.add(this.params.meterQueue, lineNegative);
            lineNegative.addLine(inverted, this.params.negativeDeltaColor).setGraphStart(StartPlace.LEFT).setAntiAlias(true).setFlip(true);
            this.setMeterBounds(lineNegative, integerBoundsNegative);

            double lineY = bounds.y + halfHeight + this.params.histoOutlineWidth - 1.5;
            ShapeLine line = new ShapeLine(bounds.x, lineY, bounds.x + bounds.w, lineY);
            line.addModifier(new Stroke(this.params.derivativeLineColor)).addModifier(new StrokeWidth(1));
            this.mask.add(line);
        } else {
            IntegerBounds integerBoundsPositive = new IntegerBounds(histoBounds.x, histoBounds.y, histoBounds.w, halfHeight);
            Histogram histogramPositive = new Histogram("derivativePositiveHisto");
            skin.add(this.params.meterQueue, histogramPositive);
            histogramPositive.setMeasure(derivative).setColor(this.params.positiveDeltaShadowColor).setGraphStart(StartPlace.LEFT).setAntiAlias(true);
            this.setMeterBounds(histogramPositive, integerBoundsPositive);

            Line linePositive = new Line("derivativePositiveLine");
            skin.add(this.params.meterQueue, linePositive);
            linePositive.addLine(derivative, this.params.positiveDeltaColor).setGraphStart(StartPlace.LEFT).setAntiAlias(true);
            this.setMeterBounds(linePositive, integerBoundsPositive);


            IntegerBounds integerBoundsNegative = new IntegerBounds(histoBounds.x, histoBounds.y + halfHeight - 1, histoBounds.w, halfHeight);
            Histogram histogramNegative = new Histogram("derivativeNegativeHisto");
            skin.add(this.params.meterQueue, histogramNegative);
            histogramNegative.setMeasure(inverted).setColor(this.params.negativeDeltaShadowColor).setGraphStart(StartPlace.LEFT).setAntiAlias(true).setFlip(true);
            this.setMeterBounds(histogramNegative, integerBoundsNegative);

            Line lineNegative = new Line("derivativeNegativeLine");
            skin.add(this.params.meterQueue, lineNegative);
            lineNegative.addLine(inverted, this.params.negativeDeltaColor).setGraphStart(StartPlace.LEFT).setAntiAlias(true).setFlip(true);
            this.setMeterBounds(lineNegative, integerBoundsNegative);

            ShapeLine line = new ShapeLine(bounds.x, bounds.y + halfHeight + this.params.histoOutlineWidth - 1, bounds.x + bounds.w, bounds.y + halfHeight + this.params.histoOutlineWidth - 1);
            line.addModifier(new Stroke(this.params.plotBackColor)).addModifier(new StrokeWidth(2));
            this.mask.add(line);
        }

        this.addHistoGrid(histoBounds, this.params.derivativeHistoHorizontalLines, this.params.histoVerticalLines);

        ShapeRectangle outline2 = ShapeRectangle.sharpOutline(bounds.x, bounds.y, bounds.w, bounds.h, this.params.histoOutlineWidth, outlineColor);
        this.mask.add(outline2);
//        System.err.println("outline2");
    }

    protected ShapeRectangle createRectangle(IntegerBounds bounds) {
        return new ShapeRectangle(bounds.x, bounds.y, bounds.w, bounds.h);
    }

    protected void backgroundSolid() {
        Shape background = new Shape("background");
        skin.add(this.params.backgroundQueue, background);
        background.disableUpdate();

        IntegerBounds fullBounds = layout.getFullBounds();
        ShapeRectangle border = this.createRectangle(fullBounds);
        border.addModifier(new Fill(this.params.borderColor)).addModifier(new StrokeWidth(0));
        background.addShape(border);

        IntegerBounds innerBounds = layout.getInnerBounds();
        ShapeRectangle inner = this.createRectangle(innerBounds);
        inner.addModifier(new Fill(this.params.backColor)).addModifier(new StrokeWidth(0));
        background.addShape(inner);

        Combine c1 = new Combine(border);
        c1.exclude(inner);
        background.addShape(c1);

        Combine c2 = new Combine(inner);
        for (var bounds : this.plotPlaces) {
            var rect = this.createRectangle(bounds);
            rect.addModifier(new StrokeWidth(0)).addModifier(new Fill(this.params.plotBackColor));
            background.addShape(rect);
            c2.exclude(rect);
            Combine combine = new Combine(rect);
            background.addShape(combine);
        }
        background.addShape(c2);
    }

    protected Pair<Meter, IntegerBounds> backgroundStart() {
        Shape background = new Shape("backgroundStart");
        skin.add(this.params.backgroundQueue, background);
        background.disableUpdate();

        IntegerBounds fullBounds = layout.getFullBounds();
        int newSavedBoundsHeight = fullBounds.y + fullBounds.h;
        fullBounds = fullBounds.move(0, -this.savedBoundsHeight);

        ShapeRectangle border = this.createRectangle(fullBounds);
        border.addModifier(new Fill(this.params.borderColor)).addModifier(new StrokeWidth(0));
        background.addShape(border);

        background.setX(fullBounds.x);
        background.setY(this.savedBoundsHeight);

        IntegerBounds innerBounds = layout.getInnerBounds();
        innerBounds = innerBounds.excludeBorder(0, 0, 0, -innerBounds.y).move(0, -this.savedBoundsHeight);
        ShapeRectangle inner = this.createRectangle(innerBounds);
        inner.addModifier(new Fill(this.params.backColor)).addModifier(new StrokeWidth(0));
        background.addShape(inner);

        Combine c1 = new Combine(border);
        c1.exclude(inner);
        background.addShape(c1);

        Combine c2 = new Combine(inner);
        for (var bounds : this.plotPlaces) {
            bounds = bounds.move(0, -this.savedBoundsHeight);
            var rect = this.createRectangle(bounds);
            rect.addModifier(new StrokeWidth(0)).addModifier(new Fill(this.params.plotBackColor));
            background.addShape(rect);
            c2.exclude(rect);
            Combine combine = new Combine(rect);
            background.addShape(combine);
        }
        background.addShape(c2);

        this.plotPlaces.clear();

        Pair<Meter, IntegerBounds> ret = new Pair<>(background, fullBounds.move(0, this.savedBoundsHeight));

        this.savedBoundsHeight = newSavedBoundsHeight;
        return ret;
    }

    protected Pair<Meter, IntegerBounds> backgroundPart() {
        Shape background = new Shape("backgroundPart");
        skin.add(this.params.backgroundQueue, background);
        background.disableUpdate();

        IntegerBounds fullBounds = layout.getFullBounds();
        int newSavedBoundsHeight = fullBounds.y + fullBounds.h;
        fullBounds = fullBounds.excludeBorder(0, 0, this.savedBoundsHeight, 0).move(0, -this.savedBoundsHeight);

        ShapeRectangle border = this.createRectangle(fullBounds);
        border.addModifier(new Fill(this.params.borderColor)).addModifier(new StrokeWidth(0));
        background.addShape(border);

        background.setX(fullBounds.x);
        background.setY(this.savedBoundsHeight);

        IntegerBounds innerBounds = layout.getInnerBounds();
        innerBounds = new IntegerBounds(innerBounds.x, 0, innerBounds.w, fullBounds.h);
        ShapeRectangle inner = this.createRectangle(innerBounds);
        inner.addModifier(new Fill(this.params.backColor)).addModifier(new StrokeWidth(0));
        background.addShape(inner);

        Combine c1 = new Combine(border);
        c1.exclude(inner);
        background.addShape(c1);

        Combine c2 = new Combine(inner);
        for (var bounds : this.plotPlaces) {
            bounds = bounds.move(0, -this.savedBoundsHeight);
            var rect = this.createRectangle(bounds);
            rect.addModifier(new StrokeWidth(0)).addModifier(new Fill(this.params.plotBackColor));
            background.addShape(rect);
            c2.exclude(rect);
            Combine combine = new Combine(rect);
            background.addShape(combine);
        }
        background.addShape(c2);

        this.plotPlaces.clear();

        Pair<Meter, IntegerBounds> ret = new Pair<>(background, fullBounds.move(0, this.savedBoundsHeight));

        this.savedBoundsHeight = newSavedBoundsHeight;
        return ret;
    }

    protected Pair<Meter, IntegerBounds> backgroundEnd() {
        Shape background = new Shape("backgroundEnd");
        skin.add(this.params.backgroundQueue, background);
        background.disableUpdate();

        IntegerBounds fullBounds = layout.getFullBounds();
        int newSavedBoundsHeight = fullBounds.y + fullBounds.h;
        fullBounds = fullBounds.excludeBorder(0, 0, this.savedBoundsHeight, -1).move(0, -this.savedBoundsHeight);

        ShapeRectangle border = this.createRectangle(fullBounds);
        border.addModifier(new Fill(this.params.borderColor)).addModifier(new StrokeWidth(0));
        background.addShape(border);

        background.setX(fullBounds.x);
        background.setY(this.savedBoundsHeight);

        IntegerBounds innerBounds = layout.getInnerBounds();
        innerBounds = new IntegerBounds(innerBounds.x, fullBounds.y, innerBounds.w, fullBounds.h - layout.getFullBounds().h - innerBounds.h - layout.getFullBounds().y).move(0, -this.savedBoundsHeight);
        ShapeRectangle inner = this.createRectangle(innerBounds);
        inner.addModifier(new Fill(this.params.backColor)).addModifier(new StrokeWidth(0));
        background.addShape(inner);

        Combine c1 = new Combine(border);
        c1.exclude(inner);
        background.addShape(c1);

        Combine c2 = new Combine(inner);
        for (var bounds : this.plotPlaces) {
            bounds = bounds.move(0, -this.savedBoundsHeight);
            var rect = this.createRectangle(bounds);
            rect.addModifier(new StrokeWidth(0)).addModifier(new Fill(this.params.plotBackColor));
            background.addShape(rect);
            c2.exclude(rect);
            Combine combine = new Combine(rect);
            background.addShape(combine);
        }
        background.addShape(c2);

        this.plotPlaces.clear();

        Pair<Meter, IntegerBounds> ret = new Pair<>(background, fullBounds.move(0, this.savedBoundsHeight));

        this.savedBoundsHeight = newSavedBoundsHeight;
        return ret;
    }

    protected void addRectangleOutline(IntegerBounds bounds) {
        this.outline.add(bounds);
    }

    protected void outlineSolid() {
        Shape shape = new Shape("outline");
        this.skin.add(this.params.outlineQueue, shape);
        shape.disableUpdate();

        for (var e : this.mask) {
            shape.addShape(e);
        }
    }

    protected Meter outlinePart(IntegerBounds bounds) {
        Shape shape = new Shape("outlinePart");
        this.skin.add(this.params.outlineQueue, shape);
        shape.disableUpdate()
                .setX(bounds.x)
                .setY(bounds.y);

        for (var e : this.outline) {
            ShapeRectangle outline = ShapeRectangle.sharpOutline(e.x - bounds.x, e.y - bounds.y, e.w, e.h, this.params.histoOutlineWidth, this.params.outlineColor);
            shape.addShape(outline);
        }
        this.outline.clear();

        return shape;
    }

    protected void addHistoGrid(IntegerBounds bounds, int horizontalLines, int verticalLines, int verticalStart, int verticalEnd) {
        this.grid.add(new Pair<>(bounds, new ArrayList<>(Arrays.asList(horizontalLines, verticalLines, verticalStart, verticalEnd))));
    }

    protected List<Meter> gridPart(Pair<IntegerBounds, Formula> positioning) {
        ArrayList<Meter> meters = new ArrayList<>();

        for (var v : this.grid) {
            IntegerBounds bounds = v.key;
            int horizontalLines = v.value.get(0);
            int verticalLines = v.value.get(1);
            int verticalStart = v.value.get(2);
            int verticalEnd = v.value.get(3);

            Shape grid = new Shape("grid");
            this.skin.add(this.params.gridQueue, grid);
            grid.disableUpdate();
            if (positioning != null) {
                grid.setX(new Formula(bounds.x).multiply(positioning.value).toString());
                grid.setY(new Formula(bounds.y).multiply(positioning.value).toString());
            } else {
                grid.setX(bounds.x);
                grid.setY(bounds.y);
            }

            double lineWidth = this.params.gridLineWidth;
            double offset = lineWidth * 0.5;
            Color color = this.params.gridLineColor;

            for (int i = 0; i < verticalLines; i++) {
                int dx = (int) Math.round(bounds.w * (i + 1) / (verticalLines + 1.0));
                if (dx < verticalStart || dx > verticalEnd) {
                    continue;
                }
                ShapeLine line = new ShapeLine(dx + offset, 0, dx + offset, bounds.h);
                line.addModifier(new Stroke(color)).addModifier(new StrokeWidth(lineWidth));
                grid.addShape(line);
            }

            for (int i = 0; i < horizontalLines; i++) {
                int dy = (int) Math.round(bounds.h * (i + 1) / (horizontalLines + 1.0));
                ShapeLine line = new ShapeLine(verticalStart, dy + offset, verticalEnd, dy + offset);
                line.addModifier(new Stroke(color)).addModifier(new StrokeWidth(lineWidth));
                grid.addShape(line);
            }

            meters.add(grid);
        }

        return meters;
    }

    protected Meter gridSolid() {
        Shape grid = new Shape("grid");
        this.skin.add(this.params.gridQueue, grid);
        grid.disableUpdate();

        for (var v : this.grid) {
            IntegerBounds bounds = v.key;
            int horizontalLines = v.value.get(0);
            int verticalLines = v.value.get(1);
            int verticalStart = v.value.get(2);
            int verticalEnd = v.value.get(3);

            double lineWidth = this.params.gridLineWidth;
            double offsetX = lineWidth * 0.5 + bounds.x;
            double offsetY = lineWidth * 0.5 + bounds.y;
            Color color = this.params.gridLineColor;

            for (int i = 0; i < verticalLines; i++) {
                int dx = (int) Math.round(bounds.w * (i + 1) / (verticalLines + 1.0));
                if (dx < verticalStart || dx > verticalEnd) {
                    continue;
                }
                ShapeLine line = new ShapeLine(dx + offsetX, bounds.y, dx + offsetX, bounds.y + bounds.h);
                line.addModifier(new Stroke(color)).addModifier(new StrokeWidth(lineWidth));
                grid.addShape(line);
            }

            for (int i = 0; i < horizontalLines; i++) {
                int dy = (int) Math.round(bounds.h * (i + 1) / (horizontalLines + 1.0));
                ShapeLine line = new ShapeLine(bounds.x + verticalStart, dy + offsetY, bounds.x + verticalEnd, dy + offsetY);
                line.addModifier(new Stroke(color)).addModifier(new StrokeWidth(lineWidth));
                grid.addShape(line);
            }
        }

        return grid;
    }

    protected void addHistoGrid(IntegerBounds bounds, int horizontalLines, int verticalLines) {
        this.addHistoGrid(bounds, horizontalLines, verticalLines, 0, bounds.w);
    }

    protected void addDelta(Measure measure, Label label, int width) {
        Calc calc = new Calc();
        this.addMeasure(calc);

        HistoDelta histoDelta = new HistoDelta("plotDelta")
                .setCurValue(new Formula(measure))
                .setHistWidth(width);
        this.addMeasure(histoDelta);

        calc.addInitialAction(histoDelta.bangReset());

        MString helper = new MString("deltaHelper");
        this.addMeasure(helper);
        label.setText(helper);

        Action update = BangUtils.setOption(helper.getName(), "String", this.getFormatBinary().formulaFormatNumber(new Formula(histoDelta), this.params.precision).toString(), null);
        histoDelta.addInitialAction(update);
        histoDelta.addUpdateAction(update);
    }

    protected void addDynamicSpeedStatisticsModule(Measure current, Measure total, String name, Color lineColor, Color shadowColor) {
        IntegerBounds headerBounds = this.layout.nextLine();
        IntegerBounds statisticsBounds = this.layout.nextLine();
        IntegerBounds plotBounds = this.layout.nextElement(this.params.middleHistoHeight);
        this.plotPlaces.add(plotBounds);

        String group = this.createGroup();

        PlotManager plotManager = new PlotManager("plotPeak")
                .setFormula(new Formula(current, Formula.MeasureParameters.NUMBER_VALUE))
                .setLinkedGroup(group);
        this.addMeasure(plotManager);

        List<Label> labelsRow1 = this.addSpreadHeaderLabels(4, headerBounds, 0.07, 0.85);

        MString helper1 = new MString("currentHelper").addGroup(group);
        this.addMeasure(helper1);
        Label currentLabel = labelsRow1.get(0).setText(helper1).addGroup(group);

        MString helper2 = new MString("histoPeakHelper").addGroup(group);
        this.addMeasure(helper2);
        Label histoPeakLabel = labelsRow1.get(1).setText(helper2).addGroup(group);

        MString helper3 = new MString("totalPeakHelper").addGroup(group);
        this.addMeasure(helper3);
        Label totalPeakLabel = labelsRow1.get(2).setText(helper3).addGroup(group);

        Label nameLabel = labelsRow1.get(3).setText(name).disableUpdate();

        List<Label> labelsRow2 = this.addSpreadHeaderLabels(4, statisticsBounds, 0.03, 0.75);

        MString helper4 = new MString("averageHelper").addGroup(group);
        this.addMeasure(helper4);
        Label averageLabel = labelsRow2.get(0).setText(helper4).addGroup(group);

        MString helper5 = new MString("histoSumHelper").addGroup(group);
        this.addMeasure(helper5);
        Label histoSumLabel = labelsRow2.get(1).setText(helper5).addGroup(group);

        MString helper6 = new MString("sessionSumHelper").addGroup(group);
        this.addMeasure(helper6);
        Label sessionSumLabel = labelsRow2.get(2).setText(helper6).addGroup(group);

        MString helper7 = new MString("totalSumHelper").addGroup(group);
        this.addMeasure(helper7);
        Label totalSumLabel = labelsRow2.get(3).setText(helper7).addGroup(group);

        if (this.params.useTooltips) {
            currentLabel.setToolTipText("Current");
            histoPeakLabel.setToolTipText("Plot peak");
            totalPeakLabel.setToolTipText("Total Peak");
            averageLabel.setToolTipText("Plot average");
            histoSumLabel.setToolTipText("Plot sum");
            sessionSumLabel.setToolTipText("Total sum");
            totalSumLabel.setToolTipText("All time sum");
        }

        ActionChain labelUpdate = new ActionChain()
                .append(BangUtils.setOption(helper1.getName(), "String", this.getFormatBinary().formulaFormatNumber(new Formula(current, Formula.MeasureParameters.NUMBER_VALUE), this.params.precision).toString() + "/s", null))
                .append(BangUtils.setOption(helper2.getName(), "String", this.getFormatBinary().formulaFormatNumber(plotManager.formulaMax(), this.params.precision).toString() + "/s", null))
                .append(BangUtils.setOption(helper3.getName(), "String", this.getFormatBinary().formulaFormatNumber(plotManager.formulaAllTimeMax(), this.params.precision).toString() + "/s", null))
                .append(BangUtils.setOption(helper4.getName(), "String", this.getFormatBinary().formulaFormatNumber(plotManager.formulaAverage(), this.params.precision).toString() + "/s", null))
                .append(BangUtils.setOption(helper5.getName(), "String", this.getFormatBinary().formulaFormatNumber(plotManager.formulaSum(), this.params.precision).toString(), null))
                .append(BangUtils.setOption(helper6.getName(), "String", this.getFormatBinary().formulaFormatNumber(plotManager.formulaSessionSum(), this.params.precision).toString(), null));

        if (total != null) {
            labelUpdate.append(BangUtils.setOption(helper7.getName(), "String", this.getFormatBinary().formulaFormatNumber(new Formula(total, Formula.MeasureParameters.NUMBER_VALUE), this.params.precision).toString(), null));
        }

        plotManager.addInitialAction(labelUpdate);
        plotManager.addUpdateAction(labelUpdate);

        Calc dynamicMeasure = new Calc("corrected").addGroup(group);
        this.skin.add(this.params.measureQueue, dynamicMeasure);
        dynamicMeasure.setDynamicVariables(true).setFormula(new Formula(current)).setMaxValue(new Formula(plotManager));

        IntegerBounds integerBounds = this.addPlot(dynamicMeasure, plotBounds, lineColor);
        plotManager.setHistWidth(integerBounds.w);

        this.addHistoGrid(plotBounds, this.params.middleHistoHorizontalLines, this.params.histoVerticalLines);

        ShapeRectangle outline = ShapeRectangle.sharpOutline(plotBounds.x, plotBounds.y, plotBounds.w, plotBounds.h, this.params.histoOutlineWidth, this.params.outlineColor);
        this.mask.add(outline);
    }

    protected void setMeterBounds(Meter meter, IntegerBounds bounds) {
        meter.setX(bounds.x).setY(bounds.y).setW(bounds.w).setH(bounds.h);
    }

    protected void addCpuModule(Measure current, Color lineColor, Color shadowColor, String name, MagnificationMode magnification) {
        IntegerBounds headerBounds = this.layout.nextLine();
        IntegerBounds plotBounds = this.layout.nextElement(this.params.bigHistoHeight);
        this.plotPlaces.add(plotBounds);
        IntegerBounds histoBounds = plotBounds.excludeBorder(this.params.histoOutlineWidth, this.params.histoOutlineWidth, this.params.histoOutlineWidth, this.params.histoOutlineWidth);

        PlotManager plotManager = new PlotManager("plotPeak").setFormula(new Formula(current, Formula.MeasureParameters.NUMBER_VALUE)).setHistWidth(histoBounds.w);
        this.skin.add(this.params.measureQueue, plotManager);

        List<Label> labels = this.addSpreadHeaderLabels(4, headerBounds, 0, 0.9);

        MString helper1 = new MString("currentHelper");
        this.addMeasure(helper1);
        Label currentLabel = labels.get(0).setText(helper1);

        MString helper2 = new MString("averageHelper");
        this.addMeasure(helper2);
        Label averageLabel = labels.get(1).setText(helper2);

        MString helper3 = new MString("maxHelper");
        this.addMeasure(helper3);
        Label maxLabel = labels.get(2).setText(helper3);

        Label nameLabel = labels.get(3).setText(name);

        if (this.params.useTooltips) {
            currentLabel.setToolTipText("Current");
            averageLabel.setToolTipText("Plot average");
            maxLabel.setToolTipText("Plot peak");
        }

        Action labelUpdate = new ActionChain()
                .append(BangUtils.setOption(helper1.getName(), "String", this.getFormatPercent().formulaFormatNumber(new Formula(current, Formula.MeasureParameters.NUMBER_VALUE), this.params.precision).toString(), null))
                .append(BangUtils.setOption(helper2.getName(), "String", this.getFormatPercent().formulaFormatNumber(plotManager.formulaAverage(), this.params.precision).toString(), null))
                .append(BangUtils.setOption(helper3.getName(), "String", this.getFormatPercent().formulaFormatNumber(plotManager.formulaMax(), this.params.precision).toString(), null));

        plotManager.addInitialAction(labelUpdate);
        plotManager.addUpdateAction(labelUpdate);

        Histogram histogram = new Histogram("currentHistogram")
                .setMeasure(current)
                .setColor(shadowColor)
                .setGraphStart(StartPlace.LEFT)
                .setAntiAlias(true);
        this.addMeter(histogram);
        this.setMeterBounds(histogram, histoBounds);

        Line line = this.addLine(current, this.makeWeakLine(lineColor), histoBounds);

        if (magnification == MagnificationMode.ALWAYS || magnification == MagnificationMode.AUTO_HIDE) {
            Calc dynamicCurrent = new Calc("corrected");
            this.skin.add(this.params.measureQueue, dynamicCurrent);
            dynamicCurrent.setDynamicVariables(true).setFormula(new Formula(current)).setMaxValue(new Formula(plotManager, Formula.MeasureParameters.NUMBER_VALUE));

            histogram.setMeasure2(dynamicCurrent);
            histogram.setColors(Color.WHITE, this.makeWeakShadow(shadowColor), shadowColor);

            Line line2 = this.addLine(dynamicCurrent, this.makeWeakLine(lineColor), histoBounds);

            if (magnification == MagnificationMode.AUTO_HIDE) {
                Calc hideManager = new Calc();
                this.addMeasure(hideManager);
                hideManager.addCondition(new Formula(plotManager).moreOrEqual(new Formula(current, Formula.MeasureParameters.MAX).multiply(0.4)),
                        new ActionChain(BangUtils.setOption(histogram.getName(), "SecondaryColor", SkinUtils.print(new Color(0, true)), null),
                                BangUtils.setOption(line2.getName(), "LineColor", SkinUtils.print(new Color(0, true)), null)),
                        new ActionChain(BangUtils.setOption(histogram.getName(), "SecondaryColor", SkinUtils.print(this.makeWeakShadow(shadowColor)), null),
                                BangUtils.setOption(line2.getName(), "LineColor", SkinUtils.print(this.makeWeakLine(lineColor)), null))
                );
            }
        }

        this.addHistoGrid(histoBounds, this.params.bigHistoHorizontalLines, this.params.histoVerticalLines);

        this.mask.add(ShapeRectangle.sharpOutline(plotBounds.x, plotBounds.y, plotBounds.w, plotBounds.h, this.params.histoOutlineWidth, this.params.outlineColor));
    }

    protected void addMemoryHeader(Measure current, String name, Color solidColor) {
        IntegerBounds integerBounds = this.layout.nextLine();

        Label totalLabel = new Label("Total" + name);
        this.skin.add(this.params.meterQueue, totalLabel);

        totalLabel.setX(integerBounds.x + integerBounds.w / 2).setY(integerBounds.y)
                .setStringAlign(Label.Align.CENTER_TOP).setStyle(this.getLabelStyle());

        Action totalLabelUpdate = BangUtils.setOption(totalLabel.getName(), "Text", name + ": " + this.getFormatBinary().formulaFormatNumber(new Formula(current, Formula.MeasureParameters.MAX), this.params.precision), null);
        current.addInitialAction(totalLabelUpdate);
        current.addChangeAction(totalLabelUpdate);

        integerBounds = this.layout.nextLine();

        List<Label> labels = this.addSpreadHeaderLabels(2, integerBounds, 0.0, 1);

        MString helper1 = new MString("currentHelper");
        this.addMeasure(helper1);
        Label currentLabel = labels.get(0).setText(helper1);

        MString helper2 = new MString("freeHelper");
        this.addMeasure(helper2);
        Label freeLabel = labels.get(1).setText(helper2);

        if (this.params.useTooltips) {
            currentLabel.setToolTipText("Used");
            freeLabel.setToolTipText("Free");
        }

        Action labelUpdate = new ActionChain()
                .append(BangUtils.setOption(helper1.getName(), "String",
                        this.getFormatPercent().formulaFormatFormula(new Formula(current, Formula.MeasureParameters.PERCENTUAL), this.params.precision)
                                + " / "
                                + this.getFormatBinary().formulaFormatFormula(new Formula(current, Formula.MeasureParameters.NUMBER_VALUE), this.params.precision), null))
                .append(BangUtils.setOption(helper2.getName(), "String",
                        this.getFormatPercent().formulaFormatFormula(new Formula(100).subtract(new Formula(current, Formula.MeasureParameters.PERCENTUAL)), this.params.precision)
                                + " / "
                                + this.getFormatBinary().formulaFormatFormula(new Formula(current, Formula.MeasureParameters.MAX).subtract(new Formula(current)), this.params.precision), null));

        current.addInitialAction(labelUpdate);
        current.addUpdateAction(labelUpdate);

        IntegerBounds barBounds = this.layout.nextElement(this.params.barHeight);

        Bar bar = new Bar(name + "Bar");
        skin.add(this.params.meterQueue, bar);

        bar.setMeasure(current).setSolidColor(this.params.plotBackColor)
                .setBarColor(solidColor).setBarOrientation(Orientation.HORIZONTAL);
        this.setMeterBounds(bar, barBounds.excludeBorder(this.params.barOutlineWidth));

        ShapeRectangle outline = ShapeRectangle.sharpOutline(barBounds.x, barBounds.y, barBounds.w, barBounds.h, this.params.barOutlineWidth, this.params.outlineColor);
        this.mask.add(outline);
    }

    protected List<Label> addSpreadHeaderLabels(int count, IntegerBounds bounds, double shift, double centerGravity) {
        ArrayList<Label> list = new ArrayList<>();

        Label firstLabel = new Label("spreadLabels0");
        skin.add(this.params.meterQueue, firstLabel);
        list.add(firstLabel);

        firstLabel.setX(bounds.x + this.params.textPadding).setY(bounds.y)
                .setStringAlign(Label.Align.LEFT_TOP).setStyle(this.getLabelStyle());

        for (int i = 1; i < count - 1; i++) {
            Label label = new Label("spreadLabels" + i);
            skin.add(this.params.meterQueue, label);
            list.add(label);

            double widthRatio = (1.0 / (count - 1) * i - 0.5) * centerGravity + 0.5 + shift;
            label.setX((int) Math.round(bounds.x + bounds.w * widthRatio)).setY(bounds.y)
                    .setStringAlign(Label.Align.CENTER_TOP).setStyle(this.getLabelStyle());
        }

        Label lastLabel = new Label("spreadLabels" + (count - 1));
        skin.add(this.params.meterQueue, lastLabel);
        list.add(lastLabel);

        lastLabel.setX(bounds.x + bounds.w - this.params.textPadding).setY(bounds.y)
                .setStringAlign(Label.Align.RIGHT_TOP).setStyle(this.getLabelStyle());

        return list;
    }

    /**
     * @return bounds of the plot
     */
    protected IntegerBounds addPlot(Measure current, IntegerBounds bounds, Color color) {
        return this.addPlot(current, bounds, color, null, 0, 0, null);
    }

    protected IntegerBounds addPlot(Measure current, IntegerBounds bounds, Color color, Meter base, int baseX, int baseY, List<String> groups) {
        return this.addPlot(current, bounds, color, this.makeShadow(color), base, baseX, baseY, groups);
    }

    protected IntegerBounds addPlot(Measure current, IntegerBounds bounds, Color lineColor, Color shadowColor, Meter base, int baseX, int baseY, List<String> groups) {
        IntegerBounds histoBounds = bounds.excludeBorder(this.params.histoOutlineWidth, this.params.histoOutlineWidth, this.params.histoOutlineWidth, this.params.histoOutlineWidth);
        this.plotPlaces.add(histoBounds);

        Histogram histogram = new Histogram("currentHistogram");
        this.addMeter(histogram);
        histogram.setMeasure(current)
                .setColor(shadowColor)
                .setGraphStart(StartPlace.LEFT);

        Line line = this.addLine(current, lineColor, histoBounds);

        if (base != null) {
            int dx = histoBounds.x - baseX;
            int dy = histoBounds.y - baseY;

            histogram
                    .setXRelative(base, dx, false)
                    .setYRelative(base, dy, false)
                    .setW(histoBounds.w)
                    .setH(histoBounds.h);
            line
                    .setXRelative(base, dx, false)
                    .setYRelative(base, dy, false);
        } else {
            this.setMeterBounds(histogram, histoBounds);
        }

        if (groups != null) {
            histogram.addGroups(groups);
            line.addGroups(groups);
        }

        this.addRectangleOutline(bounds);

        return histoBounds;
    }

    protected Line addLine(Measure measure, Color color, IntegerBounds plotBounds) {
        Line line = new Line();
        this.addMeter(line);
        line.addLine(measure, color)
                .setGraphStart(StartPlace.LEFT)
                .setAntiAlias(true)
                .setX(plotBounds.x)
                .setY(plotBounds.y)
                .setW(plotBounds.w + 1)
                .setH(plotBounds.h + 1);
        return line;
    }

    protected String createGroup() {
        return "group" + this.uid.getAndIncrement();
    }

    public enum MagnificationMode {
        NONE,
        AUTO_HIDE,
        ALWAYS;
    }
}
