package control;

import io.GeneralPreferences;

import java.awt.Point;
import java.awt.event.MouseEvent;

import view.VGraphic;

import model.MNode;
import model.VGraph;
import model.VNode;
/**
 * Handling left click on background to create nodes - all other click actions are in the superclass
 * 
 * 
 * @author Ronny Bergmann
 */
public class OCMClickMouseHandler extends ClickMouseHandler {
	
	private VGraph vg;
	private VGraphic vgc;
	private GeneralPreferences gp;
		
	public OCMClickMouseHandler(VGraphic g)
	{
		super(g);
		vgc = g;
		vg = g.getVGraph();
		gp = GeneralPreferences.getInstance();
	}
	
	/*
	 * Mouse Listener fuer Tastenaktionen
	 */
	public void mousePressed(MouseEvent e) {}
	public void mouseClicked(MouseEvent e) 
	{
		super.mouseClicked(e);
		Point p = new Point(Math.round(e.getPoint().x/((float)vgc.getZoom()/100)),Math.round(e.getPoint().y/((float)vgc.getZoom()/100))); //rausrechnen
		if (e.getModifiers()==MouseEvent.BUTTON1_MASK) // Button 1/Links
		{
			VNode r = vg.modifyNodes.getFirstinRangeOf(p);
			if (r==null) 
			{	//Kein Knoten in der Nähe, also einen erstellen
				int i= vg.getMathGraph().modifyNodes.getNextIndex();
				//TODO: Semantisch nochmal überlegen, ob die Auswahl entfertn werden soll, so ein neuer Knoten erstellt wird
				if ((vg.modifyEdges.hasSelection()||vg.modifyNodes.hasSelection()))
					vg.deselect();
				vg.modifyNodes.add(new VNode(i,p.x,p.y, gp.getIntValue("node.size"), gp.getIntValue("node.name_distance"), gp.getIntValue("node.name_rotation"), gp.getIntValue("node.name_size"), gp.getBoolValue("node.name_visible")), new MNode(i,gp.getNodeName(i)));
			}	
		}
	}	
	public void mouseEntered(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
}