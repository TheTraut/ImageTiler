import java.awt.*;
import java.awt.print.*;

public class TilePrinter {
    public static void printTiledImage(Image image, float scale) {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(new Printable() {
            @Override
            public int print(Graphics g, PageFormat pageFormat, int pageIndex) throws PrinterException {
                Graphics2D g2d = (Graphics2D) g;
                g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
                double pageWidth = pageFormat.getImageableWidth();
                double pageHeight = pageFormat.getImageableHeight();
                int scaledWidth = (int) (image.getWidth(null) * scale);
                int scaledHeight = (int) (image.getHeight(null) * scale);

                int tilesWide = (int) Math.ceil((double) scaledWidth / pageWidth);
                int tilesHigh = (int) Math.ceil((double) scaledHeight / pageHeight);

                if (pageIndex >= tilesWide * tilesHigh) {
                    return NO_SUCH_PAGE;
                }

                int row = pageIndex / tilesWide;
                int col = pageIndex % tilesWide;

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
}
