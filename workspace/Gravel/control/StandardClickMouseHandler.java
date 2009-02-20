package control;

import io.GeneralPreferences;

import java.awt.Point;
import java.awt.event.MouseEvent;

import model.VEdge;
import model.VGraph;
import model.VItem;
import model.VNode;
import model.Messages.GraphMessage;
/**
 * Standard Cick Mouse Handler
 * 
 * Handles every special click for the standard mode
 * 
 * @author Ronny Bergmann
 *
 */
public class StandardClickMouseHandler extends ClickMouseHandler {
	
	private VGraph vg;
	private GeneralPreferences gp;
	/**
	 * Initialize the given Standard Mode with an graph it is bound to 
	 * @param g
	 */
	public StandardClickMouseHandler(VGraph g)
	{
		super(g);
		vg = g;
		gp = GeneralPreferences.getInstance();
	}
	
	public void mousePressed(MouseEvent e) {}
	/**
	 * Click Actions select or deselect edges/nodes, background deselects all
	 * shoft click toggles selection of nodes/edges below
	 * 
	 */
	public void mouseClicked(MouseEvent e) 
	{
		super.mouseClicked(e);
		Point p = new Point(Math.round(e.getPoint().x/((float)gp.getIntValue("vgraphic.zoom")/100)),Math.round(e.getPoint().y/((float)gp.getIntValue("vgraphic.zoom")/100))); //rausrechnen
		if (e.getModifiers()==MouseEvent.BUTTON1_MASK) // Button 1
		{
			VNode r = vg.modifyNodes.getFirstinRangeOf(p);
			if (r != null) 
			{	//Auf einen Knoten klicken bewirkt, dass dieser selektiert wird
				if ((r.getSelectedStatus() & VItem.SELECTED) != VItem.SELECTED) {
					vg.deselect();
					r.setSelectedStatus(VItem.SELECTED);
					vg.pushNotify(new GraphMessage(GraphMessage.SELECTION, GraphMessage.UPDATE, GraphMessage.SELECTION|GraphMessage.NODE));
				} else
					vg.deselect();
			} else {
				VEdge s = vg.getEdgeinRangeOf(p,2.0);
				if (s != null) 
				{
					if ((s.getSelectedStatus() & VItem.SELECTED) != VItem.SELECTED) 
					{
						vg.deselect();
						s.setSelectedStatus(VItem.SELECTED);
						vg.pushNotify(new GraphMessage(GraphMessage.SELECTION, GraphMessage.UPDATE, GraphMessage.SELECTION|GraphMessage.EDGE));
					} else
						vg.deselect();
				} 
				else
				{
					vg.deselect();
				}
			}
		}
		if (e.getModifiers() == MouseEvent.BUTTON1_MASK+MouseEvent.SHIFT_MASK) // mit SHIFTlinks angeklickt, Auswahl erweitern
		{} //Superclass handles shift clicks
		else if ((e.getModifiers() == MouseEvent.BUTTON3_MASK) || (e.getModifiers() == MouseEvent.BUTTON1_MASK+MouseEvent.CTRL_MASK)) // mit rechts oder strg links
		{}//Superclass handles menus
	}

	public void mouseEntered(MouseEvent e) {}

	public void mouseExited(MouseEvent e) {}

	public void mouseReleased(MouseEvent e) {}

}
