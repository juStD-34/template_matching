package mappinganalyzer;


import mappinganalyzer.rectWithScroll.RectWithScrollLevel;
import org.apache.commons.io.FileUtils;
import org.opencv.core.*;
import  org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.ArrayList;
public class findTemplateMatching {
//    private String url;
    private static final int bonus = 500;
    private static  final int matchMethod = Imgproc.TM_CCOEFF;

    public  File get_scroll_screen_shot(String url, int scroll_length, WebDriver chromeDriver) {
//        WebDriver chromeDriver = new ChromeDriver ();
//        chromeDriver.get(url);
        JavascriptExecutor js = (JavascriptExecutor) chromeDriver;
        js.executeScript("window.scrollTo(0, " + scroll_length + ")");

        //wait for web loading success
        try {
            Thread.sleep (1000);
        } catch (InterruptedException e) {
            throw new RuntimeException (e);
        }

        File screenshotFile = ((TakesScreenshot) chromeDriver).getScreenshotAs(OutputType.FILE);
        try {
            FileUtils.copyFile(screenshotFile, new File("image.png"));
            System.out.println("Screenshot saved successfully.");
        } catch (IOException e) {
            System.out.println("Error");
        }
//        chromeDriver.quit();
        return screenshotFile;
    }
    private static Mat resize(Mat image, int width, int height) {
        Mat resized = new Mat();
        Imgproc.resize(image, resized, new Size(width, height));
        return resized;
    }
    public Rect run(File imgFile, String templateFile,
                    int match_method) throws Exception {
        System.out.println("\nRunning Template Matching");
        String outFile = "src/mappinganalyzer/images/outImg.jpg";


        //Read Image
        byte[] imgData = Files.readAllBytes(imgFile.toPath());
        Mat img = Imgcodecs.imdecode(new MatOfByte (imgData), Imgcodecs.IMREAD_GRAYSCALE);
        Mat blur = new Mat();
        Imgproc.GaussianBlur(img, blur, new Size(3, 3), 0);
        Mat laplacian = new Mat();
        Imgproc.Laplacian(blur, laplacian, CvType.CV_64F);
        laplacian.convertTo(laplacian, CvType.CV_32F);

        // Read template Img
        Mat templ = Imgcodecs.imread(templateFile,  Imgcodecs.IMREAD_GRAYSCALE);
        Mat templateLaplacian = new Mat();
        Imgproc.Laplacian (templ, templateLaplacian, CvType.CV_64F);
        templateLaplacian.convertTo(templateLaplacian, CvType.CV_32F);

        // Find Template matching
        double tH = templ.rows();
        double tW = templ.cols();
        Mat found = new Mat();
        double startX = 0;
        double startY = 0;
        double endX = 0;
        double endY = 0;
        double generalMax = 0;
        double generalMin = 100000000;
        for (double scale = 0.5; scale <= 2.0; scale += 0.1){
            Mat resized = resize(laplacian, (int) (laplacian.cols() * scale), (int) (laplacian.rows() * scale));
            double r = laplacian.cols() / (double) resized.cols();

            if (resized.rows() < tH || resized.cols() < tW) {
                continue;
            }

            Mat result = new Mat();
            Imgproc.matchTemplate(resized, templateLaplacian, result, match_method);
            Core.MinMaxLocResult mmr = Core.minMaxLoc(result);

            // If we have found a new maximum correlation value, then update found

            if(match_method == Imgproc.TM_SQDIFF || match_method == Imgproc.TM_SQDIFF_NORMED){
                double minVal = mmr.minVal;
                Point minLoc = mmr.minLoc;

                if (minVal < generalMin) {
                    generalMin = minVal;
                    startX = minLoc.x *r;
                    startY = minLoc.y * r;
                    endX = (minLoc.x + tW) * r;
                    endY = (minLoc.y +tH) * r;
                }
            }
            else {
                double maxVal = mmr.maxVal;
                Point maxLoc = mmr.maxLoc;
                if (maxVal > generalMax) {
//                double[] maxValArray = new double[] { maxVal };
//                found.put(0, 0, maxValArray);
                    generalMax = maxVal;
                    startX = maxLoc.x * r;
                    startY = maxLoc.y * r;
                    endX = (maxLoc.x + tW) * r;
                    endY = (maxLoc.y + tH) * r;
                }
            }

        }
        Imgcodecs.imwrite (outFile, img);
        System.out.println (generalMin + "vvv" + generalMax);
        if (generalMax > 1600000 || generalMin < 100) {
            // Draw bounding box
            Point start = new Point (startX, startY);
            Point end = new Point ( endX, endY);
            Imgproc.rectangle (img, start, end, new Scalar (0, 255, 0));
            System.out.println (start + "VV" + end);
            Imgcodecs.imwrite (outFile, img);
            return new Rect ((int) startX, (int) startY, (int) (endX - startX - tW), (int) (endY - startY - 180));
        } else {
            System.out.println(" Cursor not detected");
            throw new NotFoundException ("Cursor not detected.");
        }
    }
    public RectWithScrollLevel findMatching(String url, String templateFilePath, WebDriver chromeDriver ) throws Exception {
        boolean cursorDetected = false;
        int scrollLevel = 0;
        while (true){
            try {
                System.out.println ("scroll" + scrollLevel);
                RectWithScrollLevel result = new RectWithScrollLevel(run(get_scroll_screen_shot(url, scrollLevel, chromeDriver), templateFilePath, matchMethod), scrollLevel);
                return result;
            }
            catch (Exception e) {
                if (e instanceof NotFoundException) {
                    scrollLevel += bonus;
                    continue;
                } else {
                    throw e; // Nếu không phải NotFoundException, ném lại Exception
                }
            }
        }
    }
}
