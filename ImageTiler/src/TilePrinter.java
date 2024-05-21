import javax.print.attribute.PrintRequestAttributeSet;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.*;
import java.io.FileOutputStream;
import java.io.IOException;

public class TilePrinter {
    public static void printTiledImage(BufferedImage image, float scale, boolean isRotated) {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(new Printable() {
            @Override
            public int print(Graphics g, PageFormat pageFormat, int pageIndex) throws PrinterException {
                Graphics2D g2d = (Graphics2D) g;
                g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                double pageWidth = pageFormat.getImageableWidth();
                double pageHeight = pageFormat.getImageableHeight();
                int scaledWidth = (int) (image.getWidth() * scale);
                int scaledHeight = (int) (image.getHeight() * scale);

                TileCalculator.TilingResult tilingResult = TileCalculator.calculateOptimalTiling(scaledWidth, scaledHeight, pageWidth, pageHeight);

                if (pageIndex >= tilingResult.tilesWide * tilingResult.tilesHigh) {
                    return NO_SUCH_PAGE;
                }

                int row = pageIndex / tilingResult.tilesWide;
                int col = pageIndex % tilingResult.tilesWide;

                int x = -col * (int) pageWidth;
                int y = -row * (int) pageHeight;

                g2d.drawImage(image, x, y, scaledWidth, scaledHeight, null);

                return PAGE_EXISTS;
            }
        });

        boolean doPrint = job.printDialog();
        if (doPrint) {
            try {
                job.print();
            } catch (PrinterException e) {
                e.printStackTrace();
            }
        }
    }

    public static void saveTiledImageToPDF(BufferedImage image, float scale, boolean isRotated) {
        PrinterJob job = PrinterJob.getPrinterJob();
        PageFormat pageFormat = job.defaultPage();
        Paper paper = pageFormat.getPaper();
        paper.setSize(8.27 * 72, 11.69 * 72); // A4 size in points
        paper.setImageableArea(0, 0, 8.27 * 72, 11.69 * 72);
        pageFormat.setPaper(paper);

        job.setPrintable(new Printable() {
            @Override
            public int print(Graphics g, PageFormat pageFormat, int pageIndex) throws PrinterException {
                Graphics2D g2d = (Graphics2D) g;
                g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                double pageWidth = pageFormat.getImageableWidth();
                double pageHeight = pageFormat.getImageableHeight();
                int scaledWidth = (int) (image.getWidth() * scale);
                int scaledHeight = (int) (image.getHeight() * scale);

                TileCalculator.TilingResult tilingResult = TileCalculator.calculateOptimalTiling(scaledWidth, scaledHeight, pageWidth, pageHeight);

                if (pageIndex >= tilingResult.tilesWide * tilingResult.tilesHigh) {
                    return NO_SUCH_PAGE;
                }

                int row = pageIndex / tilingResult.tilesWide;
                int col = pageIndex % tilingResult.tilesWide;

                int x = -col * (int) pageWidth;
                int y = -row * (int) pageHeight;

                g2d.drawImage(image, x, y, scaledWidth, scaledHeight, null);

                return PAGE_EXISTS;
            }
        });

        try {
            FileOutputStream fos = new FileOutputStream("output.pdf");
            job.print((PrintRequestAttributeSet) fos);
            fos.close();
        } catch (IOException | PrinterException e) {
            e.printStackTrace();
        }
    }
}
