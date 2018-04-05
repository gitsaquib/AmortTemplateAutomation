package nbcu.compass.amorttemplate.util;

import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Savepoint;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.openqa.selenium.Keys;
import org.sikuli.basics.Settings;
import org.sikuli.script.FindFailed;
import org.sikuli.script.Key;
import org.sikuli.script.Location;
import org.sikuli.script.Match;
import org.sikuli.script.Pattern;
import org.sikuli.script.Region;
import org.sikuli.script.Screen;
import org.testng.annotations.Test;

public class SikuliMain {
	
	static String iconPath = null;
	static Screen screen = null;
	static  {
		File directory = new File(".");
		String strBasepath;
		try {
			screen = new Screen();
			strBasepath = directory.getCanonicalPath();
			iconPath = strBasepath + File.separator + "images" + File.separator;
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			Settings.OcrTextRead = true;
			Settings.OcrTextSearch = true;
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testM() {
		
		try {
			/*
			Pattern schedulingTab = new Pattern(iconPath+"schedulingtab.png");
			screen.click(schedulingTab);
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			Match networkFound = screen.findText("KWHY");
			networkFound.click();
			Thread.sleep(AmortTemplateConstants.TENSECONDSWAITTIME);
			Pattern createSchedule = new Pattern(iconPath+"createschedule.png");
			screen.click(createSchedule);
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			Match nameFieldFound = screen.findText("Name");
			nameFieldFound.click();
			screen.type(Key.TAB);
			SimpleDateFormat df = new SimpleDateFormat("MMMM YYYY");
			String defaultScheduleName = df.format(new Date());
			for(int i=0; i<defaultScheduleName.length(); i++) {
				screen.type(Key.DELETE);		
			}
			df = new SimpleDateFormat("YYYYMMDDHHmmss");
			String scheduleName = "ScheduleName_" + df.format(new Date());
			screen.type(scheduleName);
			Thread.sleep(AmortTemplateConstants.TWOECONDSWAITTIME);
			clickSaveButton("");
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			Match openSchedule = screen.findText("Open Schedule");
			openSchedule.click();
			Thread.sleep(AmortTemplateConstants.THIRTYSECONDSWAITTIME);
			*/
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			Pattern nextWeek = new Pattern(iconPath+"nextweek.png");
			screen.click(nextWeek);
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			Location location = new Location (200, 200);
			screen.rightClick(location);
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			Pattern newIcon = new Pattern(iconPath+"newicon.png");
			screen.click(newIcon);
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			screen.rightClick();
			
		} catch (InterruptedException | FindFailed e) {
			e.printStackTrace();
		}
	}
	
	private void clickSaveButton(String statusMessage, int...retry) {
		screen = new Screen();
		try {
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			screen = new Screen();
			Pattern save = new Pattern(iconPath + "save.png");
			screen.click(save);
			Pattern saveDisabled = new Pattern(iconPath + "savedisabled.png");
			Match found = waitForElementToAppearByPattern(saveDisabled, 0);
			if(null == found) {
				if(null != retry && retry.length == 1) {
					;
				} else {
					clickSaveButton(statusMessage, 1);
				}
			}
		} catch (InterruptedException | FindFailed e) {
		}
	}
	
	private Match waitForElementToAppearByPattern(Pattern pattern, int retryCnt) {
		while(retryCnt < 3) {
			try {
				Thread.sleep(AmortTemplateConstants.TWENTYSECONDSWAITTIME);
				screen = new Screen();
				return screen.find(pattern);
			} catch (InterruptedException | FindFailed e) {
				retryCnt++;
			}
		}
		return null;
	}
}
