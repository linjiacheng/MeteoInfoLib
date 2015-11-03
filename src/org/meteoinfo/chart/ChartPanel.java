/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.SimpleDoc;
import javax.print.StreamPrintServiceFactory;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.event.EventListenerList;
import org.freehep.graphics2d.VectorGraphics;
import org.freehep.graphicsio.emf.EMFGraphics2D;
import org.freehep.graphicsio.pdf.PDFGraphics2D;
import org.freehep.graphicsio.ps.PSGraphics2D;
import org.meteoinfo.chart.plot.XY1DPlot;
import org.meteoinfo.chart.plot.XY2DPlot;
import org.meteoinfo.chart.plot.XYPlot;
import org.meteoinfo.global.Extent;
import org.meteoinfo.global.GenericFileFilter;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author yaqiang
 */
public class ChartPanel extends JPanel {

    // <editor-fold desc="Variables">
    private final EventListenerList listeners = new EventListenerList();
    private BufferedImage mapBitmap = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
    private Chart chart;
    private Dimension chartSize;
    private Point mouseDownPoint = new Point(0, 0);
    private Point mouseLastPos = new Point(0, 0);
    private boolean dragMode = false;
    private JPopupMenu popupMenu;
    private MouseMode mouseMode = MouseMode.ZOOM;
    private List<int[]> selectedPoints;
    private final double INCH_2_CM = 2.54;
    // </editor-fold>

    // <editor-fold desc="Constructor">
    /**
     * Constructor
     */
    public ChartPanel() {
        super();
        //this.setBackground(Color.white);
        this.setBackground(Color.lightGray);
        this.setSize(200, 200);
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                onComponentResized(e);
            }
        });
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onMouseClicked(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                onMousePressed(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                onMouseReleased(e);
            }
        });
        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                onMouseMoved(e);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                onMouseDragged(e);
            }
        });
        this.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                //onMouseWheelMoved(e);
            }
        });

        popupMenu = new JPopupMenu();
        JMenuItem undoZoom = new JMenuItem("Undo zoom");
        undoZoom.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onUndoZoomClick(e);
            }
        });
        popupMenu.add(undoZoom);
        popupMenu.addSeparator();

        JMenuItem saveFigure = new JMenuItem("Save figure");
        saveFigure.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onSaveFigureClick(e);
            }
        });
        popupMenu.add(saveFigure);

        this.chart = null;
    }

    /**
     * Constructor
     *
     * @param chart Chart
     */
    public ChartPanel(Chart chart) {
        this();
        this.chart = chart;
    }
    
    /**
     * Constructor
     * @param chart Chart
     * @param width Chart width
     * @param height Chart height
     */
    public ChartPanel(Chart chart, int width, int height){
        this(chart);
        this.chartSize = new Dimension(width, height);
    }

    // </editor-fold>
    // <editor-fold desc="Get set methods">
    /**
     * Get chart
     *
     * @return Chart
     */
    public Chart getChart() {
        return chart;
    }

    /**
     * Set chart
     *
     * @param value
     */
    public void setChart(Chart value) {
        chart = value;
    }

    /**
     * Get popup menu
     *
     * @return Popup menu
     */
    public JPopupMenu getPopupMenu() {
        return this.popupMenu;
    }

    /**
     * Get mouse mode
     *
     * @return Mouse mode
     */
    public MouseMode getMouseMode() {
        return this.mouseMode;
    }

    /**
     * Set mouse mode
     *
     * @param value Mouse mode
     */
    public void setMouseMode(MouseMode value) {
        this.mouseMode = value;
        switch (this.mouseMode) {
            case SELECT:
                this.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
                break;
            default:
                this.setCursor(Cursor.getDefaultCursor());
                break;
        }
    }

    /**
     * Get selected chart points
     *
     * @return Selected chart points
     */
    public List<int[]> getSelectedPoints() {
        return this.selectedPoints;
    }
    // </editor-fold>

    // <editor-fold desc="Events">
    public void addPointSelectedListener(IPointSelectedListener listener) {
        this.listeners.add(IPointSelectedListener.class, listener);
    }

    public void removePointSelectedListener(IPointSelectedListener listener) {
        this.listeners.remove(IPointSelectedListener.class, listener);
    }

    public void firePointSelectedEvent() {
        firePointSelectedEvent(new PointSelectedEvent(this));
    }

    private void firePointSelectedEvent(PointSelectedEvent event) {
        Object[] listeners = this.listeners.getListenerList();
        for (int i = 0; i < listeners.length; i = i + 2) {
            if (listeners[i] == IPointSelectedListener.class) {
                ((IPointSelectedListener) listeners[i + 1]).pointSelectedEvent(event);
            }
        }
    }
    // </editor-fold>

    // <editor-fold desc="Method">
    /**
     * Paint component
     *
     * @param g Graphics
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (this.getWidth() < 5 || this.getHeight() < 5) {
            return;
        }

        //this.setBackground(Color.white);
        Graphics2D g2 = (Graphics2D) g;
        g2.drawImage(mapBitmap, null, 0, 0);

        //Draw dynamic graphics
        if (this.dragMode) {
            int aWidth = Math.abs(mouseLastPos.x - mouseDownPoint.x);
            int aHeight = Math.abs(mouseLastPos.y - mouseDownPoint.y);
            int aX = Math.min(mouseLastPos.x, mouseDownPoint.x);
            int aY = Math.min(mouseLastPos.y, mouseDownPoint.y);
            g2.setColor(this.getForeground());
            float dash1[] = {2.0f};
            g2.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f));
            g2.draw(new Rectangle(aX, aY, aWidth, aHeight));
        }
    }

    /**
     * Paint graphics
     */
    public void paintGraphics() {
        if (this.getWidth() < 5 || this.getHeight() < 5) {
            return;
        }

        this.mapBitmap = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_INT_ARGB);

        if (this.chart != null) {
            Graphics2D g = this.mapBitmap.createGraphics();
            Rectangle2D chartArea;
            if (this.chartSize == null)
                chartArea = new Rectangle2D.Double(0.0, 0.0, this.mapBitmap.getWidth(), this.mapBitmap.getHeight());
            else
                chartArea = new Rectangle2D.Double(0.0, 0.0, this.chartSize.width, this.chartSize.height);
            this.chart.draw(g, chartArea);
        }
        this.repaint();
    }

    public void paintGraphics(Graphics2D g) {
        if (this.chart != null) {
            Rectangle2D chartArea;
            if (this.chartSize == null)
                chartArea = new Rectangle2D.Double(0.0, 0.0, this.mapBitmap.getWidth(), this.mapBitmap.getHeight());
            else
                chartArea = new Rectangle2D.Double(0.0, 0.0, this.chartSize.width, this.chartSize.height);
            this.chart.draw(g, chartArea);
        }
    }        

    void onComponentResized(ComponentEvent e) {
        if (this.chart != null) {
            this.paintGraphics();
        }
    }

    void onMousePressed(MouseEvent e) {
        mouseDownPoint.x = e.getX();
        mouseDownPoint.y = e.getY();
        mouseLastPos = (Point) mouseDownPoint.clone();
    }

    void onMouseMoved(MouseEvent e) {
        this.dragMode = false;
    }

    void onMouseReleased(MouseEvent e) {
        this.dragMode = false;
        switch (this.mouseMode) {
            case ZOOM:
                if (Math.abs(mouseLastPos.x - mouseDownPoint.x) > 5) {
                    XYPlot xyplot = (XYPlot) this.chart.findPlot(mouseDownPoint.x, mouseDownPoint.y);
                    if (xyplot == null)
                        return;
                    
                    if (xyplot instanceof XY1DPlot) {
                        XY1DPlot plot = (XY1DPlot) xyplot;
                        Rectangle2D graphArea = this.chart.getGraphArea();
                        if (graphArea.contains(mouseDownPoint.x, mouseDownPoint.y) || graphArea.contains(mouseLastPos.x, mouseLastPos.y)) {
                            double[] xy1 = plot.screenToProj(mouseDownPoint.x - graphArea.getX(), mouseDownPoint.y - graphArea.getY(), graphArea);
                            double[] xy2 = plot.screenToProj(mouseLastPos.x - graphArea.getX(), mouseLastPos.y - graphArea.getY(), graphArea);
                            Extent extent = new Extent();
                            extent.minX = Math.min(xy1[0], xy2[0]);
                            extent.maxX = Math.max(xy1[0], xy2[0]);
                            extent.minY = Math.min(xy1[1], xy2[1]);
                            extent.maxY = Math.max(xy1[1], xy2[1]);
                            if (plot.getXAxis().isInverse()) {
                                Extent drawExtent = plot.getDrawExtent();
                                double minx, maxx;
                                minx = drawExtent.getWidth() - (extent.maxX - drawExtent.minX) + drawExtent.minX;
                                maxx = drawExtent.getWidth() - (extent.minX - drawExtent.minX) + drawExtent.minX;
                                extent.minX = minx;
                                extent.maxX = maxx;
                            }
                            if (plot.getYAxis().isInverse()) {
                                Extent drawExtent = plot.getDrawExtent();
                                double miny, maxy;
                                miny = drawExtent.getHeight() - (extent.maxY - drawExtent.minY) + drawExtent.minY;
                                maxy = drawExtent.getHeight() - (extent.minY - drawExtent.minY) + drawExtent.minY;
                                extent.minY = miny;
                                extent.maxY = maxy;
                            }
                            plot.setDrawExtent(extent);
                            this.paintGraphics();
                        }
                    } else if (xyplot instanceof XY2DPlot) {
                        XY2DPlot plot = (XY2DPlot) xyplot;
                        Rectangle2D graphArea = this.chart.getGraphArea();
                        if (graphArea.contains(mouseDownPoint.x, mouseDownPoint.y) || graphArea.contains(mouseLastPos.x, mouseLastPos.y)) {
                            double[] xy1 = plot.screenToProj(mouseDownPoint.x - graphArea.getX(), mouseDownPoint.y - graphArea.getY(), graphArea);
                            double[] xy2 = plot.screenToProj(mouseLastPos.x - graphArea.getX(), mouseLastPos.y - graphArea.getY(), graphArea);
                            Extent extent = new Extent();
                            extent.minX = Math.min(xy1[0], xy2[0]);
                            extent.maxX = Math.max(xy1[0], xy2[0]);
                            extent.minY = Math.min(xy1[1], xy2[1]);
                            extent.maxY = Math.max(xy1[1], xy2[1]);
                            plot.setDrawExtent(extent);
                            this.paintGraphics();
                        }
                    }
                }
                break;
            case SELECT:
                if (Math.abs(mouseLastPos.x - mouseDownPoint.x) > 5) {
                    XYPlot xyplot = (XYPlot) this.chart.getPlots().get(0);
                    if (xyplot instanceof XY1DPlot) {
                        XY1DPlot plot = (XY1DPlot) xyplot;
                        Rectangle2D graphArea = this.chart.getGraphArea();
                        if (graphArea.contains(mouseDownPoint.x, mouseDownPoint.y) || graphArea.contains(mouseLastPos.x, mouseLastPos.y)) {
                            double[] xy1 = plot.screenToProj(mouseDownPoint.x - graphArea.getX(), mouseDownPoint.y - graphArea.getY(), graphArea);
                            double[] xy2 = plot.screenToProj(mouseLastPos.x - graphArea.getX(), mouseLastPos.y - graphArea.getY(), graphArea);
                            Extent extent = new Extent();
                            extent.minX = Math.min(xy1[0], xy2[0]);
                            extent.maxX = Math.max(xy1[0], xy2[0]);
                            extent.minY = Math.min(xy1[1], xy2[1]);
                            extent.maxY = Math.max(xy1[1], xy2[1]);
                            this.selectedPoints = plot.getDataset().selectPoints(extent);
                            this.firePointSelectedEvent();
                            this.paintGraphics();
                        }
                    }
                }
                break;
        }
    }

    void onMouseDragged(MouseEvent e) {
        this.dragMode = true;
        mouseLastPos.x = e.getX();
        mouseLastPos.y = e.getY();

        this.repaint();
    }

    void onMouseClicked(MouseEvent e) {
        int clickTimes = e.getClickCount();
        if (clickTimes == 1) {
            if (e.getButton() == MouseEvent.BUTTON3) {
                popupMenu.show(this, e.getX(), e.getY());
            }
        }
    }

    private void onUndoZoomClick(ActionEvent e) {
        XYPlot xyplot = (XYPlot) this.chart.getPlots().get(0);
        if (xyplot instanceof XY1DPlot) {
            XY1DPlot plot = (XY1DPlot) xyplot;
            plot.setAutoExtent();
        } else if (xyplot instanceof XY2DPlot) {
            XY2DPlot plot = (XY2DPlot) xyplot;
            plot.setDrawExtent(plot.getMapView().getLastAddedLayer().getExtent());
        }
        this.paintGraphics();
    }

    private void onSaveFigureClick(ActionEvent e) {
        String path = System.getProperty("user.dir");
        File pathDir = new File(path);
        JFileChooser aDlg = new JFileChooser();
        aDlg.setCurrentDirectory(pathDir);
        String[] fileExts = new String[]{"png"};
        GenericFileFilter pngFileFilter = new GenericFileFilter(fileExts, "Png Image (*.png)");
        aDlg.addChoosableFileFilter(pngFileFilter);
        fileExts = new String[]{"gif"};
        GenericFileFilter mapFileFilter = new GenericFileFilter(fileExts, "Gif Image (*.gif)");
        aDlg.addChoosableFileFilter(mapFileFilter);
        fileExts = new String[]{"jpg"};
        mapFileFilter = new GenericFileFilter(fileExts, "Jpeg Image (*.jpg)");
        aDlg.addChoosableFileFilter(mapFileFilter);
        fileExts = new String[]{"eps"};
        mapFileFilter = new GenericFileFilter(fileExts, "EPS file (*.eps)");
        aDlg.addChoosableFileFilter(mapFileFilter);
        fileExts = new String[]{"pdf"};
        mapFileFilter = new GenericFileFilter(fileExts, "PDF file (*.pdf)");
        aDlg.addChoosableFileFilter(mapFileFilter);
        fileExts = new String[]{"emf"};
        mapFileFilter = new GenericFileFilter(fileExts, "EMF file (*.emf)");
        aDlg.addChoosableFileFilter(mapFileFilter);
        aDlg.setFileFilter(pngFileFilter);
        aDlg.setAcceptAllFileFilterUsed(false);
        if (JFileChooser.APPROVE_OPTION == aDlg.showSaveDialog(this)) {
            File aFile = aDlg.getSelectedFile();
            System.setProperty("user.dir", aFile.getParent());
            String extent = ((GenericFileFilter) aDlg.getFileFilter()).getFileExtent();
            String fileName = aFile.getAbsolutePath();
            if (!fileName.substring(fileName.length() - extent.length()).equals(extent)) {
                fileName = fileName + "." + extent;
            }

            try {
                this.saveImage(fileName);
            } catch (PrintException ex) {
                Logger.getLogger(ChartPanel.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(ChartPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Save image to a picture file
     *
     * @param aFile File path
     * @throws java.io.FileNotFoundException
     * @throws javax.print.PrintException
     */
    public void saveImage(String aFile) throws FileNotFoundException, PrintException, IOException {
        if (aFile.endsWith(".ps")) {
            DocFlavor flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
            String mimeType = "application/postscript";
            StreamPrintServiceFactory[] factories = StreamPrintServiceFactory.lookupStreamPrintServiceFactories(flavor, mimeType);
            FileOutputStream out = new FileOutputStream(aFile);
            if (factories.length > 0) {
                PrintService service = factories[0].getPrintService(out);
                SimpleDoc doc = new SimpleDoc(new Printable() {
                    @Override
                    public int print(Graphics g, PageFormat pf, int page) {
                        if (page >= 1) {
                            return Printable.NO_SUCH_PAGE;
                        } else {
                            double sf1 = pf.getImageableWidth() / (getWidth() + 1);
                            double sf2 = pf.getImageableHeight() / (getHeight() + 1);
                            double s = Math.min(sf1, sf2);
                            Graphics2D g2 = (Graphics2D) g;
                            g2.translate((pf.getWidth() - pf.getImageableWidth()) / 2, (pf.getHeight() - pf.getImageableHeight()) / 2);
                            g2.scale(s, s);

                            paintGraphics(g2);
                            return Printable.PAGE_EXISTS;
                        }
                    }
                }, flavor, null);
                DocPrintJob job = service.createPrintJob();
                PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
                job.print(doc, attributes);
                out.close();
            }
        } else if (aFile.endsWith(".eps")) {
            int width = this.getWidth();
            int height = this.getHeight();
//            EPSGraphics2D g = new EPSGraphics2D(0.0, 0.0, width, height);
//            paintGraphics(g);
//            FileOutputStream file = new FileOutputStream(aFile);
//            try {
//                file.write(g.getBytes());
//            } finally {
//                file.close();
//                g.dispose();
//            }

            Properties p = new Properties();
            p.setProperty("PageSize", "A5");
            VectorGraphics g = new PSGraphics2D(new File(aFile), new Dimension(width, height));
            //g.setProperties(p);
            g.startExport();
            this.paintGraphics(g);
            g.endExport();
            g.dispose();
        } else if (aFile.endsWith(".pdf")) {
            int width = this.getWidth();
            int height = this.getHeight();
            VectorGraphics g = new PDFGraphics2D(new File(aFile), new Dimension(width, height));
            //g.setProperties(p);
            g.startExport();
            this.paintGraphics(g);
            g.endExport();
            g.dispose();
        } else if (aFile.endsWith(".emf")) {
            int width = this.getWidth();
            int height = this.getHeight();
            VectorGraphics g = new EMFGraphics2D(new File(aFile), new Dimension(width, height));
            //g.setProperties(p);
            g.startExport();
            this.paintGraphics(g);
            g.endExport();
            g.dispose();
        } else {
            //String extension = aFile.substring(aFile.lastIndexOf('.') + 1);
            //ImageIO.write(this.mapBitmap, extension, new File(aFile));
            
            String extension = aFile.substring(aFile.lastIndexOf('.') + 1);
            BufferedImage aImage;
            int w, h;
            if (this.chartSize == null){
                w = this.getWidth();
                h = this.getHeight();
            } else {
                w = this.chartSize.width;
                h = this.chartSize.height;
            }
            if (extension.equalsIgnoreCase("bmp"))
                aImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            else
                aImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = aImage.createGraphics();
            paintGraphics(g);            
            ImageIO.write(aImage, extension, new File(aFile));
        }
    }

    /**
     * Save image to Jpeg file
     *
     * @param fileName File name
     * @param dpi DPI
     * @throws java.io.IOException
     */
    public void saveImage_Jpeg_old(String fileName, int dpi) throws IOException {
        BufferedImage image = this.mapBitmap;
        Iterator i = ImageIO.getImageWritersByFormatName("jpeg");
        //are there any jpeg encoders available?

        if (i.hasNext()) //there's at least one ImageWriter, just use the first one
        {
            ImageWriter imageWriter = (ImageWriter) i.next();
            //get the param
            ImageWriteParam param = imageWriter.getDefaultWriteParam();
            ImageTypeSpecifier its = new ImageTypeSpecifier(image.getColorModel(), image.getSampleModel());

            //get metadata
            IIOMetadata iomd = imageWriter.getDefaultImageMetadata(its,
                    param);

            String formatName = "javax_imageio_jpeg_image_1.0";//this is the DOCTYPE of the metadata we need

            Node node = iomd.getAsTree(formatName);
            //what are child nodes?
            NodeList nl = node.getChildNodes();
            for (int j = 0; j < nl.getLength(); j++) {
                Node n = nl.item(j);
                System.out.println("node from IOMetadata is : "
                        + n.getNodeName());

                if (n.getNodeName().equals("JPEGvariety")) {
                    NodeList childNodes = n.getChildNodes();

                    for (int k = 0; k < childNodes.getLength(); k++) {
                        System.out.println("node #" + k + " is "
                                + childNodes.item(k).getNodeName());
                        if (childNodes.item(k).getNodeName().equals("app0JFIF")) {
                            NamedNodeMap nnm = childNodes.item(k).getAttributes();
                            //get the resUnits, Xdensity, and Ydensity attribuutes 
                            Node resUnitsNode = getAttributeByName(childNodes.item(k), "resUnits");
                            Node XdensityNode = getAttributeByName(childNodes.item(k), "Xdensity");
                            Node YdensityNode = getAttributeByName(childNodes.item(k), "Ydensity");

                            //reset values for nodes
                            resUnitsNode.setNodeValue("1"); //indicate DPI mode 
                            XdensityNode.setNodeValue(String.valueOf(dpi));
                            YdensityNode.setNodeValue(String.valueOf(dpi));

                            System.out.println("name="
                                    + resUnitsNode.getNodeName() + ", value=" + resUnitsNode.getNodeValue());
                            System.out.println("name="
                                    + XdensityNode.getNodeName() + ", value=" + XdensityNode.getNodeValue());
                            System.out.println("name="
                                    + YdensityNode.getNodeName() + ", value=" + YdensityNode.getNodeValue());

                        } //end if (childNodes.item(k).getNodeName().equals("app0JFIF"))
                    } //end if (n.getNodeName().equals("JPEGvariety")
                    break; //we don't care about the rest of the children
                } //end if (n.getNodeName().equals("JPEGvariety"))

            } //end  for (int j = 0; j < nl.getLength(); j++)

            try {
                iomd.setFromTree(formatName, node);
            } catch (IIOInvalidTreeException e) {
                e.printStackTrace();  //To change body of catch statement use Options | File Templates.
            }
            //attach the metadata to an image
            IIOImage iioimage = new IIOImage(image, null, iomd);
            FileImageOutputStream stream = new FileImageOutputStream(new File(fileName));
            try {
                imageWriter.setOutput(stream);
                imageWriter.write(iioimage);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                stream.close();
            }
        }  //end if (i.hasNext()) //there's at least one ImageWriter, just use the first one
    }

    /**
     * @param node
     * @param attributeName - name of child node to return
     * @return Node
     */
    private Node getAttributeByName(Node node, String attributeName) {
        if (node == null) {
            return null;
        }
        NamedNodeMap nnm = node.getAttributes();
        for (int i = 0; i < nnm.getLength(); i++) {
            Node n = nnm.item(i);
            if (n.getNodeName().equals(attributeName)) {
                return n;
            }
        }
        return null; // no such attribute was found
    }

    public boolean saveImage_Jpeg(String file, int dpi) {
        BufferedImage bufferedImage = this.mapBitmap;

        try {
            // Image writer 
            ImageWriter imageWriter = ImageIO.getImageWritersBySuffix("jpeg").next();
            ImageOutputStream ios = ImageIO.createImageOutputStream(new File(file));
            imageWriter.setOutput(ios);

            // Compression
            JPEGImageWriteParam jpegParams = (JPEGImageWriteParam) imageWriter.getDefaultWriteParam();
            jpegParams.setCompressionMode(JPEGImageWriteParam.MODE_EXPLICIT);
            jpegParams.setCompressionQuality(0.85f);

            // Metadata (dpi)
            IIOMetadata data = imageWriter.getDefaultImageMetadata(new ImageTypeSpecifier(bufferedImage), jpegParams);
            Element tree = (Element) data.getAsTree("javax_imageio_jpeg_image_1.0");
            Element jfif = (Element) tree.getElementsByTagName("app0JFIF").item(0);
            jfif.setAttribute("Xdensity", Integer.toString(dpi));
            jfif.setAttribute("Ydensity", Integer.toString(dpi));
            jfif.setAttribute("resUnits", "1"); // density is dots per inch	
            data.setFromTree("javax_imageio_jpeg_image_1.0", tree);

            // Write and clean up
            imageWriter.write(null, new IIOImage(bufferedImage, null, data), jpegParams);
            ios.close();
            imageWriter.dispose();
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    /**
     * Save image
     *
     * @param fileName File name
     * @param dpi DPI
     * @throws IOException
     */
    public void saveImage(String fileName, float dpi) throws IOException {
        File output = new File(fileName);
        output.delete();

        BufferedImage image = this.mapBitmap;
        String formatName = fileName.substring(fileName.lastIndexOf('.') + 1);
        if (formatName.equals("jpg")) {
            formatName = "jpeg";
        }

        for (Iterator<ImageWriter> iw = ImageIO.getImageWritersByFormatName(formatName); iw.hasNext();) {
            ImageWriter writer = iw.next();
            ImageWriteParam writeParam = writer.getDefaultWriteParam();
            ImageTypeSpecifier typeSpecifier = ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB);
            IIOMetadata metadata = writer.getDefaultImageMetadata(typeSpecifier, writeParam);
            if (metadata.isReadOnly() || !metadata.isStandardMetadataFormatSupported()) {
                continue;
            }

            setDPI(metadata, dpi);

            final ImageOutputStream stream = ImageIO.createImageOutputStream(output);
            try {
                writer.setOutput(stream);
                writer.write(metadata, new IIOImage(image, null, metadata), writeParam);
            } finally {
                stream.close();
            }
            break;
        }
    }

    private void setDPI(IIOMetadata metadata, float dpi) throws IIOInvalidTreeException {

        // for PMG, it's dots per millimeter
        double dotsPerMilli = 1.0 * dpi / 10 / INCH_2_CM;

        IIOMetadataNode horiz = new IIOMetadataNode("HorizontalPixelSize");
        horiz.setAttribute("value", Double.toString(dotsPerMilli));

        IIOMetadataNode vert = new IIOMetadataNode("VerticalPixelSize");
        vert.setAttribute("value", Double.toString(dotsPerMilli));

        IIOMetadataNode dim = new IIOMetadataNode("Dimension");
        dim.appendChild(horiz);
        dim.appendChild(vert);

        IIOMetadataNode root = new IIOMetadataNode("javax_imageio_1.0");
        root.appendChild(dim);

        metadata.mergeTree("javax_imageio_1.0", root);
    }

    /**
     * Get view image
     *
     * @return View image
     */
    public BufferedImage getViewImage() {
        return this.mapBitmap;
    }
    
    /**
     * Paint view image
     * @return View image
     */
    public BufferedImage paintViewImage(){
        BufferedImage aImage;
            int w, h;
            if (this.chartSize == null){
                w = this.getWidth();
                h = this.getHeight();
            } else {
                w = this.chartSize.width;
                h = this.chartSize.height;
            }
            aImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = aImage.createGraphics();
            paintGraphics(g); 
            
            return aImage;
    }
    // </editor-fold>
}
