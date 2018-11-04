package rxtd.flexmon.templates;

import rxtd.rainmeter.Skin;
import rxtd.rainmeter.SkinUtils;
import rxtd.rainmeter.Suite;
import rxtd.rainmeter.UniqueNamePrefixProvider;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        SkinUtils.setNamePrefixProviderFactory(() -> {
            var provider = new UniqueNamePrefixProvider();
            provider.setZeroPaddingSize(3);
            return provider;
        });

//        Widget.setMode("4.2");
        Widget.setMode("4.3");

        Suite suite = new Suite("test suite");
//        Suite suite = new Suite("Dense Monitor");
        suite.setFillMetadata(true);
        suite.setLicense("Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International");
        suite.setVersion("1.1");
        suite.setInformation("https://forum.rainmeter.net/viewtopic.php?f=27&t=29101");
        suite.setPrintUnmanagedResources(true);
        suite.setPrintManagedResources(true);

        Skin memory = new MemoryWidget().construct();
        suite.addSkin(memory, "Memory");

        Skin network = new NetworkWidget().construct();
        suite.addSkin(network, "Network");

        Skin physDrives = new PhysicalDrivesWidget().construct();
        suite.addSkin(physDrives, "Disks", "PhysicalTotal");

        Skin logicalDisks = new DisksWidget("Logical Disks").constructLogical(15);
        suite.addSkin(logicalDisks, "Disks", "Logical");

        Skin physicalDisks = new DisksWidget("Physical Disks").constructPhysical(15);
        suite.addSkin(physicalDisks, "Disks", "Physical");

        Skin cpu0 = new CPUWidget("CPU").construct(0, 1);
        suite.addSkin(cpu0, "CPU");
        for (int i = 0; i < 4; i++) {
            int cores = (i + 1) * 2;
            Skin cpu = new CPUWidget("CPU" + cores).construct(cores, 1);
            suite.addSkin(cpu, "CPU");
            Skin cpu2 = new CPUWidget("CPU" + cores + "x2").construct(cores * 2, 2);
            suite.addSkin(cpu2, "CPU");
        }

        Skin gpu = new GPUWidget().construct();
        suite.addSkin(gpu, "GPU");

        int processInstances = 7;

        Skin topCpu = new TopProseccesWidget().constructProcessor(processInstances);
        suite.addSkin(topCpu, "TopProcesses", "TopCPU");

        Skin topMemory = new TopProseccesWidget().constructMemory(processInstances);
        suite.addSkin(topMemory, "TopProcesses", "TopMemory");

        Skin topIO = new TopProseccesWidget().constructIO(processInstances);
        suite.addSkin(topIO, "TopProcesses", "TopIO");

        Skin timeUptime = new TimeWidget("TimeUptime").constructBoth();
        suite.addSkin(timeUptime, "Time");

        Skin time = new TimeWidget("Time").constructTime(1);
        suite.addSkin(time, "Time", "Time");

        Skin timeBig = new TimeWidget("TimeBig").constructTime(1.2);
        suite.addSkin(timeBig, "Time", "Time");

        Skin uptime = new TimeWidget("Uptime").constructUptime();
        suite.addSkin(uptime, "Time", "Uptime");

        Skin uptime2 = new TimeWidget("UptimeBig").constructUptime2();
        suite.addSkin(uptime2, "Time", "Uptime");

        suite.write();
    }
}
