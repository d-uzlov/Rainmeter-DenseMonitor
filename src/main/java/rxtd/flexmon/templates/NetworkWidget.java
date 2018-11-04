package rxtd.flexmon.templates;

import rxtd.Pair;
import rxtd.rainmeter.Skin;
import rxtd.rainmeter.actions.Action;
import rxtd.rainmeter.actions.ActionChain;
import rxtd.rainmeter.actions.BangUtils;
import rxtd.rainmeter.elements.measures.Net;
import rxtd.rainmeter.elements.measures.WebParser;
import rxtd.rainmeter.elements.measures.plugins.Ping;
import rxtd.rainmeter.elements.measures.plugins.SysInfo;
import rxtd.rainmeter.elements.measures.scripts.IpResolveManager;
import rxtd.rainmeter.elements.meters.string.Label;
import rxtd.rainmeter.formulas.Formula;

import java.awt.Color;
import java.util.ArrayList;

public class NetworkWidget extends Widget {
    private final Color upLineColor = new Color(255, 103, 0);
    private final Color upShadowColor = new Color(upLineColor.getRGB() & 0xFFFFFF | params.shadowTranslucency << 24, true);

    private final Color downLineColor = new Color(248, 229, 78);
    private final Color downShadowColor = new Color(downLineColor.getRGB() & 0xFFFFFF | params.shadowTranslucency << 24, true);

    private final String plainRegex = "^(\\d.*)";
    private final String[][] ipChecks = {
            // small html
            {"http://checkip.dyndns.org/", "(?siU)Current IP Address: (.*)<"},
            // plain with html commentary
            {"http://ip.changeip.com/ip.asp", "(?s)^(\\d.*)<"},
            // json
            {"https://httpbin.org/ip", "(?sU)\"origin\": \"(.*)\""},
            // html
            {"https://monip.org/", "(?siU)>IP : (.*)<"},
            // limited to 1k requests/24h
            {"https://ipinfo.io/ip", plainRegex},
            // unreliable?
            {"https://api.ipify.org/", plainRegex},

            // plain
            {"http://ipecho.net/plain", plainRegex},
            {"https://icanhazip.com/", plainRegex},
            {"https://wtfismyip.com/text", plainRegex},
            {"https://checkip.amazonaws.com/", plainRegex},
            {"http://plain-text-ip.com/", plainRegex},
            {"http://xur.io/ip", plainRegex},
            {"http://showmyip.ca/ip.php", plainRegex},
            {"https://da.gd/ip", plainRegex},
            {"https://ifconfig.co/ip", plainRegex},
            {"http://ip.haschek.at/", plainRegex},
            {"http://ident.me/", plainRegex},
            {"https://myexternalip.com/raw", plainRegex},
            {"https://bot.whatismyipaddress.com", plainRegex},
    };

    public NetworkWidget() {
        super("Network");
    }

    private void addIP() {
        IntegerBounds integerBounds = this.layout.nextLine();

        SysInfo localIp = new SysInfo()
                .setSysInfoType(SysInfo.InfoType.IP_ADDRESS)
                .setSysInfoData("Best")
                .addSubstitute("", "No network");
        this.addMeasure(localIp);

        Label localIPLabel = new Label("localIp")
                .setX(integerBounds.x + this.params.textPadding)
                .setY(integerBounds.y)
                .setStringAlign(Label.Align.LEFT_TOP)
                .setToolTipText("Local IP Address")
                .setToolTipHidden(!this.params.useTooltips)
                .setStyle(this.getLabelStyle())
                .setText(localIp);
        this.addMeter(localIPLabel);

        Label externalIPLabel = new Label("externalIp");
        this.addMeter(externalIPLabel);

        IpResolveManager ipResolveManager = new IpResolveManager("ipResolveManager");
        this.addMeasure(ipResolveManager);

        WebParser webParser = new WebParser("externalIpResolver");
        this.addMeasure(webParser);

        localIp.addChangeAction(webParser.bangUpdate());

        Action changeSite = new ActionChain()
                .append(ipResolveManager.bangNext())
                .append(BangUtils.setOption(webParser.getName(), "URL", ipResolveManager.formulaUrl().toString(), null))
                .append(BangUtils.setOption(webParser.getName(), "RegExp", ipResolveManager.formulaRegExp().toString(), null));
        ipResolveManager.addInitialAction(changeSite);
        Action handleError = new ActionChain()
//                .append(BangUtils.log("zxc", null))
                .append(ipResolveManager.bangError())
                .append(BangUtils.setOption(webParser.getName(), "URL", ipResolveManager.formulaUrl().toString(), null))
                .append(BangUtils.setOption(webParser.getName(), "RegExp", ipResolveManager.formulaRegExp().toString(), null))
                .append(webParser.bangUpdate());
        webParser.addOnRegExpErrorAction(handleError);

        ArrayList<Pair<String, String>> sourceList = new ArrayList<>();
        for (var v : this.ipChecks) {
            sourceList.add(new Pair<>(v[0], v[1].replace("\\", "\\\\").replace("'", "\\'")));
        }
        ipResolveManager.setSources(sourceList);

        Ping ping = new Ping("ping");
        this.addMeasure(ping);
        ping.setDestAddress("8.8.4.4").setTimeout(5000).setDisabled(true).setUpdateRate(10)
//                .setFinishAction(BangUtils.log(new Formula(ping).toString(), null))
                .setIfAbove(new Formula(29999), new ActionChain()
//                        .append(BangUtils.log("asd", null))
                        .append(BangUtils.disableMeasure(webParser.getName(), null)))
                .setIfBelow(new Formula(30000),
                        new ActionChain()
//                                .append(BangUtils.log("qwe", null))
                                .append(BangUtils.enableMeasure(webParser.getName(), null))
                                .append(webParser.bangUpdate())
                                .append(BangUtils.disableMeasure(ping.getName(), null))
                );
        webParser.addOnConnectErrorAction(new ActionChain()
//                .append(BangUtils.log("err", null))
                .append(BangUtils.disableMeasure(webParser.getName(), null))
                .append(BangUtils.enableMeasure(ping.getName(), null))
                .append(BangUtils.setOption(externalIPLabel.getName(), "Text", "No internet", null))
                .append(BangUtils.updateMeter(externalIPLabel.getName(), null))
                .append(BangUtils.redraw(null)));
        webParser.setFinishAction(new ActionChain()
//                .append(BangUtils.log("suc", null))
                .append(BangUtils.setOption(externalIPLabel.getName(), "Text", "%1", null))
                .append(BangUtils.updateMeter(externalIPLabel.getName(), null))
                .append(BangUtils.redraw(null))
                .append(ipResolveManager.bangSuccess()));

        WebParser child = webParser.createChild("externalIpResolverChild");
        this.addMeasure(child);
        child.setStringIndex(1)
                .addSubstitute("", "No internet");

        externalIPLabel.setX(integerBounds.x + integerBounds.w - this.params.textPadding).setY(integerBounds.y).setStringAlign(Label.Align.RIGHT_TOP)
                .setToolTipText("External IP Address").setStyle(this.getLabelStyle())
                .setText(child);

        child.addChangeAction(new ActionChain()
                .append(BangUtils.updateMeter(externalIPLabel.getName(), null))
                .append(BangUtils.redraw(null)));
    }

    public Skin construct() {
        Net up = new Net("currentUpload", Net.Type.NET_OUT).setAverageSize(this.params.averageFactor);
        this.skin.add(this.params.measureQueue, up);
        up.useBestInterface();
        Net upTotal = new Net("totalUpload", Net.Type.NET_OUT).setAverageSize(this.params.averageFactor);
        upTotal.setCumulative(true);
        upTotal.useBestInterface();
        this.skin.add(this.params.measureQueue, upTotal);
        this.addDynamicSpeedStatisticsModule(up, upTotal, "Up", this.upLineColor, this.upShadowColor);

        Net down = new Net("currentDownload", Net.Type.NET_IN).setAverageSize(this.params.averageFactor);
        this.skin.add(this.params.measureQueue, down);
        down.useBestInterface();
        Net downTotal = new Net("totalDownload", Net.Type.NET_IN).setAverageSize(this.params.averageFactor);
        downTotal.setCumulative(true);
        downTotal.useBestInterface();
        this.skin.add(this.params.measureQueue, downTotal);
        this.addDynamicSpeedStatisticsModule(down, downTotal, "Down", this.downLineColor, this.downShadowColor);

        this.addIP();

        this.backgroundSolid();
        this.outlineSolid();
        this.gridSolid();

        return skin;
    }
}
