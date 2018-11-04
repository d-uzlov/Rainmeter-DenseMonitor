package rxtd.flexmon.templates;

import rxtd.rainmeter.Skin;
import rxtd.rainmeter.elements.measures.Calc;
import rxtd.rainmeter.elements.measures.Measure;
import rxtd.rainmeter.elements.measures.Memory;
import rxtd.rainmeter.elements.measures.plugins.custom.PerfMonRXTD;
import rxtd.rainmeter.elements.meters.Histogram;
import rxtd.rainmeter.elements.meters.Line;
import rxtd.rainmeter.elements.meters.StartPlace;
import rxtd.rainmeter.elements.meters.shape.shapetypes.ShapeLine;
import rxtd.rainmeter.elements.meters.shape.shapetypes.ShapeRectangle;
import rxtd.rainmeter.elements.meters.shape.shapetypes.modifiers.attribute.Stroke;
import rxtd.rainmeter.elements.meters.shape.shapetypes.modifiers.attribute.StrokeWidth;
import rxtd.rainmeter.formulas.Formula;

import java.awt.Color;

public class MemoryWidget extends Widget {
    private final Color mainLineColor = new Color(0, 200, 255);
    private final Color mainShadowColor = new Color(mainLineColor.getRGB() & 0xFFFFFF | params.shadowTranslucency << 24, true);

    private final Color secondLineColor = new Color(250, 205, 22);
    private final Color secondShadowColor = new Color(114, 74, 54, params.shadowTranslucency);

    private final Color swapLineColor = new Color(152, 51, 0);
    private final Color swapShadowColor = new Color(swapLineColor.getRGB() & 0xFFFFFF | params.shadowTranslucency << 24, true);

    private final int gapBeforeMainHisto = 2;

    public MemoryWidget() {
        super("Memory");
    }

    private void addMainHisto(Measure first, Measure second, Formula barrierRatio) {
        IntegerBounds bounds = layout.nextElement(this.params.bigHistoHeight + this.params.histoOutlineWidth * 2);
        IntegerBounds integerBounds = bounds.excludeBorder(this.params.histoOutlineWidth, this.params.histoOutlineWidth, 0, 0);
        this.plotPlaces.add(integerBounds);

        Histogram histogram = new Histogram("mainHisto");
        skin.add(this.params.meterQueue, histogram);
        histogram.setMeasure(first);
        histogram.setMeasure2(second);
        histogram.setColors(this.mainLineColor, this.secondShadowColor, this.mainShadowColor)
                .setGraphStart(StartPlace.LEFT).setAntiAlias(true);
        this.setMeterBounds(histogram, integerBounds);

        Line line = new Line("mainLine");
        skin.add(this.params.meterQueue, line);
        line.setGraphStart(StartPlace.LEFT).setAntiAlias(true)
                .addLine(first, this.mainLineColor)
                .addLine(second, this.secondLineColor);
        this.setMeterBounds(line, integerBounds);

        this.addHistoGrid(integerBounds, this.params.bigHistoHorizontalLines, this.params.histoVerticalLines);

        if (barrierRatio != null) {
            Formula barrierY = Formula.BuiltIn.round(barrierRatio.multiply(integerBounds.h)).add(integerBounds.y + this.params.histoBarrierWidth * 0.5);
            ShapeLine barrier = new ShapeLine(new Formula(integerBounds.x), barrierY, new Formula(integerBounds.x + integerBounds.w), barrierY);
            barrier.addModifier(new StrokeWidth(this.params.histoBarrierWidth)).addModifier(new Stroke(this.params.histoBarrierColor));
            this.mask.add(barrier);
        }

        ShapeRectangle outline = ShapeRectangle.sharpOutline(bounds.x, bounds.y, bounds.w, bounds.h, this.params.histoOutlineWidth, this.params.outlineColor);
        this.mask.add(outline);
    }

    public Skin construct() {
        Memory ramCurrent = new Memory("RamCurrent", Memory.Type.RAM).setAverageSize(this.params.averageFactor);
        this.skin.add(this.params.measureQueue, ramCurrent);
        this.addMemoryHeader(ramCurrent, "RAM", this.mainLineColor);
        this.addDerivative(ramCurrent, this.mainLineColor, true);

        Memory commitCurrent = new Memory("CommitCurrent", Memory.Type.COMMIT).setAverageSize(this.params.averageFactor);
        this.skin.add(this.params.measureQueue, commitCurrent);
        this.addMemoryHeader(commitCurrent, "Commit", this.secondLineColor);
        this.addDerivative(commitCurrent, this.secondLineColor, true);

        PerfMonRXTD parent = new PerfMonRXTD("perfmonPdhParent")
                .setSyncRawFormatted(false)
                .setCategory("Paging File")
                .setCounterList("% Usage")
                .setWhitelist("_Total");
        this.addMeasure(parent);


        PerfMonRXTD.Child swap = parent.createChild("PagesCurrent")
                .setType(PerfMonRXTD.ChildType.GET_RAW_COUNTER).setCounterIndex(0).setInstanceIndex(0).setAverageSize(this.params.averageFactor);
        this.addMeasure(swap);

        Calc swapCurrent = new Calc("SwapCurrent");
        this.skin.add(this.params.measureQueue, swapCurrent);
        swapCurrent.setFormula(new Formula(swap, Formula.MeasureParameters.NUMBER_VALUE).multiply(4096));
        swapCurrent.setMaxValue(new Formula(commitCurrent, Formula.MeasureParameters.MAX)
                .subtract(new Formula(ramCurrent, Formula.MeasureParameters.MAX)));

        this.addMemoryHeader(swapCurrent, "Swap", this.swapLineColor);
        this.addDerivative(swapCurrent, this.swapLineColor, true);

        this.layout.nextElement(this.gapBeforeMainHisto);

        Calc relativeRam = new Calc("RamRelative");
        this.skin.add(this.params.measureQueue, relativeRam);
        relativeRam.setFormula(new Formula(ramCurrent, Formula.MeasureParameters.NUMBER_VALUE));
        relativeRam.setMaxValue(new Formula(commitCurrent, Formula.MeasureParameters.MAX));
        Formula barrierRatio = new Formula(1).subtract(new Formula(ramCurrent, Formula.MeasureParameters.MAX).divide(new Formula(commitCurrent, Formula.MeasureParameters.MAX)));
        this.addMainHisto(relativeRam, commitCurrent, barrierRatio);

        this.backgroundSolid();
        this.outlineSolid();
        this.gridSolid();

        return skin;
    }
}
