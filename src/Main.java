import mappinganalyzer.rectWithScroll.RectWithScrollLevel;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import mappinganalyzer.findTemplateMatching;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.Pause;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.File;
import java.sql.Driver;
import java.time.Duration;
import java.util.Collections;

public class Main {
    public static void main(String[] args) {
        System.loadLibrary("opencv_java490");
        String url = "https://www.selenium.dev/documentation/webdriver/actions_api/wheel/";
        String templateFilePath = "src/mappinganalyzer/images/template-test-5.png";
        RemoteWebDriver chromeDriver = new ChromeDriver ();
        chromeDriver.get (url);
        try {
            RectWithScrollLevel rectWithScrollLevel = new findTemplateMatching ().findMatching (url, templateFilePath, chromeDriver);
            int scrollLevel = rectWithScrollLevel.getScrollLevel ();
            Rect rect = rectWithScrollLevel.getRect ();
            System.out.println (rect.x + " vv" + rect.y);
            int x_coor = rect.x + rect.width/2;
            int y_coor = rect.y + rect.height/2;
            System.out.println (x_coor + " " + y_coor);

            Actions actions = new Actions (chromeDriver);
            actions.scrollByAmount (0, scrollLevel).perform ();

            Thread.sleep (1000);

            PointerInput mouse = new PointerInput (PointerInput.Kind.MOUSE, "default mouse");
            Sequence clickAction = new Sequence (mouse, 0)
                    .addAction (mouse.createPointerMove (Duration.ZERO, PointerInput.Origin.viewport (), x_coor, y_coor))
                    .addAction(mouse.createPointerDown(PointerInput.MouseButton.LEFT.asArg())) // Nhấn chuột xuống
                    .addAction(new Pause (mouse, Duration.ofMillis(100))) // Tạm dừng 100 milliseconds
                    .addAction(mouse.createPointerUp(PointerInput.MouseButton.LEFT.asArg())) ;// Nhấc chuột lên
            chromeDriver.perform(Collections.singleton (clickAction));
        } catch (Exception e){
            System.out.println (e);
        }
    }
}