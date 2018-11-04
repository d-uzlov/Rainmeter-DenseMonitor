package rxtd.flexmon.templates;

import rxtd.rainmeter.Skin;
import rxtd.rainmeter.actions.Action;
import rxtd.rainmeter.actions.ActionChain;
import rxtd.rainmeter.actions.BangUtils;
import rxtd.rainmeter.elements.measures.Loop;
import rxtd.rainmeter.elements.measures.Time;
import rxtd.rainmeter.elements.measures.Uptime;
import rxtd.rainmeter.elements.meters.shape.Shape;
import rxtd.rainmeter.elements.meters.shape.shapetypes.ShapeLine;
import rxtd.rainmeter.elements.meters.shape.shapetypes.modifiers.attribute.Stroke;
import rxtd.rainmeter.elements.meters.shape.shapetypes.modifiers.attribute.StrokeWidth;
import rxtd.rainmeter.elements.meters.string.InlineSettings;
import rxtd.rainmeter.elements.meters.string.Label;
import rxtd.rainmeter.formulas.Formula;

import java.awt.Color;

public class TimeWidget extends Widget {
    private Loop loop;

    public TimeWidget(String name) {
        super(name);
    }

    private void addUpTime() {
        IntegerBounds bounds = this.layout.nextElement((int) Math.round(this.params.bigFontSize * 1.4));

        Label uptimeNameLabel = new Label("uptimeHeader");
        this.addMeter(uptimeNameLabel);
        uptimeNameLabel.setStyle(this.getLabelStyle()).setText("UpTime")
                .setStringAlign(Label.Align.RIGHT_TOP)
                .setX(bounds.x + bounds.w)
                .setY(bounds.y)
                .setFontSize(this.params.bigFontSize)
                .setFontFace(this.params.bigFontFamily);

        Uptime uptime = new Uptime("uptime");
        this.addMeasure(uptime);
        uptime.setFormat(new Uptime.FormatBuilder()
                .append(Uptime.FormatCode.DAYS, 2)
                .append(" ").append(Uptime.FormatCode.HOURS, 2)
                .append(" ").append(Uptime.FormatCode.MINUTES, 2)
                .append(" ").append(Uptime.FormatCode.SECONDS, 2));

        Label uptimeLabel = new Label("uptime");
        this.addMeter(uptimeLabel);
        uptimeLabel.setStyle(uptimeNameLabel).setText(new Label.TextBuilder().append(uptime))
                .setStringAlign(Label.Align.LEFT_TOP).setX(bounds.x).setY(bounds.y)
                .setFontSize(this.params.bigFontSize);

        Label colon1 = new Label("colon1");
        this.addMeter(colon1);
        colon1.setText(new Label.TextBuilder().append(this.loop))
                .setX(bounds.x + 28).setY(bounds.y + 2)
                .setStyle(uptimeLabel);

        Label colon2 = new Label("colon2")
                .setText(new Label.TextBuilder().append(this.loop))
                .setX(bounds.x + 62).setY(bounds.y + 2)
                .setStyle(uptimeLabel);
        this.addMeter(colon2);

        Label colon3 = new Label("colon3")
                .setText(new Label.TextBuilder().append(this.loop))
                .setX(bounds.x + 97).setY(bounds.y + 2)
                .setStyle(uptimeLabel);
        this.addMeter(colon3);
    }

    private void addUpTime2() {
        IntegerBounds bounds = this.layout.nextElement((int) Math.round(this.params.hugeFontSize * 1.4 - 7));

        Label uptimeNameLabel = new Label("uptimeHeader")
                .setStyle(this.getLabelStyle())
                .setText("UpTime")
                .setStringAlign(Label.Align.CENTER_TOP)
                .setX(bounds.x + bounds.w / 2)
                .setY(bounds.y - 10)
                .setFontSize(this.params.hugeFontSize)
                .setFontFace(this.params.bigFontFamily);
        this.addMeter(uptimeNameLabel);

        bounds = this.layout.nextElement((int) Math.round(this.params.hugeFontSize * 1.4 - 10));

        Uptime uptime = new Uptime("uptime")
                .setFormat(new Uptime.FormatBuilder()
                        .append(Uptime.FormatCode.DAYS, 2)
                        .append(":").append(Uptime.FormatCode.HOURS, 2)
                        .append(":").append(Uptime.FormatCode.MINUTES, 2)
                        .append(":").append(Uptime.FormatCode.SECONDS, 2));
        this.addMeasure(uptime);


        Label uptimeLabel = new Label("uptime")
                .setStyle(uptimeNameLabel)
                .setText(new Label.TextBuilder().append(uptime))
                .setStringAlign(Label.Align.CENTER_TOP)
                .setX(bounds.x + bounds.w / 2)
                .setY(bounds.y - 10)
                .setH(0)
                .setFontSize(this.params.hugeFontSize)
                .addInlineSetting(":", new InlineSettings.None());
        this.addMeter(uptimeLabel);

        Action redraw = new ActionChain()
                .append(BangUtils.updateMeter(uptimeLabel.getName(), null))
                .append(BangUtils.redraw(null));

        this.loop.addCondition(new Formula(this.loop, Formula.MeasureParameters.NUMBER_VALUE).notEqual(new Formula(0)),
                new ActionChain()
                        .append(BangUtils.setOption(uptimeLabel.getName(), "InlineSetting", new InlineSettings.Color(new Color(0, 0, 0, 0)).toString(), null))
                        .append(redraw),
                new ActionChain()
                        .append(BangUtils.setOption(uptimeLabel.getName(), "InlineSetting", new InlineSettings.Color(new Color(0, 0, 0, 255)).toString(), null))
                        .append(redraw)
        ).setDynamicVariables(true);
    }

    private void addDateTime(double scale) {

        IntegerBounds bounds = this.layout.nextElement((int) Math.round(this.params.bigFontSize * 1.4 * scale));

        Time localDate = new Time("date");
        this.addMeasure(localDate);
        localDate.setFormat(new Time.FormatBuilder().appendLocalDate());

        Label dateNameLabel = new Label("dateHeaderLabel")
                .setStyle(this.getLabelStyle())
                .setText("Date")
                .setX(bounds.x).setY(bounds.y)
                .setStringAlign(Label.Align.LEFT_TOP)
                .setFontSize((int) Math.round(this.params.bigFontSize * scale))
                .setFontFace(this.params.bigFontFamily);
        this.addMeter(dateNameLabel);

        Label dateLabel = new Label("dateLabel")
                .setStyle(dateNameLabel).setText(new Label.TextBuilder().append(localDate))
                .setX(bounds.x + bounds.w).setY(bounds.y)
                .setStringAlign(Label.Align.RIGHT_TOP);
        this.addMeter(dateLabel);

        bounds = this.layout.nextElement((int) Math.round(this.params.bigFontSize * 1.4 * scale));

        Time localTime = new Time("time")
                .setFormat(new Time.FormatBuilder()
                        .append(Time.FormatCode.HOUR_24)
                        .append(" ").append(Time.FormatCode.MINUTE)
                        .append(" ").append(Time.FormatCode.SECOND));
        this.addMeasure(localTime);

        Label timeNameLabel = new Label("timeHeaderLabel")
                .setStyle(this.getLabelStyle())
                .setText("Time")
                .setX(bounds.x).setY(bounds.y)
                .setStringAlign(Label.Align.LEFT_TOP)
                .setFontSize((int) Math.round(this.params.bigFontSize * scale))
                .setFontFace(this.params.bigFontFamily);
        this.addMeter(timeNameLabel);

        Label timeLabel = new Label("timeLabel");
        this.addMeter(timeLabel);
        timeLabel.setStyle(timeNameLabel).setText(new Label.TextBuilder().append(localTime))
                .setX(bounds.x + bounds.w).setY(bounds.y)
                .setStringAlign(Label.Align.RIGHT_TOP);

        Label colon1 = new Label("colon1");
        this.addMeter(colon1);
        colon1.setText(new Label.TextBuilder().append(this.loop))
                .setX((int) Math.round(bounds.x + bounds.w - 70 * scale)).setY(bounds.y - 1)
                .setStyle(timeNameLabel);

        Label colon2 = new Label("colon2");
        this.addMeter(colon2);
        colon2.setText(new Label.TextBuilder().append(this.loop))
                .setX((int) Math.round(bounds.x + bounds.w - 36 * scale)).setY(bounds.y - 1)
                .setStyle(timeNameLabel);

        this.layout.nextElement((int) Math.round(this.params.bigFontSize * 0.2));
    }

    private int addBreaker(Color color, int y) {
        IntegerBounds bounds = this.layout.getCurrentBounds();

        Shape shape = new Shape("breaker");
        this.skin.add(this.params.meterQueue, shape);
        ShapeLine line = new ShapeLine(bounds.x + 0.5, y + 0.5, bounds.x + bounds.w + 0.5, y + 0.5);
        line.addModifier(new StrokeWidth(1)).addModifier(new Stroke(color));
        shape.addShape(line);

        return bounds.y;
    }

    public Skin constructBoth() {
        this.skin.getRainmeterSection().setFPS(30.0);

        this.getLabelStyle().setFontWeight(400);
        this.loop = new Loop();
        this.skin.add(this.params.supportQueue, this.loop);
        this.loop.setMinValue(0.0).setMaxValue(1.0).setUpdateDivider(16)
                .addSubstitute("0", ":")
                .addSubstitute("1", "");

        this.addUpTime();
        IntegerBounds bounds = this.layout.getCurrentBounds();
        this.addBreaker(new Color(100, 100, 100, 150), bounds.y + bounds.h + 3);
        this.addDateTime(1);

        this.backgroundSolid();
        this.outlineSolid();
        this.gridSolid();

        return skin;
    }

    public Skin constructTime(double scale) {
        this.skin.getRainmeterSection().setFPS(30.0);

        this.getLabelStyle().setFontWeight(400);
        this.loop = new Loop();
        this.skin.add(this.params.supportQueue, this.loop);
        this.loop.setMinValue(0.0).setMaxValue(1.0).setUpdateDivider(16)
                .addSubstitute("0", ":")
                .addSubstitute("1", "");

        this.addDateTime(scale);

        this.backgroundSolid();
        this.outlineSolid();
        this.gridSolid();

        return skin;
    }

    public Skin constructUptime() {
        this.skin.getRainmeterSection().setFPS(30.0);

        this.getLabelStyle().setFontWeight(400);
        this.loop = new Loop();
        this.skin.add(this.params.supportQueue, this.loop);
        this.loop.setMinValue(0.0).setMaxValue(1.0).setUpdateDivider(16)
                .addSubstitute("0", ":")
                .addSubstitute("1", "");

        this.addUpTime();
        this.layout.nextElement(5);

        this.backgroundSolid();
        this.outlineSolid();
        this.gridSolid();

        return skin;
    }

    public Skin constructUptime2() {
        this.skin.getRainmeterSection().setFPS(30.0);

        this.getLabelStyle().setFontWeight(400);
        this.loop = new Loop();
        this.skin.add(this.params.supportQueue, this.loop);
        this.loop.setMinValue(0.0).setMaxValue(1.0).setUpdateDivider(16)
                .addSubstitute("0", ":")
                .addSubstitute("1", "");

        this.addUpTime2();

        this.backgroundSolid();
        this.outlineSolid();
        this.gridSolid();

        return skin;
    }
}
