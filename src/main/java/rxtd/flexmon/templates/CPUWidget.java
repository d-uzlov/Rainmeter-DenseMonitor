package rxtd.flexmon.templates;

import rxtd.rainmeter.Skin;
import rxtd.rainmeter.actions.Action;
import rxtd.rainmeter.actions.ActionChain;
import rxtd.rainmeter.actions.BangUtils;
import rxtd.rainmeter.elements.measures.CPU;
import rxtd.rainmeter.elements.measures.Calc;
import rxtd.rainmeter.elements.measures.MString;
import rxtd.rainmeter.elements.measures.Measure;
import rxtd.rainmeter.elements.meters.Bar;
import rxtd.rainmeter.elements.meters.Image;
import rxtd.rainmeter.elements.meters.Orientation;
import rxtd.rainmeter.elements.meters.shape.shapetypes.ShapeRectangle;
import rxtd.rainmeter.elements.meters.string.Label;
import rxtd.rainmeter.formulas.Formula;
import rxtd.rainmeter.resources.ResourceFactory;

import java.awt.Color;

public class CPUWidget extends Widget {
    private final Color mainLineColor = this.makeSaturatedColor(this.params.brightPalette[0]);
    private final Color mainShadowColor = this.makeShadow(this.mainLineColor);

    public CPUWidget(String name) {
        super(name);
    }

    /**
     * @return plot bounds
     */
    protected IntegerBounds addTinyPlot(Measure current, Color color) {
        int rightPadding = 35;

        IntegerBounds wholeBounds = layout.nextElement(this.params.tinyHistoHeight + this.params.histoOutlineWidth * 2);
        IntegerBounds gridBounds = wholeBounds.excludeBorder(this.params.histoOutlineWidth, this.params.histoOutlineWidth, this.params.histoOutlineWidth, this.params.histoOutlineWidth);

        IntegerBounds bounds = wholeBounds.excludeBorder(0, rightPadding, 0, 0);
        IntegerBounds plotBounds = this.addPlot(current, bounds, color, null, 0, 0, null);

        this.addHistoGrid(gridBounds, this.params.tinyHistoHorizontalLines, this.params.histoVerticalLines, 0, gridBounds.w - rightPadding);

        MString helper = new MString("currentHelper");
        this.addMeasure(helper);
        Label currentLabel = new Label("currentLabel")
                .setText(helper);
        this.addMeter(currentLabel);

        currentLabel.setX(wholeBounds.x + wholeBounds.w - this.params.textPadding).setY(wholeBounds.y)
                .setStringAlign(Label.Align.RIGHT_TOP).setStyle(this.getLabelStyle());

        Action labelUpdate = new ActionChain()
                .append(BangUtils.setOption(helper.getName(), "String", this.getFormatPercent().formulaFormatNumber(new Formula(current, Formula.MeasureParameters.NUMBER_VALUE), this.params.smallPrecision).toString(), null));

        current.addInitialAction(labelUpdate);
        current.addChangeAction(labelUpdate);

        Bar bar = new Bar("currentBar")
                .setX(gridBounds.x + gridBounds.w - rightPadding)
                .setY(gridBounds.y + gridBounds.h - this.params.tinyBarHeight)
                .setW(rightPadding)
                .setH(this.params.tinyBarHeight)
                .setMeasure(current)
                .setBarOrientation(Orientation.HORIZONTAL)
                .setBarColor(color)
                .setSolidColor(this.params.plotBackColor);
        this.addMeter(bar);

        ShapeRectangle outline = ShapeRectangle.sharpOutline(wholeBounds.x, wholeBounds.y, wholeBounds.w, wholeBounds.h, this.params.histoOutlineWidth, this.params.outlineColor);
        this.mask.add(outline);

        return plotBounds;
    }

    private void addCores(int[] cores, int blockNumber, Label blockNumberStyle, Color color) {
        int leftOffset = 3;
        int rightOffset = 5;

        CPU vCore1 = new CPU("currentCore" + cores[0]).setProseccor(cores[0]).setAverageSize(this.params.averageFactor);
        this.skin.add(this.params.measureQueue, vCore1);
        IntegerBounds firstPlotBounds = this.addTinyPlot(vCore1, color);

        IntegerBounds lastPlotBounds;
        if (cores.length > 1) {
            for (int i = 1; i < cores.length - 1; i++) {
                CPU vCore = new CPU("currentCore" + cores[i]).setProseccor(cores[i]).setAverageSize(this.params.averageFactor);
                this.skin.add(this.params.measureQueue, vCore);
                this.addTinyPlot(vCore, color);
            }

            CPU vCoreN = new CPU("currentCore" + cores[cores.length - 1]).setProseccor(cores[cores.length - 1]).setAverageSize(this.params.averageFactor);
            this.skin.add(this.params.measureQueue, vCoreN);
            lastPlotBounds = this.addTinyPlot(vCoreN, color);
        } else {
            lastPlotBounds = firstPlotBounds;
        }

        Label label = new Label("cores" + cores[0] + "_" + cores[cores.length - 1]);
        this.skin.add(this.params.shadowInfoQueue, label);
        label.setX(firstPlotBounds.x + firstPlotBounds.w - rightOffset)
                .setY(firstPlotBounds.y)
                .setH(0)
                .setText(Integer.toString(blockNumber)).setStyle(blockNumberStyle);

        Image back = new Image("cores" + cores[0] + "_" + cores[cores.length - 1] + "_Back");
        this.skin.add(this.params.shadowInfoBackQueue, back);
        back.setSolidColor(this.makeWeakShadow(color))
                .setX(new Formula(label, Formula.MeterParameters.X).subtract(new Formula(leftOffset)).toString())
                .setY(firstPlotBounds.y)
                .setW(new Formula(label, Formula.MeterParameters.W).add(leftOffset + rightOffset).toString())
                .setH(lastPlotBounds.y - firstPlotBounds.y + lastPlotBounds.h)
                .setDynamicVariables(true);
    }

    public Skin construct(int coreTally, int coreGrouping) {
        CPU currentCPU = new CPU("currentCpu").setAverageSize(this.params.averageFactor);
        this.skin.add(this.params.measureQueue, currentCPU);
        if (this.params.useUnixProcessorUsage) {
            Calc cpuPerCore = new Calc("currentCpuScaled");
            this.skin.add(this.params.measureQueue, cpuPerCore);
            cpuPerCore.setFormula(new Formula(currentCPU).multiply(new Formula("%NUMBER_OF_PROCESSORS%")));
            cpuPerCore.setMaxValue(new Formula(currentCPU, Formula.MeasureParameters.MAX).multiply(new Formula("%NUMBER_OF_PROCESSORS%")));
            this.addCpuModule(cpuPerCore, this.mainLineColor, this.mainShadowColor, "CPU", MagnificationMode.AUTO_HIDE);
        } else {
            this.addCpuModule(currentCPU, this.mainLineColor, this.mainShadowColor, "CPU", MagnificationMode.AUTO_HIDE);
        }

        int elementInterval = this.layout.getElementInterval();
        this.layout.setElementInterval(0);
        this.layout.nextElement(1);


        Label coreNumberStyle = new Label("coreNumbersStyle");
        this.skin.add(this.params.supportQueue, coreNumberStyle);
        coreNumberStyle
                .setFontColor(new Color(255, 255, 255, 150))
                .setFontFace(new ResourceFactory().jarFont("/rxtd/flexmon/fonts/Lato-Black.ttf", "Lato"))
                .setFontSize(this.params.fontSize * 14 * coreGrouping / 9).setFontWeight(800)
                .disableUpdate()
                .setStringAlign(Label.Align.RIGHT_TOP);

        for (int i = 0; i < coreTally / coreGrouping; i++) {
            int colorIndex = i + 1;
            Color color = this.makeSaturatedColor(this.params.brightPalette[colorIndex]);
            int[] cores = new int[coreGrouping];
            for (int j = 0; j < coreGrouping; j++) {
                cores[j] = i * coreGrouping + j;
            }
            this.addCores(cores, i + this.params.firstCoreNumber, coreNumberStyle, color);
        }

        this.layout.setElementInterval(elementInterval);

        this.backgroundSolid();
        this.outlineSolid();
        this.gridSolid();

        return skin;
    }

}
