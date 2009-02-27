package io;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;
import java.util.Observable;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
/**
 * All Preferences of the programm are stored in this class and read at start from an xml.
 * If this xml does not exist, or there are values missing
 *  there are std values (factroy presets) that are used then
 * <br><br>
 * This Class is a Singleton and Observable.
 * 
 * @author Ronny Bergmann
 *
 */
public class GeneralPreferences extends Observable
{
	/**
	 * A Handler, that reads the Data from the xml using the SAX ContentHandler Interface
	 * 
	 * @author Rommy Bergmann
	 *
	 */
	private class gpExtractor implements ContentHandler
	{
		private String key,type;
		private Stack<String> path;
		public gpExtractor()
		{
			type = new String();
			key = new String();
			path = new Stack<String>();
		}
		public void characters(char[] text, int start, int length) throws SAXException 
		{
			String val = "";
			for (int i=0; i<length; i++)
				val += text[start+i];
			//System.err.println("Get Key |"+key+"|, Type |"+type+"| Value |"+val+"|");
			if (type==null)
			{
				System.err.println(key+" has no type");
				return;
			}
			if (type.equals("Integer"))
			{
				IntValues.put(key,(new Integer(val)).intValue());
			}
			else if (type.equals("Boolean"))
			{
				BoolValues.put(key,(new Boolean(val)).booleanValue());				
			}
			else if (type.equals("String"))
			{
				if ((val==null)&&(key.endsWith("name")))
					StringValues.put(key,"");
				else
					StringValues.put(key,val);
			}
		}
		public void endDocument() throws SAXException {}
		public void endElement(String namespaceURI, String localName,String qualifiedName) throws SAXException 
		{
			if ((localName.equals("group"))||(localName.equals("value")))
			{	
				String verlassen = path.pop();
				//System.err.println("Verlasse "+verlassen);
				if (key.equals(verlassen))
					key = "";
				else //sonst den Punkt auch entfernen
					key = key.substring(0, key.length()-verlassen.length()-1);		
			}
		}
		public void endPrefixMapping(String arg0) throws SAXException {}
		public void ignorableWhitespace(char[] arg0, int arg1, int arg2) throws SAXException {}
		public void processingInstruction(String arg0, String arg1) throws SAXException {}
		public void setDocumentLocator(Locator arg0) {}
		public void skippedEntity(String arg0) throws SAXException {}
		public void startDocument() throws SAXException {}
		public void startElement(String namespaceURI, String localName,String qualifiedName, Attributes atts)
		{
			if ((localName.equals("group"))||(localName.equals("value")))
			{
				//System.err.println("Betrete "+atts.getValue("name"));
				if (atts.getValue("name").contains("."))
					System.err.println("Warning : The Attribute '"+atts.getValue("name")+"' may cause trouble.");
				if (!key.equals(""))
					key +=".";
				key +=atts.getValue("name");
				path.push(atts.getValue("name"));
				if (localName.equals("value"))
					type = atts.getValue("type");
			}
		}
		public void startPrefixMapping(String arg0, String arg1) throws SAXException {}

	}
	
	private static GeneralPreferences instance = null;
	//Einmal die Standard / Fabrikwerte einmal die aktullen Werte
	private TreeMap<String,Integer> IntValues;
	private TreeMap<String,Boolean> BoolValues;
	private TreeMap<String,String> StringValues;
	
	/**
	 * static Method for the singleton.
	 * @return
	 */
	public static GeneralPreferences getInstance()
	{
		if (instance==null)
		{
			instance = new GeneralPreferences();
		}
		return instance;
	}
	/**
	 * private constor, that is only called the very first time and then stored in instance
	 */
	private GeneralPreferences()
	{
		IntValues = new TreeMap<String, Integer>();
		BoolValues = new TreeMap<String, Boolean>();
		StringValues = new TreeMap<String, String>();
		readXML(); //load the standard values from 'xml/preferencex.xml'
	}
	/**
	 * Creates the Node name, where $ID is replaced by the id
	 * @param id id of a node
	 * @return
	 */
	public String getNodeName(int id)
	{
		return replace(this.getStringValue("node.name"), "$ID",""+id);
	}
	/**
	 * Creates the Subgraph name, where $ID is replaced by the id
	 * @param id id of a MSubgraph, we need a name for
	 * @return
	 */
	public String getSubgraphName(int id)
	{
		return replace(this.getStringValue("subgraph.name"),"$ID",""+id);
	}
	/**
	 * Create Edge name, where
	 * @param id $ID is replaced by id
	 * @param sid $SID is replaced by sid
	 * @param eid $EID is replaced by eid
	 * @return the edge name
	 */
	public String getEdgeName(int id, int sid, int eid)
	{
		String t = replace(this.getStringValue("edge.name"), "$ID", ""+id);
		t = replace(t,"$SID",""+sid);
		t = replace(t, "$EID", ""+eid);
		return t;
	}
	
	/**
	 * get the value stored with a key
	 * @param key
	 * 			the key of the Int Value
	 * @return
	 * 			a number if the Value exists, else null
	 */
	public int getIntValue(String key)
	{
		if (IntValues.get(key)!=null)
		{
			return IntValues.get(key);
		}
			return -1;
	}
	/**
	 * Set an Integer Value (if it doesn't exist it is created)
	 * @param key the name of the Key
	 * @param value the Value
	 */
	public void setIntValue(String key, int value)
	{
		IntValues.put(key, value);
		setChanged();
		notifyObservers(key);
	}
	/**
	 * Remove a Key from the Preferences. If it does not exist, nothing happens, else it is removed
	 * @param key name of the key to be removed
	 */
	public void removeIntValue(String key)
	{
		IntValues.remove(key);
		setChanged();
		notifyObservers(key);		
	}
	/**
	 * Get the Value of a Bool Key, if it does not exist, this method returns false
	 * @param key name of the key
	 * @return the value of the key, if it exists, else false
	 */
	public boolean getBoolValue(String key)
	{
		if (BoolValues.get(key)!=null)
		{
			return BoolValues.get(key);
		}
			return false;
	}
	/**
	 * Set a
	 * @param key Boolean Key to the
	 * @param value specified value
	 */
	public void setBoolValue(String key, boolean value)
	{
		BoolValues.put(key, value);
		setChanged();
		notifyObservers(key);
	}
	/**
	 * Remove a Key from the Preferences. If it does not exist, nothing happens, else it is removed
	 * @param key name of the key to be removed
	 */
	public void removeBoolValue(String key)
	{
		BoolValues.remove(key);
		setChanged();
		notifyObservers(key);		
	}
	/**
	 * Get teh Value of a String Key, if it does not exist, an empty String is returned
	 * @param key the name of the wanted key
	 * @return the String Value of the key, if it exists, else an empty String
	 */
	public String getStringValue(String key)
	{
		if (StringValues.get(key)!=null)
		{
			return StringValues.get(key);
		}
			return "";
	}
	/**
	 * Set the Value of a String-Entry
	 * @param key the key of the entry
	 * @param value the value
	 */
	public void setStringValue(String key, String value)
	{
		StringValues.put(key, value);
		setChanged();
		notifyObservers(key);
	}
	/**
	 * Remove a Key from the Preferences. If it does not exist, nothing happens, else it is removed
	 * @param key name of the key to be removed
	 */	
	public void removeStringValue(String key)
	{
		StringValues.remove(key);
		setChanged();
		notifyObservers(key);		
	}
	/** 
	 * check Validity of the Preferences stored at the moment in here. Returns false if one is missing
	 * 
	 */
	public boolean check()
	{
		if (IntValues.get("edge.arralpha")==null) return false;
		if (IntValues.get("edge.arrpart")==null) return false;
		if (IntValues.get("edge.arrsize")==null) return false;
		if (IntValues.get("edge.value")==null) return false;
		if (IntValues.get("edge.width")==null) return false;
		if (IntValues.get("edge.text_position")==null) return false;
		if (IntValues.get("edge.text_distance")==null) return false;
		if (IntValues.get("edge.text_size")==null) return false;
		if (IntValues.get("edge.line_distance")==null) return false;
		if (IntValues.get("edge.line_length")==null) return false;
		if (IntValues.get("edge.line_type")==null) return false;
		
		if (IntValues.get("edge.looplength")==null) return false;
		if (IntValues.get("edge.loopdirection")==null) return false;
		if (IntValues.get("edge.loopproportion")==null) return false;
		if (BoolValues.get("edge.loopclockwise")==null) return false;
		
		
		if (StringValues.get("edge.name")==null) return false;
		
		if (BoolValues.get("edge.orth_verticalfirst")==null) return false;
		if (BoolValues.get("edge.text_visible")==null) return false;
		if (BoolValues.get("edge.text_showvalue")==null) return false;
		
		
		
		if (BoolValues.get("graph.directed")==null) return false;
		//Dieser Wert ist nicht schlimm, also einfach Std setzen, schon okay
		if (BoolValues.get("graph.loadfileonstart")==null) return false;
		else //load on start is there and true but no file
		if (BoolValues.get("graph.loadfileonstart")&&(StringValues.get("graph.lastfile")==null))
			return false;
		
		if (BoolValues.get("grid.enabled")==null) return false;
		if (BoolValues.get("grid.synchron")==null) return false;
		if (BoolValues.get("grid.orientated")==null) return false;
		if (IntValues.get("grid.x")==null) return false;
		if (IntValues.get("grid.y")==null) return false;

		if (BoolValues.get("history.trackSelection")==null) return false;
		if (IntValues.get("history.Stacksize")==null) return false;
				
		if (StringValues.get("node.name")==null) return false;
		if (IntValues.get("node.size")==null) return false;
		
		if (IntValues.get("node.name_distance")==null) return false;
		if (IntValues.get("node.name_rotation")==null) return false;
		if (IntValues.get("node.name_size")==null) return false;
		if (BoolValues.get("node.name_visible")==null) return false;
		
		if (BoolValues.get("pref.saveonexit")==null) return false;
		
		if (IntValues.get("statistics.y")==null) return false;
		
		if (StringValues.get("subgraph.name")==null) return false;
		
		if (BoolValues.get("vgraphic.cpshow")==null) return false;
		if (IntValues.get("vgraphic.cpsize")==null) return false;
		if (IntValues.get("vgraphic.framedistance")==null) return false;
		if (IntValues.get("vgraphic.selcolb")==null) return false;
		if (IntValues.get("vgraphic.selcolg")==null) return false;
		if (IntValues.get("vgraphic.selcolr")==null) return false;
		if (IntValues.get("vgraphic.selwidth")==null) return false;
		if (IntValues.get("vgraphic.x")==null) return false;
		if (IntValues.get("vgraphic.y")==null) return false;

		if (IntValues.get("window.x")==null) return false;
		if (IntValues.get("window.y")==null) return false;
		
		//All clear despite new ones
		if (IntValues.get("edge.arrpos")==null)
		{ //Set to actual
			IntValues.put("edge.arrpos",100);
			this.writetoXML(); 
		}
		return true; //Falls alle da sind, alles okay
	}
		
	/**
	 * Return to the Defaut Values, the factory preset ones.
	 */
	public void resettoDefault()
	{
		IntValues = new TreeMap<String, Integer>();
		BoolValues = new TreeMap<String, Boolean>();
		StringValues = new TreeMap<String, String>();

		IntValues.put("edge.width",1);
		IntValues.put("edge.value",1);		
		IntValues.put("edge.arrsize",10);          // Size of the arrow segment
		IntValues.put("edge.arrpart",75);          // Size of the arrow segments
		IntValues.put("edge.arralpha",38);
		IntValues.put("edge.arrpos",100);
		IntValues.put("edge.text_distance",7);
		IntValues.put("edge.text_position",25);
		IntValues.put("edge.text_size",12);
		IntValues.put("edge.line_distance",10);
		IntValues.put("edge.line_length",10);
		IntValues.put("edge.line_type",1);
		
		IntValues.put("edge.looplength",33);
		IntValues.put("edge.loopdirection",0);
		IntValues.put("edge.loopproportion",100);
		
		IntValues.put("grid.x",50);
		IntValues.put("grid.y",50);
		IntValues.put("history.Stacksize",50);

		IntValues.put("node.size", 9);
		IntValues.put("node.name_distance",18);
		IntValues.put("node.name_rotation",270);
		IntValues.put("node.name_size",12);
		IntValues.put("statistics.y", 200);
		IntValues.put("vgraphic.framedistance",10);
		IntValues.put("vgraphic.cpsize",3);
		IntValues.put("vgraphic.selwidth",6);
		IntValues.put("vgraphic.selcolr",115);
		IntValues.put("vgraphic.selcolg",169);
		IntValues.put("vgraphic.selcolb",225);
		IntValues.put("vgraphic.x",500);
		IntValues.put("vgraphic.y",500);
		IntValues.put("window.x",600);
		IntValues.put("window.y",500);
	
		BoolValues.put("edge.orth_verticalfirst",false);
		BoolValues.put("edge.text_visible",false);
		BoolValues.put("edge.text_showvalue",false);
		BoolValues.put("edge.loopclockwise",true);
		BoolValues.put("graph.allowmultiple",false);
		BoolValues.put("graph.allowloops",false);
		BoolValues.put("graph.directed",true);
		BoolValues.put("graph.loadfileonstart",false);
		BoolValues.put("grid.enabled", false);
		BoolValues.put("grid.synchron", true);
		BoolValues.put("grid.orientated", true);
		BoolValues.put("history.trackSelection", false);

		BoolValues.put("node.name_visible",false);
		BoolValues.put("pref.saveonexit",true);
		BoolValues.put("vgraphic.cpshow",false);
		BoolValues.put("vgraphic.directed",true);
		
		StringValues.put("edge.name", "e_{$ID}");
		StringValues.put("graph.lastfile","$NONE");
		StringValues.put("graph.fileformat","visual");
		StringValues.put("node.name", "v_{$ID}");
		StringValues.put("subgraph.name","Untergraph #$ID");
	}
	/**
	 * Read the Preferences from the xml file
	 */
	public boolean readXML()
	{
		String curDir = System.getProperty("user.dir");
		curDir+="/data/xml/preferences.xml";
		File f = new File(curDir);
		if (!f.exists())
			return false;
	
		XMLReader parser;
		try {
			parser = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
		}
		catch (SAXException e) 
		{
			try {
				//System.err.println("bla");
				//Std System-Parser holen
				parser = XMLReaderFactory.createXMLReader();
			}
			catch (SAXException e2) 
			{
				throw new NoClassDefFoundError("No SAX parser is available");
				// or whatever exception your method is declared to throw
			}
		}
		try {
			InputStream in = new FileInputStream(f);
			InputSource input = new InputSource(in);
			input.setSystemId(f.getAbsolutePath());
			parser.setContentHandler(new gpExtractor());
			parser.parse(input);
		//	System.err.println(parser.toString());
		}
		catch (Exception e2)
		{
			System.err.println("Error on reading file '"+f.getName()+"' : "+e2.getMessage()+"");
		}
		setChanged();
		notifyObservers("load");
		return true;
	}
	
	/**
	 * Write actual Keys to xml
	 *
	 */
	public void writetoXML()
	{
		String curDir = System.getProperty("user.dir");
		curDir+="/data/xml";
		File f = new File(curDir);
		if (!f.exists())
			f.mkdir();
		curDir +="/preferences.xml";
		f = new File (curDir);
		if (!f.exists())
			try{
				f.createNewFile();
			}
			catch (Exception e)
			{
				System.err.println("DEBUG : Error Creating File : "+e.getMessage());
			}
		
		try {        
	        OutputStream fout= new FileOutputStream(f);
	        OutputStream bout= new BufferedOutputStream(fout);
	        OutputStreamWriter out = new OutputStreamWriter(bout, "UTF8");
	        out.write("<?xml version=\"1.0\" ");
	        out.write("encoding=\"UTF-8\"?>\r\n");  
	        out.write("<!-- Gravel Preferences File, please do not edit yourself -->\r\n\r\n");
	        out.write("<!DOCTYPE gravel.preferences [\r\n");
	        out.write("<!ELEMENT gravel.preferences (group*,value*)>\r\n");
	        out.write("<!ELEMENT group (group*,value*)>\r\n");
	        out.write("<!ELEMENT value (#PCDATA)>\r\n");
	        out.write("<!ATTLIST group\r\n");
	        out.write("\t name NMTOKEN #REQUIRED>\r\n");
	        out.write("<!ATTLIST value\r\n");
	        out.write("\t name NMTOKEN #REQUIRED\r\n");
	        out.write("\t type (Integer|Boolean|String) #REQUIRED>\r\n");	       	
	        out.write("]>\r\n\r\n\r\n");	        
	        out.write("<gravel.preferences>\r\n");  
	        
			TreeSet<String> AllValues = new TreeSet<String>();
			
			//ALL Integer Keys
			Iterator<String> intiter = IntValues.keySet().iterator();
			while (intiter.hasNext())
				AllValues.add(intiter.next());
			
			//All Boolean Keys
			Iterator<String> booliter = BoolValues.keySet().iterator();
			while (booliter.hasNext())
				AllValues.add(booliter.next());

			//All String Keys
			Iterator<String> stringiter = StringValues.keySet().iterator();
			while (stringiter.hasNext())
				AllValues.add(stringiter.next());

				//noch mehr ?
				
	        Iterator<String> iter = AllValues.iterator();
	        Stack<String> path = new Stack<String>();
	        if (iter.hasNext()) //wenigstens einer da
	        {    
	        	String next = iter.next();
	        	String form[] = next.split("\\.");
      	    	for (int i=0; i<form.length-1; i++)
	        	{
	        		
	        		for (int j=0; j<=i; j++) 
	        		{
	        			out.write("\t");
	        		}
		        	out.write("<group name=\""+form[i]+"\">\r\n");
        			path.push(form[i]);
	        	}
	        	//aktuelle wert schreiben (je nachdem wo die alle existieren
	        	if (IntValues.get(next)!=null)
    			{
	        		for (int j=0; j<form.length; j++)		out.write("\t");
	        		out.write("<value name=\""+form[form.length-1]+"\" type=\"Integer\">"+IntValues.get(next)+"</value>\r\n");
    			}
	        	if (BoolValues.get(next)!=null)
	        	{
	        		for (int j=0; j<form.length; j++)		out.write("\t");
	        		out.write("<value name=\""+form[form.length-1]+"\" type=\"Boolean\">"+BoolValues.get(next)+"</value>\r\n");
	        	    //out.write("<"+form[form.length-1]+" type=Boolean>"+BoolValues.get(next)+"</"+form[form.length-1]+">\r\n");
	        	}
	        	if (StringValues.get(next)!=null)
	        	{
	        		for (int j=0; j<form.length; j++)		out.write("\t");
	        		out.write("<value name=\""+form[form.length-1]+"\" type=\"String\">"+StringValues.get(next)+"</value>\r\n");
	        	    //out.write("<"+form[form.length-1]+" type=Boolean>"+BoolValues.get(next)+"</"+form[form.length-1]+">\r\n");
	        	}
	        	while (iter.hasNext())
	        	{	
	        		next = iter.next();
	        		form = next.split("\\.");
	        		int i = form.length-2; //length-1 ist das defintiv neue
	        		while ((!path.isEmpty())&&(!path.peek().equals(form[i])))
	        		{
	        			i--;
	        			for (int j=0; j<=i+1; j++) 
	        				out.write("\t");
	        			out.write("</group>\r\n"); 
	        			path.pop();    		
	        		} //nun ist der präfix identisch, alle nicht identischen neu einbauen
	        		i++; 
	        		for (int i2=i; i2<form.length-1; i2++)
		        	{
	        			for (int j=0; j<=i2; j++) 
	        				out.write("\t");
		        		out.write("<group name=\""+form[i2]+"\">\r\n");
		        		path.push(form[i2]);
		        	}
//	        		aktuelle wert schreiben (je nachdem wo die alle existieren
	        		if (IntValues.get(next)!=null)
	    			{
		        		for (int j=0; j<form.length; j++)		out.write("\t");
		        		out.write("<value name=\""+form[form.length-1]+"\" type=\"Integer\">"+IntValues.get(next)+"</value>\r\n");
	    			}
		        	if (BoolValues.get(next)!=null)
		        	{
		        		for (int j=0; j<form.length; j++)		out.write("\t");
		        		out.write("<value name=\""+form[form.length-1]+"\" type=\"Boolean\">"+BoolValues.get(next)+"</value>\r\n");
		        	    //out.write("<"+form[form.length-1]+" type=Boolean>"+BoolValues.get(next)+"</"+form[form.length-1]+">\r\n");
		        	}
		        	if (StringValues.get(next)!=null)
		        	{
		        		for (int j=0; j<form.length; j++)		out.write("\t");
		        		out.write("<value name=\""+form[form.length-1]+"\" type=\"String\">"+StringValues.get(next)+"</value>\r\n");
		        	    //out.write("<"+form[form.length-1]+" type=Boolean>"+BoolValues.get(next)+"</"+form[form.length-1]+">\r\n");
		        	}
		        }
//	        	alle noch offenen Schließen
		        int i = path.size();
		        while(!path.isEmpty())
		        {	
		        	i--;
        			for (int j=0; j<i+1; j++) out.write("\t");
        			out.write("</group>\r\n");
        			path.pop();		        	
		        }
	        }
	        out.write("</gravel.preferences>\r\n"); 
	        
	        out.flush();  // Don't forget to flush!
	        out.close();
	      }
	      catch (Exception e) {
	        System.err.println("Writing : "+e+" "+e.getLocalizedMessage());        
	      }
	}
	
	/**
	 * simple substring replacement
	 * @param in input
	 * @param remove remove this substring and replace it with  
	 * @param replace the replacement
	 * @return
	 */
	public static String replace(String in,String remove, String replace) 
	{
		if (in==null || remove==null || remove.length()==0) return in;
		StringBuffer sb = new StringBuffer();
		int oldIndex = 0;
		int newIndex = 0;
		int remLength = remove.length();
		while ( (newIndex = in.indexOf(remove,oldIndex)) > -1) 
		{
				//copy from last to new appearance
				sb.append(in.substring(oldIndex,newIndex));
				sb.append(replace);
				//set old index to end of last apperance.
				oldIndex = newIndex + remLength;
		}
		int inLength = in.length();
		//add part after last appearance of string to remove
		if(oldIndex<inLength) sb.append(in.substring(oldIndex,inLength));
		return sb.toString();
	}
}
