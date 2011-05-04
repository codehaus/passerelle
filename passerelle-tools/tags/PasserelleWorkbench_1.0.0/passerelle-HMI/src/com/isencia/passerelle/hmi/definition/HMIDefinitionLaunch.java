/*
 * (c) Copyright 2004, iSencia Belgium NV
 * All Rights Reserved.
 * 
 * This software is the proprietary information of iSencia Belgium NV.  
 * Use is subject to license terms.
 */
package com.isencia.passerelle.hmi.definition;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;

public class HMIDefinitionLaunch {

    /**
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
    	
 
        //sequence_samba1
        FieldMapping m1FieldBundle = new FieldMapping();
        m1FieldBundle.addFieldMapping("rockingCurveField","rockingCurve");
        m1FieldBundle.addFieldMapping("scanNameColumn","StringsGenerator.Strings List (separated by commas)");
        /*
        m1FieldBundle.addFieldMapping("loopScansUseValuesList","Loop Scans.Use Values List (not selected-> use Start Value n Step)");
        */
        Model m1 = new Model(new URL("file://sequence_samba1.moml"),m1FieldBundle);
        
        //sequence_samba2
        FieldMapping m2FieldBundle = new FieldMapping();
        m2FieldBundle.addFieldMapping("rockingCurveField","rockingCurve");
        m2FieldBundle.addFieldMapping("scanNameColumn","StringsGenerator.Strings List (separated by commas)");
        m2FieldBundle.addFieldMapping("temperatureColumn","Loop Temperatures.Values List (separated by commas)");
        
        /*
        m2FieldBundle.addFieldMapping("loopTemperaturesUseValuesList","Loop Temperatures.Use Values List (not selected-> use Start Value n Step)");
        m2FieldBundle.addFieldMapping("setTemperatureWaitRead","SetTemperature.Wait read part equals write part");
        m2FieldBundle.addFieldMapping("loopScansUseValuesList","Loop Scans.Use Values List (not selected-> use Start Value n Step)");
        */
        m2FieldBundle.addFieldMapping("temperatureToleranceField","SetTemperature.Tolerance");
        m2FieldBundle.addFieldMapping("temperatureTimeoutField","SetTemperature.Timeout");
        
        m2FieldBundle.addFieldMapping("temperatureAttributeName","SetTemperature.Attribute Name");
       
        Model m2 = new Model(new URL("file://sequence_samba2.moml"),m2FieldBundle);
        
        
        //sequence_samba3
        FieldMapping m3FieldBundle = new FieldMapping();
        m3FieldBundle.addFieldMapping("rockingCurveField","rockingCurve");
        m3FieldBundle.addFieldMapping("scanPositionColumn","Loop Scans.Values List (separated by commas)");
        m3FieldBundle.addFieldMapping("scanNameColumn","StringsGenerator.Strings List (separated by commas)");
        
        /*
        m3FieldBundle.addFieldMapping("loopScansUseValuesList","Loop Scans.Use Values List (not selected-> use Start Value n Step)");
        m3FieldBundle.addFieldMapping("positionerWaitRead","Positioner.Wait read part equals write part");
        */
        m3FieldBundle.addFieldMapping("positionerAttributeName","Positioner.Attribute Name");
        
        Model m3 = new Model(new URL("file://sequence_samba3.moml"),m3FieldBundle);
        
        
        //sequence_samba4
        FieldMapping m4FieldBundle = new FieldMapping();
        m4FieldBundle.addFieldMapping("rockingCurveField","rockingCurve");
        m4FieldBundle.addFieldMapping("scanPositionColumn","Loop Scans.Values List (separated by commas)");
        m4FieldBundle.addFieldMapping("scanNameColumn","StringsGenerator.Strings List (separated by commas)");
        m4FieldBundle.addFieldMapping("temperatureColumn","Loop Temperatures.Values List (separated by commas)");
        /*
        m4FieldBundle.addFieldMapping("loopTemperaturesUseValuesList","Loop Temperatures.Use Values List (not selected-> use Start Value n Step)");
        m4FieldBundle.addFieldMapping("loopScansUseValuesList","Loop Scans.Use Values List (not selected-> use Start Value n Step)");
        m4FieldBundle.addFieldMapping("setTemperatureWaitRead","SetTemperature.Wait read part equals write part");
        m4FieldBundle.addFieldMapping("positionerWaitRead","Positioner.Wait read part equals write part");
        */
        
        m4FieldBundle.addFieldMapping("temperatureToleranceField","SetTemperature.Tolerance");
        m4FieldBundle.addFieldMapping("temperatureTimeoutField","SetTemperature.Timeout");
        
        m4FieldBundle.addFieldMapping("positionerAttributeName","Positioner.Attribute Name");
        m4FieldBundle.addFieldMapping("temperatureAttributeName","SetTemperature.Attribute Name");
        
        Model m4 = new Model(new URL("file://sequence_samba4.moml"),m4FieldBundle);
        
        //sequence_samba5
        FieldMapping m5FieldBundle = new FieldMapping();
        m5FieldBundle.addFieldMapping("rockingCurveField","rockingCurve");
        m5FieldBundle.addFieldMapping("scanNameColumn","StringsGenerator.Strings List (separated by commas)");
        m5FieldBundle.addFieldMapping("device","CommandInOut.Device Name");
        m5FieldBundle.addFieldMapping("command","CommandInOut.Command Name");
        /*
        m5FieldBundle.addFieldMapping("loopScansUseValuesList","Loop Scans.Use Values List (not selected-> use Start Value n Step)");
        */
        Model m5 = new Model(new URL("file://sequence_samba5.moml"),m5FieldBundle);
        
        //testCassiopee
        FieldMapping m6FieldBundle = new FieldMapping();
        m6FieldBundle.addFieldMapping("modeComboBox","ScientaRegion.Mode");
        m6FieldBundle.addFieldMapping("energyScaleComboBox","ScientaRegion.Energy Scale");
        //m6FieldBundle.addFieldMapping("lowEnergyTextField","ScientaRegion.Energy.Low Energy");
        //m6FieldBundle.addFieldMapping("highEnergyTextField","ScientaRegion.Energy.High Energy");
        m6FieldBundle.addFieldMapping("energyGroup","ScientaRegion.Energy");
        m6FieldBundle.addFieldMapping("energyStepTextField","ScientaRegion.Step.Energy Step");
        m6FieldBundle.addFieldMapping("stepTimeTextField","ScientaRegion.Step.Step Time");
        m6FieldBundle.addFieldMapping("lensModeComboBox","ScientaRegion.Lens Mode");
        m6FieldBundle.addFieldMapping("passEnergyComboBox","ScientaRegion.Pass Energy");
        m6FieldBundle.addFieldMapping("xMinTextField","ScientaRegion.Detector.X min");
        m6FieldBundle.addFieldMapping("xMaxTextField","ScientaRegion.Detector.X max");
        m6FieldBundle.addFieldMapping("yMinTextField","ScientaRegion.Detector.Y min");
        m6FieldBundle.addFieldMapping("yMaxTextField","ScientaRegion.Detector.Y max");
        m6FieldBundle.addFieldMapping("slicesTextField","ScientaRegion.Detector.Slices");
        m6FieldBundle.addFieldMapping("passModeComboBox","ScientaRegion.Pass Mode");
        
        Model m6 = new Model(new URL("file://testCassiopee.moml"),m6FieldBundle);
        
        
        ModelBundle mb = new ModelBundle();
        
        mb.addModel("testCassiopee", m6);
        mb.addModel("sequence_samba5", m5);
        mb.addModel("sequence_samba4", m4);
        mb.addModel("sequence_samba3", m3);
        mb.addModel("sequence_samba2", m2);
        mb.addModel("sequence_samba1", m1);
        
        
        
        String def =ModelBundle.generateDef(mb);
        
        Writer defWriter=null;
        try {
            defWriter = new FileWriter("src/hmi_def.xml");
            defWriter.write(def);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(defWriter!=null) try {defWriter.close();} catch (Exception e) {}
        }

    }

}
