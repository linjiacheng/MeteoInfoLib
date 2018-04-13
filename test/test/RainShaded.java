package test;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.print.PrintException;

import org.meteoinfo.data.GridData;
import org.meteoinfo.data.StationData;
import org.meteoinfo.data.meteodata.DrawMeteoData;
import org.meteoinfo.data.meteodata.GridDataSetting;
import org.meteoinfo.data.meteodata.MeteoDataInfo;
import org.meteoinfo.geoprocess.analysis.InterpolationMethods;
import org.meteoinfo.geoprocess.analysis.InterpolationSetting;
import org.meteoinfo.layer.VectorLayer;
import org.meteoinfo.legend.LegendManage;
import org.meteoinfo.legend.LegendScheme;
import org.meteoinfo.map.MapView;
import org.meteoinfo.shape.ShapeTypes;

public class RainShaded {

	public static void main(String[] args) {

		try {
			String inputFilePath = "/Users/donkie/Documents/Projects/MeteoInfo/ConsoleMeteoInfo/bin/Debug/rain.txt";
			MeteoDataInfo meteoDataInfo = new MeteoDataInfo();
			meteoDataInfo.openLonLatData(inputFilePath);
			StationData stationData = meteoDataInfo.getStationData("Precipitation");

			GridDataSetting gridDataSetting = new GridDataSetting();
			gridDataSetting.dataExtent.minX = 122.8;
			gridDataSetting.dataExtent.maxX = 124;
			gridDataSetting.dataExtent.minY = 41.1;
			gridDataSetting.dataExtent.maxY = 42.3;
			gridDataSetting.xNum = 100;
			gridDataSetting.yNum = 100;
			InterpolationSetting interSetting = new InterpolationSetting();
			interSetting.setGridDataSetting(gridDataSetting);
			interSetting.setInterpolationMethod(InterpolationMethods.IDW_Radius);
			interSetting.setRadius(1.7);
			interSetting.setMinPointNum(12);

			GridData gridData = stationData.interpolateData(interSetting);
			
			double[] cValues = new double[] { 0.1, 1.0, 10.0, 25.0, 50.0, 100.0, 250.0 };
			Color[] colors = LegendManage.createRainBowColors(cValues.length + 1);

			LegendScheme shadedLS = LegendManage.createGraduatedLegendScheme(cValues, colors, ShapeTypes.Polygon, 0,
					1000, false, stationData.missingValue);
			VectorLayer shadedLayer = DrawMeteoData.createShadedLayer(gridData, shadedLS, "Shaded_EF", "pecp", true);

			MapView mapView = new MapView();
			mapView.setSize(gridData.getXNum() * 10, gridData.getYNum() * 10);

			mapView.setLockViewUpdate(true);
			mapView.projectLayers(meteoDataInfo.getProjectionInfo());
			mapView.addLayer(shadedLayer);
			mapView.zoomToExtent(shadedLayer.getExtent());
			mapView.setLockViewUpdate(false);
			mapView.paintLayers();

			mapView.exportToPicture("/Users/donkie/Documents/Projects/MeteoInfo/ConsoleMeteoInfo/bin/Debug/rain_java.png");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (PrintException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
