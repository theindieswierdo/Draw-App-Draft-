package nbdrawapp1;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.io.*;
import java.util.ArrayList;


public class NBDrawApp1 extends JFrame
{
    // GUI Component dimentsions.
    private final int CANVAS_INITIAL_WIDTH = 800;
    private final int CANVAS_INITIAL_HEIGHT = 640;
    
    private final int CONTROL_PANEL_WIDTH = 200;
    private final int MESSAGE_AREA_HEIGHT = 100;
    
    private final int MAX_FREEHAND_PIXELS = 1000;
    //pixels data structure 
    private class Pixel{
        int x,y,size;
        Color colour;
        public Pixel(int x, int y, int size, Color colour){
            this.x = x;
            this.y = y;
            this.size = size;
            this.colour = colour;
        }
    }
    //pixels arraylist
    private ArrayList<Pixel> pixels;
    // Add new fields to track shape drawing
    private Point startPoint = null;
    private Point currentPoint = null;
    private Shape currentShape = null;
    
    private class Shape{
        int x, y, width, height;
        Color colour;
        String type;
        public Shape(int x, int y, int width, int height,Color colour,String type){
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.colour = colour;
            this.type = type;
        }
    }
    private ArrayList<Shape> shapes = new ArrayList<>();
    // Drawing area class (inner class).
    class Canvas extends JPanel
    {
        // Called every time there is a change in the canvas contents.
        @Override
        public void paintComponent(Graphics g){
            super.paintComponent(g);
            draw(g);
        }
    } // end inner class Canvas
    
    private Canvas canvas;
    
    private JPanel controlPanel;
    private JLabel coordinatesLabel;
    private JRadioButton lineRadioButton, ovalRadioButton, rectangleRadioButton, freehandRadioButton;
    private JSlider freehandSizeSlider;
    private JCheckBox fineCheckBox, coarseCheckBox;
    private JButton colourButton, clearButton, animateButton;
    
    private JTextArea messageArea;
    private JMenuBar menuBar;
    
    private Color selectedColour;
    private int[][] fxy = new int[MAX_FREEHAND_PIXELS][3];//position and size of square 
    private int freehandPixelsCount = 0;
        
    
    /*****************************************************************
     * 
     * Constructor method starts here
     *    ... and goes on for quite a few lines of code 
     */
    
    public NBDrawApp1(){
        pixels = new ArrayList<>();
        setTitle("Drawing Application");
        setLayout(new BorderLayout());  // Layout manager for the frame.
        
        // Canvas
        canvas = new Canvas();
          canvas.setBorder(new TitledBorder(new EtchedBorder(), "Canvas"));
          canvas.setPreferredSize(new Dimension(CANVAS_INITIAL_WIDTH, CANVAS_INITIAL_HEIGHT));
          // next line changes the cursor's rendering whenever the mouse drifts onto the canvas
          canvas.addMouseMotionListener(new CanvasMouseMotionListener());
          canvas.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
          add(canvas, BorderLayout.CENTER);
        canvas.addMouseMotionListener(new CanvasMouseMotionListener());//set the mouse listener in place
        // Menu bar
        menuBar = new JMenuBar();
          JMenu fileMenu = new JMenu("File");
            JMenuItem fileSaveMenuItem = new JMenuItem("Save");
            fileMenu.add(fileSaveMenuItem);
            JMenuItem fileLoadMenuItem = new JMenuItem("Load");
            fileMenu.add(fileLoadMenuItem);
            fileMenu.addSeparator();
            JMenuItem fileExitMenuItem = new JMenuItem("Exit");
            fileMenu.add(fileExitMenuItem);
          menuBar.add(fileMenu);
          JMenu helpMenu = new JMenu("Help");
            JMenuItem helpAboutMenuItem = new JMenuItem("About");
            helpMenu.add(helpAboutMenuItem);
          menuBar.add(helpMenu);
        add(menuBar, BorderLayout.PAGE_START);
        
        // Control Panel
        controlPanel = new JPanel();
          controlPanel.setBorder(new TitledBorder(new EtchedBorder(), "Control Panel"));
          controlPanel.setPreferredSize(new Dimension(CONTROL_PANEL_WIDTH, CANVAS_INITIAL_HEIGHT));
          // the following two lines put the control panel in a scroll pane (nicer?).      
          JScrollPane controlPanelScrollPane = new JScrollPane(controlPanel);
          controlPanelScrollPane.setPreferredSize(new Dimension(CONTROL_PANEL_WIDTH + 30, CANVAS_INITIAL_HEIGHT));
        add(controlPanelScrollPane, BorderLayout.LINE_START);        

        // Control Panel contents are specified in the next section eg: 
        //    mouse coords panel; 
        //    shape tools panel; 
        //    trace-slider panel; 
        //    grid panel; 
        //    colour choice panel; 
        //    "clear" n "animate" buttons
        
        // Mouse Coordinates panel
        JPanel coordinatesPanel = new JPanel();
          coordinatesPanel.setBorder(new TitledBorder(new EtchedBorder(), "Drawing Position"));
          coordinatesPanel.setPreferredSize(new Dimension(CONTROL_PANEL_WIDTH - 20, 60));
          coordinatesLabel = new JLabel();
          coordinatesPanel.add(coordinatesLabel);
        controlPanel.add(coordinatesPanel);
        
        // Drawing tools panel
        JPanel drawingToolsPanel = new JPanel();
          drawingToolsPanel.setPreferredSize(new Dimension(CONTROL_PANEL_WIDTH - 20, 140));
          drawingToolsPanel.setLayout(new GridLayout(0, 1));
          drawingToolsPanel.setBorder(new TitledBorder(new EtchedBorder(), "Drawing Tools"));
        controlPanel.add(drawingToolsPanel);
        
         //buttons
        // Initialize the freehand radio button
        freehandRadioButton = new JRadioButton("Freehand");
        // Create other radio buttons (if not already created)
        lineRadioButton = new JRadioButton("Line");
        ovalRadioButton = new JRadioButton("Oval");
        rectangleRadioButton = new JRadioButton("Rectangle");

        // Group the radio buttons so only one can be selected
        ButtonGroup drawingToolsGroup = new ButtonGroup();
        drawingToolsGroup.add(lineRadioButton);
        drawingToolsGroup.add(ovalRadioButton);
        drawingToolsGroup.add(rectangleRadioButton);
        drawingToolsGroup.add(freehandRadioButton);

        // Add the radio buttons to the drawing tools panel
        drawingToolsPanel.add(lineRadioButton);
        drawingToolsPanel.add(ovalRadioButton);
        drawingToolsPanel.add(rectangleRadioButton);
        drawingToolsPanel.add(freehandRadioButton);

        // Set Freehand as the default selected tool
        freehandRadioButton.setSelected(true);
        // Freehand trace size slider
        JPanel freehandSliderPanel = new JPanel();
          freehandSliderPanel.setPreferredSize(new Dimension(CONTROL_PANEL_WIDTH - 20, 90));
          drawingToolsPanel.setLayout(new GridLayout(0, 1));
          freehandSliderPanel.setBorder(new TitledBorder(new EtchedBorder(), "Freehand Size"));
        controlPanel.add(freehandSliderPanel);
        
        //initialise freehandsizeSlider
        freehandSizeSlider = new JSlider(JSlider.HORIZONTAL, 1, 50, 5); // min=1, max=50, default=5
        freehandSizeSlider.setMajorTickSpacing(10);
        freehandSizeSlider.setMinorTickSpacing(1);
        freehandSizeSlider.setPaintTicks(true);
        freehandSizeSlider.setPaintLabels(true);
        freehandSliderPanel.add(freehandSizeSlider);

        class FreehandSliderListener implements ChangeListener {
            @Override
            public void stateChanged(ChangeEvent event) {
                canvas.repaint(); // Repaint the canvas when the slider value changes
            }
        }
        freehandSizeSlider.addChangeListener(new FreehandSliderListener());

        // Grid Panel
        JPanel gridPanel = new JPanel();
          gridPanel.setPreferredSize(new Dimension(CONTROL_PANEL_WIDTH - 20, 80));
          gridPanel.setLayout(new GridLayout(0, 1));
          gridPanel.setBorder(new TitledBorder(new EtchedBorder(), "Grid"));
          fineCheckBox = new JCheckBox("Fine Grid");
          coarseCheckBox = new JCheckBox("Coarse Grid");
          // Add it to the appropriate panel
          gridPanel.add(fineCheckBox);
          gridPanel.add(coarseCheckBox);
          controlPanel.add(gridPanel);
          
          class MyCheckBoxListener implements ChangeListener{
              @Override
              public void stateChanged(ChangeEvent event){
                canvas.repaint();
              }
          }
          fineCheckBox.addChangeListener(new MyCheckBoxListener());
          coarseCheckBox.addChangeListener(new MyCheckBoxListener());
          
        // Colour Panel
        JPanel colourPanel = new JPanel();
          colourPanel.setPreferredSize(new Dimension(CONTROL_PANEL_WIDTH - 20, 90));
          colourPanel.setBorder(new TitledBorder(new EtchedBorder(), "Colour"));
          colourButton = new JButton();
          colourButton.setPreferredSize(new Dimension(50, 50));
          colourPanel.add(colourButton);
        controlPanel.add(colourPanel);
        class colourActionListener implements ActionListener{
            @Override
            public void actionPerformed(ActionEvent event) {
                if (event.getSource() == colourButton){
                    //coloiur picking
                    Color newColour = JColorChooser.showDialog(null, "Choose newdrawing colour", selectedColour);
                    if (newColour != null){
                        selectedColour = newColour; 
                        colourButton.setBackground(newColour);//update button to sho colour being used
                    }
                }    
            }
        }
        colourButton.addActionListener(new colourActionListener());
        
        // Clear button
        clearButton = new JButton("Clear Canvas");
        
          clearButton.setPreferredSize(new Dimension(CONTROL_PANEL_WIDTH - 20, 50));
        controlPanel.add(clearButton);
        class MyClearActionListener implements ActionListener{//isn't working
            @Override
            public void actionPerformed(ActionEvent e){
                    pixels.clear();
                    shapes.clear();
                    currentShape = null;
                    canvas.repaint();
                }
                
            }
        
        clearButton.addActionListener(new MyClearActionListener());
        
        // Animate button 
        animateButton = new JButton("Animate");
          animateButton.setPreferredSize(new Dimension(CONTROL_PANEL_WIDTH - 20, 50));
        controlPanel.add(animateButton);
        // that completes the control panel section

        // Message area
        messageArea = new JTextArea();
        messageArea.setEditable(false);
        messageArea.setBackground(canvas.getBackground());
        JScrollPane textAreaScrollPane = new JScrollPane(messageArea);
        textAreaScrollPane.setBorder(new TitledBorder(new EtchedBorder(), "Message Area"));
        textAreaScrollPane.setPreferredSize(new Dimension(CONTROL_PANEL_WIDTH + CANVAS_INITIAL_WIDTH, MESSAGE_AREA_HEIGHT));
        add(textAreaScrollPane, BorderLayout.PAGE_END);
        messageArea.append("Explore the drawing application!");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        pack();
        setVisible(true);  
    }
    // end of the NBDrawApp1 constructor method
     //mouse listeners for coordinates panel
    public class CanvasMouseMotionListener implements MouseMotionListener{
        @Override
        public void mouseMoved(MouseEvent event){
            String mousePosition = String.format("X: %d, Y: %d",event.getX(),event.getY());
            coordinatesLabel.setText(mousePosition);
        }
        public void mousePressed(MouseEvent event){
            startPoint = event.getPoint();
            // Check if freehand tool is selected and space is available
            if (freehandRadioButton.isSelected()) {
                // Get the mouse coordinates
                int x = event.getX();
                int y = event.getY();
                // Default size for squares
                int size = 5;
                // Repaint the canvas to display the new square
                canvas.repaint();
            }
        }
        public void mouseReleased(MouseEvent event){
            if (currentShape != null) {
                shapes.add(currentShape);
                currentShape = null;
            }
            startPoint = null;
            currentPoint = null;
            canvas.repaint();
        }
        
        public void mouseClicked(MouseEvent event){}
        public void mouseEntered(MouseEvent event){}
        public void mouseExited(MouseEvent event){}

        @Override
        public void mouseDragged(MouseEvent event) {
            currentPoint = event.getPoint();
            // Check if freehand tool is selected and space is available
            if (freehandRadioButton.isSelected()) {
                // Get the mouse coordinates
                int x = event.getX();
                int y = event.getY();
                int size = freehandSizeSlider.getValue(); // Get size from the slider
                //adding new pixels to the arraylist
                pixels.add(new Pixel(x - size/2, y - size/2, size, selectedColour));
                mouseMoved(event);
                canvas.repaint();
            }
            // Calculate width and height
            int width = currentPoint.x - startPoint.x;
            int height = currentPoint.y - startPoint.y;
        
            // Create temporary shape for preview
            if (width != 0 && height != 0) {  // Ensure non-zero dimensions
                int x = width < 0 ? currentPoint.x : startPoint.x;
                int y = height < 0 ? currentPoint.y : startPoint.y;
                width = Math.abs(width);
                height = Math.abs(height);

                String type = rectangleRadioButton.isSelected() ? "rectangle" : 
                             ovalRadioButton.isSelected() ? "oval" : "";
                         
                if (!type.isEmpty()) {
                    currentShape = new Shape(x, y, width, height, selectedColour, type);
                    canvas.repaint();
                }
            }
        }
    }
        // Called by the canvas' paintComponent method
        void draw(Graphics g){
           for (Pixel pixel : pixels) {
                g.setColor(pixel.colour);  // Use the pixel's stored color
                g.fillRect(pixel.x, pixel.y, pixel.size, pixel.size);
            }
           // Draw all completed shapes
            for (Shape shape : shapes) {
                g.setColor(shape.colour);
                if (shape.type.equals("rectangle")) {
                    g.drawRect(shape.x, shape.y, shape.width, shape.height);
                } else if (shape.type.equals("oval")) {
                    g.drawOval(shape.x, shape.y, shape.width, shape.height);
                }
            }

            // Draw current shape being drawn (preview)
            if (currentShape != null) {
                g.setColor(currentShape.colour);
                if (currentShape.type.equals("rectangle")) {
                    g.drawRect(currentShape.x, currentShape.y, 
                              currentShape.width, currentShape.height);
                } else if (currentShape.type.equals("oval")) {
                    g.drawOval(currentShape.x, currentShape.y, 
                              currentShape.width, currentShape.height);
                }
            }

            // Draw freehand pixels if any
            if (freehandRadioButton.isSelected()) {
                for (Pixel pixel : pixels) {
                    g.setColor(pixel.colour);
                    g.fillRect(pixel.x, pixel.y, pixel.size, pixel.size);
                }
            }
        }     

    public static void main(String[] args) {
        NBDrawApp1 NBDrawApp_instance = new NBDrawApp1();
    }
}
