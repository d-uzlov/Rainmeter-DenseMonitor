package rxtd.flexmon.templates;

import rxtd.rainmeter.Skin;
import rxtd.rainmeter.elements.measures.Calc;
import rxtd.rainmeter.elements.measures.Registry;
import rxtd.rainmeter.elements.measures.plugins.custom.MSIAfterburner;
import rxtd.rainmeter.formulas.Formula;

import java.awt.Color;

public class GPUWidget extends Widget {
    private final Color mainLineColor = this.params.brightPalette[10];
    private final Color mainShadowColor = this.makeShadow(this.mainLineColor);

    private final Color vramLineColor = this.params.brightPalette[9];

    public GPUWidget() {
        super("GPU");
    }

    public Skin construct() {
        MSIAfterburner current = new MSIAfterburner("current").setGpuNumber(0).setSource(MSIAfterburner.Source.GPU_USAGE).setMaxValue(100.0).setAverageSize(this.params.averageFactor);
        this.skin.add(this.params.measureQueue, current);
        this.addCpuModule(current, this.mainLineColor, this.mainShadowColor, "GPU", MagnificationMode.AUTO_HIDE);

        Registry videoPath = new Registry("registryVideoPath");
        this.skin.add(this.params.measureQueue, videoPath);
        videoPath.setRegHKey(Registry.HKeys.LOCAL_MACHINE);
        videoPath.setRegKey("HARDWARE\\DEVICEMAP\\VIDEO");
        videoPath.setRegValue("\\Device\\Video0");
        videoPath.addSubstitute("\\Registry\\Machine\\", "");
        videoPath.addSubstitute("System", "SYSTEM");

        Registry totalVram = new Registry("registryTotalVram");
        this.skin.add(this.params.measureQueue, totalVram);
        totalVram.setRegHKey(Registry.HKeys.LOCAL_MACHINE);
        totalVram.setRegKey(new Formula(videoPath));
        totalVram.setRegValue("HardwareInformation.qwMemorySize");
        totalVram.setDynamicVariables(true);

        MSIAfterburner currentVram = new MSIAfterburner("currentVramPercent").setGpuNumber(0).setSource(MSIAfterburner.Source.MEMORY_USAGE);
        this.skin.add(this.params.measureQueue, currentVram);

        Calc currenrVramBytes = new Calc("currentVramBytes").setFormula(new Formula(currentVram).multiply(1024 * 1024)).setMaxValue(new Formula(totalVram)).setDynamicVariables(true);
        this.skin.add(this.params.measureQueue, currenrVramBytes);
        this.addMemoryHeader(currenrVramBytes, "VRAM", this.vramLineColor);

        this.addDerivative(currenrVramBytes, this.vramLineColor, true);

        this.backgroundSolid();
        this.outlineSolid();
        this.gridSolid();

        return skin;
    }
}
