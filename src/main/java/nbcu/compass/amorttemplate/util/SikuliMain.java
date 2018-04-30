package nbcu.compass.amorttemplate.util;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.sikuli.basics.Settings;
import org.sikuli.script.FindFailed;
import org.sikuli.script.Key;
import org.sikuli.script.Location;
import org.sikuli.script.Match;
import org.sikuli.script.Pattern;
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
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			Pattern applyEpisodes = new Pattern(iconPath+"applyepisodes.png");
			screen.click(applyEpisodes);
			Thread.sleep(AmortTemplateConstants.TWENTYSECONDSWAITTIME);
			Pattern programType = new Pattern(iconPath + "programtype.png");
			screen.type(programType, "Series");
			Pattern episodeTitle = new Pattern(iconPath + "episodetitle.png");
			screen.type(episodeTitle, "EPS-1");
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			Pattern searchBtn = new Pattern(iconPath+"search.png");
			screen.click(searchBtn);
			Thread.sleep(AmortTemplateConstants.TENSECONDSWAITTIME);
			Pattern checkbox = new Pattern(iconPath+"checkbox.png");
			screen.click(checkbox);
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			screen.type(Key.TAB);
			screen.type(Key.TAB);
			screen.type(Key.TAB);
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			screen.type(Key.SPACE);
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			screen.type(Key.SPACE);
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			Pattern save = new Pattern(iconPath + "saveschedule.png");
			screen.click(save);
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			Pattern closeSchedule = new Pattern(iconPath + "closeschedule.png");
			screen.click(closeSchedule);
			Thread.sleep(AmortTemplateConstants.THIRTYSECONDSWAITTIME);
			Pattern monthly = new Pattern(iconPath + "monthly.png");
			screen.rightClick(monthly);
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			Pattern approve = new Pattern(iconPath + "approve.png");
			screen.click(approve);
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
			Pattern approvePopup = new Pattern(iconPath + "approvepopup.png");
			screen.click(approvePopup);
		} catch (InterruptedException | FindFailed e) {
			e.printStackTrace();
		}
	}
	
	private void clickSaveButton(String statusMessage, int...retry) {
		try {
			Thread.sleep(AmortTemplateConstants.FIVESECONDSWAITTIME);
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
