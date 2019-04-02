package nodemanager.gui;

import java.awt.*;
import java.awt.event.*;
import nodemanager.Mode;
import nodemanager.Session;
import nodemanager.events.*;
import nodemanager.node.Node;

/**
 * @author Matt Crow (greengrappler12@gmail.com)
 */



/**
 * NodeIcons are used to visually display Nodes on a MapImage, 
 * as well as respond to mouse clicks.
 * 
 * Note how this does not implement a mouselistener. 
 * That is because this would have to be a component to do so,
 * but since NodeIcons need to be constantly repositioned,
 * java doesn't like that.
 * Solving the problem: MapImage will pass click events to this' mouse event listeners.
 */
public class NodeIcon{
    public final Node node;
    private final int id; //the ID to display
    private final Color color;
    private Scale scale;
    private int x; //position on the map image
    private int y;
    private boolean drawLinks; //whether or not this should draw links
    private MapImage onImage; // the image this is being drawn on
    
    private static int size = 30;
    
    /**
     * Creates a visual representation of the given Node
     * @param n the Node to create a representation of
     */
    public NodeIcon(Node n){
        id = n.id;
        node = n;
        color = (n.id < 0) ? Color.green : Color.red;
        
        x = node.getX();
        y = node.getY();
        
        drawLinks = false;
        onImage = null;
    }
    
    
    /**
     * Sets the size of node icons,
     * this changes both how they are drawn,
     * and their bounds when checking if they were clicked
     * @param s 
     */
    public static void setSize(int s){
        size = s;
    }
    
    
    /**
     * Gets the width and height of NodeIcons, in pixels
     * @return the size of NodeIcons
     */
    public static int getSize(){
        return size;
    }
    
    
    /**
     * Sets the map image this is displayed on.
     * Not that this doesn't effect anything apart from NodeDataPane's delete node option
     * @param i the map image this is drawn on.
     */
    public void setHost(MapImage i){
        onImage = i;
    }
    
    /**
     * gets the map image this is drawn on
     * @return the map image
     */
    public MapImage getHost(){
        return onImage;
    }
    
    /**
     * Gets the Node this represents
     * @return the Node this represents.
     */
    public Node getNode(){
        return node;
    }
    
    
    /**
     * Gets the scale this is resized to
     * @return this' scale
     */
    public Scale getScale(){
        return scale;
    }
    
    /**
     * 
     * @return this' x-coordinate on the map image
     */
    public int getX(){
        return x;
    }
    
    /**
     * 
     * @return this' y-coordinate on the map image
     */
    public int getY(){
        return y;
    }
    
    /**
     * Sets the position of this icon on the map image.
     * Note that this does not edit this' node
     * @param xc the x-coordinate on the map
     * @param yc the y-coordinate on the map
     */
    public void setPos(int xc, int yc){
        x = xc;
        y = yc;
    }
    
    /**
     * Updates this' node's position on the source.
     * this way, we can differentiate between moving a node,
     * and calling scaleTo
     */
    public void respositionNode(){
        node.update();
    }
    
    /**
     * Sets the scale this should be positioned based on,
     * @see Scale for more info
     * @param s the Scale to set position off of
     */
    public void scaleTo(Scale s){
        if(true || scale == null){
            x = s.x(node.getX());
            y = s.y(node.getY());
        } else {
            //this is causing problems, but I need it to work or user will have to repos nodes after scaleto is called
            if(scale.inverseX(x) - s.inverseX(s.x(scale.inverseX(x))) != 0.0){
                System.out.println("Moving from " + scale.inverseX(x) + " to " + s.inverseX(s.x(scale.inverseX(x))));
            }
            x = s.x(scale.inverseX(x));
            y = s.y(scale.inverseY(y));
        }
        scale = s;
    }
    
    /**
     * Moves this component back to where it was when the Node was initially imported
     */
    public void resetPos(){
        x = scale.x(node.getX());
        y = scale.y(node.getY());
    }
    
    
    /**
     * Checks to see if the given points 
     * is contained within this NodeIcon
     * @param xc the x-coordinate of the click, as a point on the map image
     * @param yc the y-coordinate of the click, as a point on the map image
     * @return whether or not this was clicked on
     */
    public boolean isIn(int xc, int yc){
        return x <= xc && 
               x + size >= xc &&
               y <= yc &&
               y + size >= yc;
    }
    
    /**
     * Sets if this should draw its links
     * @param b whether or not this should draw its links
     */
    public void setDrawLinks(boolean b) {
        drawLinks = b;
    }
    
    public void mouseClicked(MouseEvent me) {
        switch (Session.getMode()) {
            case NONE:
                Session.selectNode(node);
                node.displayData();
                break;
            case ADD_CONNECTION:
                Session.selectedNode.addAdjId(node.id);
                Session.logAction(new ConnectionAddedEvent(Session.selectedNode.id, node.id));
                Session.setMode(Mode.NONE);
                break;
            case REMOVE_CONNECTION:
                if(Session.selectedNode.removeAdj(node.id)){
                    Session.logAction(new ConnectionRemovedEvent(Session.selectedNode.id, node.id));
                    Session.setMode(Mode.NONE);
                }
                break;
            default:
                break;
        }
    }

    public void mouseEntered(MouseEvent me) {
        //drawLinks = true;
        //make this change to being displayed as node data
    }
    
    public void mouseExited(MouseEvent me) {
        //drawLinks = false;
        //make revert to displayed as node icon
    }
    
    
    /**
     * Draws this on a Graphics object
     * @param g the graphics context to draw on
     */
    public void draw(Graphics g){
        g.setColor(color);
        g.fillRect(x, y, size, size);
        
        g.setColor(Color.black);
        g.drawString(Integer.toString(id), x, y);
        
        if(drawLinks){
            drawAllLinks(g);
        }
    }
    
    
    /**
     * Graphically display all the connections between this icon's Nodes and its adjacent Nodes
     * @param g the Graphics context to draw on
     */
    private void drawAllLinks(Graphics g){
        node.getAdjIds()
                .stream()
                .map(id -> Node.get(id))
                .forEach(node -> drawLink(g, node));
    }
    
    
    /**
     * Draw a link between this NodeIcon and another Node's icon
     * @param n the Node to draw a connection to
     */
    private void drawLink(Graphics g, Node n){
        Graphics2D g2d = (Graphics2D)g;
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(size / 2));
        g2d.drawLine(getX(), getY(), n.getIcon().getX(), n.getIcon().getY());
    }
}
