package org.ideademo.pawz.pages;

import java.io.StringReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.util.Version;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.annotations.PageActivationContext;
import org.apache.tapestry5.annotations.Path;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.hibernate.HibernateSessionManager;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.hibernate.Session;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.MatchMode;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.query.dsl.BooleanJunction;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.hibernate.search.query.dsl.TermMatchingContext;
import org.ideademo.pawz.entities.Paw;

import java.awt.Toolkit;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.AssetSource;
import org.apache.tapestry5.internal.TapestryInternalUtils;
import org.ideademo.pawz.services.util.PDFStreamResponse;

import com.itextpdf.text.Anchor;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.ListItem;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import org.apache.log4j.Logger;


public class Index 
{
	 
  private static Logger logger = Logger.getLogger(Index.class);
  private static final StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_31); 

  
  /////////////////////////////
  //  Drives QBE Search
  @Persist (PersistenceConstants.FLASH)
  private Paw example;
  
  
  //////////////////////////////////////////////////////////////
  // Used in rendering within Loop just as in Grid (Table) Row
  @SuppressWarnings("unused")
  @Property 
  private Paw row;

    
  @Property
  @Persist (PersistenceConstants.FLASH)
  private String searchText;

  @Inject
  private Session session;
  
  @Inject
  private HibernateSessionManager sessionManager;

  @Property 
  @Persist (PersistenceConstants.FLASH)
  int retrieved; 
  @Property 
  @Persist (PersistenceConstants.FLASH)
  int total;

  @Inject
  @Path("context:layout/images/image067.gif")
  private Asset logoAsset;
  
  @Inject
  private AssetSource assetSource;
  
  @Inject
  Messages messages;

  
  ///////////////////////////////////////////////////////////////////////////////////////////////////////
  //  Select Boxes - Enumaration values - the user-visible labels are externalized in Index.properties 
  
  
  // the Capability Select box
  @Property
  @Persist (PersistenceConstants.FLASH)
  private Capability capability;
  
  public enum Capability
  {
	VARIABILITY, IMPACTS
  }

  
  
  // the Focus Area select box
  @Property
  @Persist (PersistenceConstants.FLASH)
  private Focus focusarea;
  
  public enum Focus
  {
	 WATER, COASTAL, ECOSYSTEM
  }

  
  // the regions select box
  @Property
  @Persist (PersistenceConstants.FLASH)
  @PageActivationContext 
  private Regions regions;
  
  public enum Regions
  {
	 // BAS = Pacific Basin, GLB = global - see the properties file 
	 CNP, WNP, SP, BAS, GLB
  }
  
  
  ////////////////////////////////////////////////////////////////////////////////////////////////////////
  //  Entity List generator - QBE, Text Search or Show All 
  //

  @SuppressWarnings("unchecked")
  public List<Paw> getList()
  {
	//////////////////////////////////
	// first interpret search criteria
	  
	// text search string 
	logger.info("Search Text = " + searchText);
	
	// Construct example for QBE Search by recording what selections have been in the choice boxes on this page  
	if (capability != null)  onValueChangedFromCapability(capability.toString());
	if (focusarea != null) onValueChangedFromFocusArea(focusarea.toString());
	if (regions != null) onValueChangedFromRegions(regions.toString());
	// at this point all the booleans in example have been set.
	// NOTE/MAY BE TODO: Lucene dependency may be removed by setting the text search criteria into various text fields of the example. 
	
	// Get all records anyway
	List <Paw> alst = session.createCriteria(Paw.class).list();
    total = alst.size();

	
	// then makes lists and sublists as per the search criteria 
	List<Paw> xlst=null; // xlst = Query by Example search List
    if(example != null)
    {
       Example ex = Example.create(example).excludeFalse().ignoreCase().enableLike(MatchMode.ANYWHERE);
       
       xlst = session.createCriteria(Paw.class).add(ex).list();
       
       
       if (xlst != null)
       {
    	   logger.info("Paw Example Search Result List Size  = " + xlst.size() );
    	   Collections.sort(xlst);
       }
       else
       {
         logger.info("Paw Example Search result did not find any results...");
       }
    }
    
    List<Paw> tlst=null;
    if (searchText != null && searchText.trim().length() > 0)
    {
      FullTextSession fullTextSession = Search.getFullTextSession(sessionManager.getSession());  
      try
      {
        fullTextSession.createIndexer().startAndWait();
       }
       catch (java.lang.InterruptedException e)
       {
         logger.warn("Lucene Indexing was interrupted by something " + e);
       }
      
       QueryBuilder qb = fullTextSession.getSearchFactory().buildQueryBuilder().forEntity( Paw.class ).get();
       
       // fields being covered by text search 
       TermMatchingContext onFields = qb
		        .keyword()
		        .onFields("code","name","description", "keywords","contact", "organization", "url", "worksheet", "partners","objectives", "dates", "resources", "feedback");
       
       BooleanJunction<BooleanJunction> bool = qb.bool();
       /////// Tokenize the search string for default AND logic ///
       TokenStream stream = analyzer.tokenStream(null, new StringReader(searchText));
       CharTermAttribute cattr = stream.addAttribute(CharTermAttribute.class);
       try
       {
        while (stream.incrementToken()) 
         {
    	   String token = cattr.toString();
    	   logger.info("Adding search token " +  token + " to look in Paws database");
    	   bool.must(onFields.matching(token).createQuery());
         }
        stream.end(); 
        stream.close(); 
       }
       catch (IOException ioe)
       {
    	   logger.warn("Paws Text Search: Encountered problem tokenizing search term " + searchText);
    	   logger.warn(ioe);
       }
       
       /////////////  the lucene query built from non-simplistic English words 
       org.apache.lucene.search.Query luceneQuery = bool.createQuery();
       
       tlst = fullTextSession.createFullTextQuery(luceneQuery, Paw.class).list();
       if (tlst != null) 
       {
    	   logger.info("TEXT Search for " + searchText + " found " + tlst.size() + " Paws records in database");
    	   Collections.sort(tlst);
       }
       else
       {
          logger.info("TEXT Search for " + searchText + " found nothing in Paws");
       }
    }
    
    
    // organize what type of list is returned...either total, partial (subset) or intersection of various search results  
    if (example == null && (searchText == null || searchText.trim().length() == 0))
    {
    	// Everything...
    	if (alst != null && alst.size() > 0)
    	{
    		logger.info ("Returing all " + alst.size() + " Paws records");
        	Collections.sort(alst);
    	}
    	else
    	{
    		logger.warn("No Paw records found in the database");
    	}
    	retrieved = total;
        return alst; 
    }
    else if (xlst == null && tlst != null)
    {
    	// just text search results
    	logger.info("Returing " + tlst.size() + " Paws records as a result of PURE text search (no QBE) for " + searchText);
    	retrieved = tlst.size();
    	return tlst;
    }
    else if (xlst != null && tlst == null)
    {
    	// just example query results
    	logger.info("Returning " + xlst.size() + " Paws records as a result of PURE Query-By-Example (QBE), no text string");
    	retrieved = xlst.size();
    	return xlst;
    }
    else 
    {
    	// get the INTERSECTION of the two lists
    	
    	// TRIVIAL: if one of them is empty, return the other
    	// if one of them is empty, return the other
    	if (xlst.size() == 0 && tlst.size() > 0)
    	{
        	logger.info("Returing " + tlst.size() + " Paws records as a result of ONLY text search, QBE pulled up ZERO records for " + searchText);
        	retrieved = tlst.size();
    		return tlst;
    	}

    	if (tlst.size() == 0 && xlst.size() > 0)
    	{
        	logger.info("Returning " + xlst.size() + " Paws records as a result of ONLY Query-By-Example (QBE), text search pulled up NOTHING for string " + searchText);
        	retrieved = xlst.size();
	        return xlst;
    	}
    	
    	
    	List <Paw> ivec = new Vector<Paw>();
    	// if both are empty, return this Empty vector. 
    	if (xlst.size() == 0 && tlst.size() == 0)
    	{
        	logger.info("Neither QBE nor text search for string " + searchText +  " pulled up ANY Paws Records.");
        	retrieved = 0;
    		return ivec;
    	}
    	
    	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    	// now deal with BOTH text and QBE being non-empty lists - implementing intersection by Database Primary Key -  Id
    	Iterator<Paw> xiterator = xlst.iterator();
    	while (xiterator.hasNext()) 
    	{
    		Paw x = xiterator.next();
    		Long xid = x.getId();
    		
        	Iterator<Paw> titerator = tlst.iterator();
    		while(titerator.hasNext())
    		{
        		Paw t = titerator.next();
        		Long tid = t.getId();
    			
        		if (tid == xid)
        		{
        			ivec.add(t); break;
        		}
        		
    		}
    			
    	}
    	// sort again - 
    	if (ivec.size() > 0)  Collections.sort(ivec);
    	logger.info("Returning " + ivec.size() + " Paws records from COMBINED (text, QBE) Search");
    	retrieved = ivec.size();
    	return ivec;
    }
    
  }
  

  
  ///////////////////////////////////////////////////////////////
  //  Action Event Handlers 
  //
  
  Object onSelectedFromSearch() 
  {
    return null; 
  }

  Object onSelectedFromClear() 
  {
    this.searchText = "";
   
    // nullify selectors 
    capability=null;
    focusarea=null;
    regions=null;
    
    this.example = null;
    return null; 
  }
  
  // regions select box listener...may be hooked-up to some AJAX zone if needed (later)
  Object onValueChangedFromRegions(String choice)
  {	
	  // if there is no example
	  
	  if (this.example == null) 
	  {
		  logger.info("Region Select:  Example is NULL");
		  this.example = new Paw(); 
	  }
	  else
	  {
		  logger.info("Region Select:  Example is NOT null");
	  }
	  logger.info("Region Choice = " + choice);
	  
	  clearRegions(example);
      if (choice == null)
	  {
    	// clear 
	  }
      else if (choice.equalsIgnoreCase("CNP"))
      {
    	example.setCentralNorthPacific(true);
    	logger.info("Example setCentralNorthPacific");
      }
      else if (choice.equalsIgnoreCase("WNP"))
      {
    	example.setWesternNorthPacific(true);
      }
      else if (choice.equalsIgnoreCase("SP"))
      {
    	example.setSouthPacific(true);  
      }
      else if (choice.equalsIgnoreCase("BAS"))
      {
    	example.setPacificBasin(true);   
      }
      else if (choice.equalsIgnoreCase("GLB"))
      {
    	example.setGlobal(true);
      }
      else
      {
    	  // do nothing
      }
      
	  // return request.isXHR() ? editZone.getBody() : null;
      // return index;
      return null;
  }
	
  // Focus select box listener
  Object onValueChangedFromFocusArea(String choice)
  {	
	  // if there is no example
	  
	  if (this.example == null) 
	  {
		  logger.info("Focus Area Select: Example is NULL");
		  this.example = new Paw(); 
	  }
	  else
	  {
		  logger.info("Focus Area Select: Example is NOT null");
	  }
	  logger.info("Focus Area Choice = " + choice);
	  
	  clearFocusArea(example);
      if (choice == null)
	  {
    	// clear 
	  }
      else if (choice.equalsIgnoreCase("WATER"))
      {
    	example.setWater(true);
      }
      else if (choice.equalsIgnoreCase("COASTAL"))
      {
    	example.setCoastal(true);
      }
      else if (choice.equalsIgnoreCase("ECOSYSTEM"))
      {
    	example.setEcosystem(true);  
      }
      else
      {
    	 // do nothing
      }
      
	  // return request.isXHR() ? editZone.getBody() : null;
      // return index;
      return null;
  }
  
  // Capability Type box listener
  Object onValueChangedFromCapability(String choice)
  {	
	  // if there is no example
	  
	  if (this.example == null) 
	  {
		  logger.info("Capability Select Value Changed, Example is NULL");
		  this.example = new Paw(); 
	  }
	  else
	  {
		  logger.info("Capability Type Select Value Changed, Example is NOT null");
	  }
	  logger.info("Capability Chosen = " + choice);
	   
	  clearCapabilities(example);
      if (choice == null)
	  {
    	// clear 
	  }
      else if (choice.equalsIgnoreCase("VARIABILITY"))
      {
    	example.setVariability(true);  
      }
      else if (choice.equalsIgnoreCase("IMPACTS"))
      {
    	example.setImpacts(true);  
      }
      else
      {
    	 // do nothing
      }
      
	  // return request.isXHR() ? editZone.getBody() : null;
      // return index;
      return null;
  }
  
 
  
  ////////////////////////////////////////////////
  //  QBE Setter 
  //  

  public void setExample(Paw x) 
  {
    this.example = x;
  }
  public StreamResponse onSelectedFromPdf() 
  {
      // Create PDF
      InputStream is = getPdfTable(getList());
      // Return response
      return new PDFStreamResponse(is,"PaCISProjectsActivities" + System.currentTimeMillis());
  }

  private InputStream getPdfTable(List list) 
  {

      // step 1: creation of a document-object
      Document document = new Document();

      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      try {
              // step 2:
              // we create a writer that listens to the document
              // and directs a PDF-stream to a file
              PdfWriter writer = PdfWriter.getInstance(document, baos);
              // step 3: we open the document
              document.open();
              
              java.awt.Image awtImage = Toolkit.getDefaultToolkit().createImage(logoAsset.getResource().toURL());
              if (awtImage != null)
              {
            	  com.itextpdf.text.Image logo = com.itextpdf.text.Image.getInstance(awtImage, null); 
            	  logo.scalePercent(50);
            	  if (logo != null) document.add(logo);
              }

              DateFormat formatter = new SimpleDateFormat
                      ("EEE MMM dd HH:mm:ss zzz yyyy");
                  Date date = new Date(System.currentTimeMillis());
                  TimeZone eastern = TimeZone.getTimeZone("Pacific/Honolulu");
                  formatter.setTimeZone(eastern);

              document.add(new Paragraph("Projects and Activities " + formatter.format(date)));
              
              String subheader = "Printing " + retrieved + " of total " + total + " records.";
              if (StringUtils.isNotBlank(searchText))
              {
            	  subheader += "  Searching for \"" + searchText + "\""; 
              }
              
              document.add(new Paragraph(subheader));
              document.add(Chunk.NEWLINE);document.add(Chunk.NEWLINE);
              
              // create table, 2 columns
           	Iterator<Paw> iterator = list.iterator();
           	int count=0;
       		while(iterator.hasNext())
      		{
       			count++;
          		Paw paw = iterator.next();
          		
              // create table, 2 columns
              String acronym = StringUtils.trimToEmpty(paw.getCode());
              String name = StringUtils.trimToEmpty(paw.getName());
              String description = StringUtils.trimToEmpty(paw.getDescription());
              String leadAgencies = StringUtils.trimToEmpty(paw.getOrganization());
              String contacts = StringUtils.trimToEmpty(paw.getContact());
              String partnering = StringUtils.trimToEmpty(paw.getPartners());
              String url = StringUtils.trimToEmpty(paw.getUrl());
              
              String feedback = StringUtils.trimToEmpty(paw.getFeedback());
              String objectives = StringUtils.trimToEmpty(paw.getObjectives());
              String resources = StringUtils.trimToEmpty(paw.getResources());
          	  String timelines = StringUtils.trimToEmpty(paw.getDates());
          	
          	  
          	  
          	  
          	  
                PdfPTable table = new PdfPTable(2);
                table.setWidths(new int[]{1, 4});
                table.setSplitRows(false);
                
                PdfPCell nameTitle = new PdfPCell(new Phrase("Name")); 
                
                if (StringUtils.isNotBlank(acronym)) name = name + " (" + acronym + ")";
                PdfPCell nameCell = new PdfPCell(new Phrase(name));
                
                nameTitle.setBackgroundColor(BaseColor.CYAN);  nameCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                
                table.addCell(nameTitle);  table.addCell(nameCell);          		          		
          		
                // variability
                com.itextpdf.text.List variabilities = new com.itextpdf.text.List(com.itextpdf.text.List.UNORDERED);
          		if (paw.isVariability()) 
          		{
          			ListItem item = new ListItem(getLabel("variability")); variabilities.add(item);
          		}
          		if (paw.isVarObservations()) 
          		{
          			ListItem item = new ListItem(getLabel("varObservations")); variabilities.add(item);
          		}
          		if (paw.isVarOperations()) 
          		{
          			ListItem item = new ListItem(getLabel("varOperations")); variabilities.add(item);
          		}
          		if (paw.isVarResearch()) 
          		{
          			ListItem item = new ListItem(getLabel("varResearch")); variabilities.add(item);
          		}
          		if (paw.isVarHistorical()) 
          		{
          			ListItem item = new ListItem(getLabel("varHistorical")); variabilities.add(item);
          		}
          		if (paw.isVarProjections()) 
          		{
          			ListItem item = new ListItem(getLabel("varProjections")); variabilities.add(item);
          		}
          		if (paw.isVarTraining()) 
          		{
          			ListItem item = new ListItem(getLabel("varTraining")); variabilities.add(item);
          		}
          		if (paw.isVarGuidance()) 
          		{
          			ListItem item = new ListItem(getLabel("varGuidance")); variabilities.add(item);
          		}
          		if (paw.isVarDecision()) 
          		{
          			ListItem item = new ListItem(getLabel("varDecision")); variabilities.add(item);
          		}
                
                
          		if(variabilities.size() > 0)
          		{
          		  PdfPCell varsCell = new PdfPCell(); varsCell.addElement(variabilities);
          		  table.addCell(new PdfPCell(new Phrase("Capability Area: Variability/Changes")));  table.addCell(varsCell);
          		}

                // ecv
                com.itextpdf.text.List essentials = new com.itextpdf.text.List(com.itextpdf.text.List.UNORDERED);
          		if (paw.isEcvAtmosphericSurface()) 
          		{
          			ListItem item = new ListItem(getLabel("ecvAtmosphericSurface")); essentials.add(item);
          		}
          		if (paw.isEcvAtmosphericUpperAir()) 
          		{
          			ListItem item = new ListItem(getLabel("ecvAtmosphericUpperAir")); essentials.add(item);
          		}
          		if (paw.isEcvAtmosphericComposition()) 
          		{
          			ListItem item = new ListItem(getLabel("ecvAtmosphericComposition")); essentials.add(item);
          		}
          		if (paw.isEcvOceanicSurface()) 
          		{
          			ListItem item = new ListItem(getLabel("ecvOceanicSurface")); essentials.add(item);
          		}
          		if (paw.isEcvOceanicSubsurface()) 
          		{
          			ListItem item = new ListItem(getLabel("ecvOceanicSubsurface")); essentials.add(item);
          		}
          		if (paw.isEcvTerrestrial()) 
          		{
          			ListItem item = new ListItem(getLabel("ecvTerrestrial")); essentials.add(item);
          		}
          		if(essentials.size() > 0)
          		{
          		  PdfPCell essCell = new PdfPCell(); essCell.addElement(essentials);
          		  table.addCell(new PdfPCell(new Phrase("ECV")));  table.addCell(essCell);
          		}
          		
          		// timeframe
          		com.itextpdf.text.List timeframes = new com.itextpdf.text.List(com.itextpdf.text.List.UNORDERED);
          		if (paw.isVarSeasonal()) 
          		{
          			ListItem item = new ListItem(getLabel("varSeasonal")); timeframes.add(item);
          		}
          		if (paw.isVarIntraAnnual()) 
          		{
          			ListItem item = new ListItem(getLabel("varIntraAnnual")); timeframes.add(item);
          		}
          		if (paw.isVarMultiDecadal()) 
          		{
          			ListItem item = new ListItem(getLabel("varMultiDecadal")); timeframes.add(item);
          		}
          		if(timeframes.size() > 0)
          		{
          		  PdfPCell essCell = new PdfPCell(); essCell.addElement(timeframes);
          		  table.addCell(new PdfPCell(new Phrase("Timeframe")));  table.addCell(essCell);
          		}
          		
                // impacts
                com.itextpdf.text.List impacts = new com.itextpdf.text.List(com.itextpdf.text.List.UNORDERED);
          		if (paw.isImpacts()) 
          		{
          			ListItem item = new ListItem(getLabel("impacts")); impacts.add(item);
          		}
          		if (paw.isClimateImpacts()) 
          		{
          			ListItem item = new ListItem(getLabel("climateImpacts")); impacts.add(item);
          		}
          		if (paw.isImpObservations()) 
          		{
          			ListItem item = new ListItem(getLabel("impObservations")); impacts.add(item);
          		}
          		if (paw.isImpResearch()) 
          		{
          			ListItem item = new ListItem(getLabel("impResearch")); impacts.add(item);
          		}
          		if (paw.isImpHistorical()) 
          		{
          			ListItem item = new ListItem(getLabel("impHistorical")); impacts.add(item);
          		}
          		if (paw.isImpProjections()) 
          		{
          			ListItem item = new ListItem(getLabel("impProjections")); impacts.add(item);
          		}
          		if (paw.isClimateAdaptation()) 
          		{
          			ListItem item = new ListItem(getLabel("climateAdaptation")); impacts.add(item);
          		}
          		if (paw.isImpTraining()) 
          		{
          			ListItem item = new ListItem(getLabel("impTraining")); impacts.add(item);
          		}
          		if (paw.isImpGuidance()) 
          		{
          			ListItem item = new ListItem(getLabel("impGuidance")); impacts.add(item);
          		}
          		if (paw.isImpDecision()) 
          		{
          			ListItem item = new ListItem(getLabel("impDecision")); impacts.add(item);
          		}
          		if (paw.isImpPolicies()) 
          		{
          			ListItem item = new ListItem(getLabel("impPolicies")); impacts.add(item);
          		}
          		if (paw.isImpAssessment()) 
          		{
          			ListItem item = new ListItem(getLabel("impAssessment")); impacts.add(item);
          		}
          		
          		if(impacts.size() > 0)
          		{
          		  PdfPCell typesCell = new PdfPCell(); typesCell.addElement(impacts);
          		  table.addCell(new PdfPCell(new Phrase("Capability Area: Impacts/Adaptations")));  table.addCell(typesCell);
          		}
                

          	    // sectors
          		com.itextpdf.text.List sectors = new com.itextpdf.text.List(com.itextpdf.text.List.UNORDERED);
          		if (paw.isHealth()) 
          		{
          			ListItem item = new ListItem(getLabel("health")); sectors.add(item);
          		}
          		if (paw.isFreshWater()) 
          		{
          			ListItem item = new ListItem(getLabel("freshWater")); sectors.add(item);
          		}
          		if (paw.isEnergy()) 
          		{
          			ListItem item = new ListItem(getLabel("energy")); sectors.add(item);
          		}
          		if (paw.isTransportation()) 
          		{
          			ListItem item = new ListItem(getLabel("transportation")); sectors.add(item);
          		}
          		if (paw.isPlanning()) 
          		{
          			ListItem item = new ListItem(getLabel("planning")); sectors.add(item);
          		}
          		if (paw.isSocioCultural()) 
          		{
          			ListItem item = new ListItem(getLabel("socioCultural")); sectors.add(item);
          		}
          		if (paw.isAgriculture()) 
          		{
          			ListItem item = new ListItem(getLabel("agriculture")); sectors.add(item);
          		}
          		if (paw.isRecreation()) 
          		{
          			ListItem item = new ListItem(getLabel("recreation")); sectors.add(item);
          		}
          		if (paw.isEcological()) 
          		{
          			ListItem item = new ListItem(getLabel("ecological")); sectors.add(item);
          		}
          		if (paw.isOtherSector()) 
          		{
          			ListItem item = new ListItem(getLabel("otherSector")); sectors.add(item);
          		}


          		if(sectors.size() > 0)
          		{
          		  PdfPCell typesCell = new PdfPCell(); typesCell.addElement(sectors);
          		  table.addCell(new PdfPCell(new Phrase("Sectors")));  table.addCell(typesCell);
          		}
          		
          		//status
          		com.itextpdf.text.List status = new com.itextpdf.text.List(com.itextpdf.text.List.UNORDERED);
          		if (paw.isCompleted())
          		{
          			ListItem item = new ListItem(getLabel("completed")); status.add(item);
          		}
          		if (paw.isOngoing())
          		{
          			ListItem item = new ListItem(getLabel("ongoing")); status.add(item);
          		}
          		if (paw.isPlanned())
          		{
          			ListItem item = new ListItem(getLabel("planned")); status.add(item);
          		}
          		if (paw.isProposed())
          		{
          			ListItem item = new ListItem(getLabel("proposed")); status.add(item);
          		}
          		
          		if (status.size() > 0)
          		{
           		  PdfPCell sCell = new PdfPCell(); sCell.addElement(status);
           		  table.addCell(new PdfPCell(new Phrase("Status")));  table.addCell(sCell);
          		}
	
          		
          		// focus area
          		com.itextpdf.text.List fa = new com.itextpdf.text.List(com.itextpdf.text.List.UNORDERED);
          		if(paw.isWater())
          		{
          			ListItem item = new ListItem(getLabel("water")); fa.add(item);
          		}
          		if(paw.isCoastal())
          		{
          			ListItem item = new ListItem(getLabel("coastal")); fa.add(item);
          		}
          		if(paw.isEcosystem())
          		{
          			ListItem item = new ListItem(getLabel("ecosystem")); fa.add(item);
          		}
          		
          		if (fa.size() > 0)
          		{
           		  PdfPCell faCell = new PdfPCell(); faCell.addElement(fa);
           		  table.addCell(new PdfPCell(new Phrase("Focus Area")));  table.addCell(faCell);
          		}

               
          		//region
          		com.itextpdf.text.List regions = new com.itextpdf.text.List(com.itextpdf.text.List.UNORDERED);
          		if (paw.isCentralNorthPacific())
          		{
          			ListItem item = new ListItem(getLabel("centralNorthPacific")); regions.add(item);
          		}
          		if (paw.isStateOfHawaii())
          		{
          			ListItem item = new ListItem(getLabel("stateOfHawaii")); regions.add(item);
          		}
          		if (paw.isNorthWestHawaiianIslands())
          		{
          			ListItem item = new ListItem(getLabel("northWesternHawaiianIslands")); regions.add(item);
          		}
          		if (paw.isPacificRemoteIslands())
          		{
          			ListItem item = new ListItem(getLabel("pacificRemoteIslands")); regions.add(item);
          		}
          		if (paw.isWesternNorthPacific())
          		{
          			ListItem item = new ListItem(getLabel("westernNorthPacific")); regions.add(item);
          		}
          		if (paw.isCnmi())
          		{
          			ListItem item = new ListItem(getLabel("cnmi")); regions.add(item);
          		}
          		if (paw.isFsm())
          		{
          			ListItem item = new ListItem(getLabel("fsm")); regions.add(item);
          		}
          		if (paw.isGuam())
          		{
          			ListItem item = new ListItem(getLabel("guam")); regions.add(item);
          		}
          		if (paw.isPalau())
          		{
          			ListItem item = new ListItem(getLabel("palau")); regions.add(item);
          		}
          		if (paw.isRmi())
          		{
          			ListItem item = new ListItem(getLabel("rmi")); regions.add(item);
          		}
          		if (paw.isOtherWesternNorthPacific())
          		{
          			ListItem item = new ListItem(getLabel("otherWesternNorthPacific")); regions.add(item);
          		}
          		if (paw.isSouthPacific())
          		{
          			ListItem item = new ListItem(getLabel("southPacific")); regions.add(item);
          		}
          		if (paw.isAmericanSamoa())
          		{
          			ListItem item = new ListItem(getLabel("americanSamoa")); regions.add(item);
          		}
          		if (paw.isAustralia())
          		{
          			ListItem item = new ListItem(getLabel("australia")); regions.add(item);
          		}
          		if (paw.isCookIslands())
          		{
          			ListItem item = new ListItem(getLabel("cookIslands")); regions.add(item);
          		}
          		if (paw.isFiji())
          		{
          			ListItem item = new ListItem(getLabel("fiji")); regions.add(item);
          		}
          		if (paw.isFrenchPolynesia())
          		{
          			ListItem item = new ListItem(getLabel("frenchPolynesia")); regions.add(item);
          		}
          		if (paw.isKiribati())
          		{
          			ListItem item = new ListItem(getLabel("kiribati")); regions.add(item);
          		}
          		if (paw.isNewZealand())
          		{
          			ListItem item = new ListItem(getLabel("newZealand")); regions.add(item);
          		}
          		if (paw.isPng())
          		{
          			ListItem item = new ListItem(getLabel("png")); regions.add(item);
          		}
          		if (paw.isSamoa())
          		{
          			ListItem item = new ListItem(getLabel("samoa")); regions.add(item);
          		}
          		if (paw.isSolomonIslands())
          		{
          			ListItem item = new ListItem(getLabel("solomonIslands")); regions.add(item);
          		}
          		if (paw.isTonga())
          		{
          			ListItem item = new ListItem(getLabel("tonga")); regions.add(item);
          		}
          		if (paw.isTuvalu())
          		{
          			ListItem item = new ListItem(getLabel("tuvalu")); regions.add(item);
          		}
          		if (paw.isVanuatu())
          		{
          			ListItem item = new ListItem(getLabel("vanuatu")); regions.add(item);
          		}
          		if (paw.isOtherSouthPacific())
          		{
          			ListItem item = new ListItem(getLabel("otherSouthPacific")); regions.add(item);
          		}
          		if (paw.isPacificBasin())
          		{
          			ListItem item = new ListItem(getLabel("pacificBasin")); regions.add(item);
          		}
          		if (paw.isGlobal())
          		{
          			ListItem item = new ListItem(getLabel("global")); regions.add(item);
          		}
          		
        		
          		if (regions.size() > 0)
          		{
           		  PdfPCell rCell = new PdfPCell(); rCell.addElement(regions);
           		  table.addCell(new PdfPCell(new Phrase("Regions")));  table.addCell(rCell);
          		}
          		
          		
          		
          		
          		// text fields
          		
          		if (StringUtils.isNotBlank(description))
          		{
          		  table.addCell(new PdfPCell(new Phrase("Description")));  table.addCell(new PdfPCell(new Phrase(description)));
          		}
          		
          		if (StringUtils.isNotBlank(objectives))
          		{
          		  table.addCell(new PdfPCell(new Phrase("Objectives/Outcomes")));  table.addCell(new PdfPCell(new Phrase(objectives)));
          		}
          		
          		if (StringUtils.isNotBlank(leadAgencies))
          		{
          		  table.addCell(new PdfPCell(new Phrase("Lead Agencies")));  table.addCell(new PdfPCell(new Phrase(leadAgencies)));
          		}

          		if (StringUtils.isNotBlank(contacts))
          		{
          		  table.addCell(new PdfPCell(new Phrase("Contacts")));  table.addCell(new PdfPCell(new Phrase(contacts)));
          		}
                
          		if (StringUtils.isNotBlank(partnering))
          		{
          		  table.addCell(new PdfPCell(new Phrase("Partnering Agencies")));  table.addCell(new PdfPCell(new Phrase(partnering)));
          		}
          		
          		if (StringUtils.isNotBlank(resources))
          		{
          		  table.addCell(new PdfPCell(new Phrase("Required Resources")));  table.addCell(new PdfPCell(new Phrase(resources)));
          		}

          		if (StringUtils.isNotBlank(timelines))
          		{
          		  table.addCell(new PdfPCell(new Phrase("Projected Timelines")));  table.addCell(new PdfPCell(new Phrase(timelines)));
          		}
          		
          		if (StringUtils.isNotBlank(feedback))
          		{
          		  table.addCell(new PdfPCell(new Phrase("Feedback/Evaluation")));  table.addCell(new PdfPCell(new Phrase(feedback)));
          		}
          		
          		if (StringUtils.isNotBlank(url))
          		{
                          Anchor link = new Anchor(StringUtils.trimToEmpty(url)); link.setReference(StringUtils.trimToEmpty(url));
          		  table.addCell(new PdfPCell(new Phrase("Url")));  table.addCell(new PdfPCell(link));
          		}

          		
          		document.add(table);
          		document.add(Chunk.NEWLINE);
      		}
              
              
      } catch (DocumentException de) {
              logger.fatal(de.getMessage());
      }
      catch (IOException ie)
      {
    	 logger.warn("Could not find NOAA logo (likely)");
    	 logger.warn(ie);
      }

      // step 5: we close the document
      document.close();
      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
      return bais;
}

  public StreamResponse onReturnStreamResponse(long id) 
  {

      Paw paw =  (Paw) session.load(Paw.class, id);


      // step 1: creation of a document-object
      Document document = new Document();

      ByteArrayOutputStream baos = new ByteArrayOutputStream();

      try {
              // step 2:
              // we create a writer that listens to the document
              // and directs a PDF-stream to a file
              PdfWriter writer = PdfWriter.getInstance(document, baos);
              // step 3: we open the document
              document.open();
              
              java.awt.Image awtImage = Toolkit.getDefaultToolkit().createImage(logoAsset.getResource().toURL());
              if (awtImage != null)
              {
            	  com.itextpdf.text.Image logo = com.itextpdf.text.Image.getInstance(awtImage, null); 
            	  logo.scalePercent(50);
            	  if (logo != null) document.add(logo);
              }

              DateFormat formatter = new SimpleDateFormat
                      ("EEE MMM dd HH:mm:ss zzz yyyy");
                  Date date = new Date(System.currentTimeMillis());
                  TimeZone eastern = TimeZone.getTimeZone("Pacific/Honolulu");
                  formatter.setTimeZone(eastern);

              document.add(new Paragraph("Projects and Activities" + formatter.format(date)));
              
              
              document.add(Chunk.NEWLINE);document.add(Chunk.NEWLINE);
              
              // create table, 2 columns
              String acronym = StringUtils.trimToEmpty(paw.getCode());
              String name = StringUtils.trimToEmpty(paw.getName());
              String description = StringUtils.trimToEmpty(paw.getDescription());
              String leadAgencies = StringUtils.trimToEmpty(paw.getOrganization());
              String contacts = StringUtils.trimToEmpty(paw.getContact());
              String partnering = StringUtils.trimToEmpty(paw.getPartners());
              String url = StringUtils.trimToEmpty(paw.getUrl());
              
              String feedback = StringUtils.trimToEmpty(paw.getFeedback());
              String objectives = StringUtils.trimToEmpty(paw.getObjectives());
              String resources = StringUtils.trimToEmpty(paw.getResources());
          	  String timelines = StringUtils.trimToEmpty(paw.getDates());
          	
          	  
          	  
          	  
          	  
                PdfPTable table = new PdfPTable(2);
                table.setWidths(new int[]{1, 4});
                table.setSplitRows(false);
                
                PdfPCell nameTitle = new PdfPCell(new Phrase("Name")); 
                
                if (StringUtils.isNotBlank(acronym)) name = name + " (" + acronym + ")";
                PdfPCell nameCell = new PdfPCell(new Phrase(name));
                
                nameTitle.setBackgroundColor(BaseColor.CYAN);  nameCell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                
                table.addCell(nameTitle);  table.addCell(nameCell);          		          		
          		
                // variability
                com.itextpdf.text.List variabilities = new com.itextpdf.text.List(com.itextpdf.text.List.UNORDERED);
          		if (paw.isVariability()) 
          		{
          			ListItem item = new ListItem(getLabel("variability")); variabilities.add(item);
          		}
          		if (paw.isVarObservations()) 
          		{
          			ListItem item = new ListItem(getLabel("varObservations")); variabilities.add(item);
          		}
          		if (paw.isVarOperations()) 
          		{
          			ListItem item = new ListItem(getLabel("varOperations")); variabilities.add(item);
          		}
          		if (paw.isVarResearch()) 
          		{
          			ListItem item = new ListItem(getLabel("varResearch")); variabilities.add(item);
          		}
          		if (paw.isVarHistorical()) 
          		{
          			ListItem item = new ListItem(getLabel("varHistorical")); variabilities.add(item);
          		}
          		if (paw.isVarProjections()) 
          		{
          			ListItem item = new ListItem(getLabel("varProjections")); variabilities.add(item);
          		}
          		if (paw.isVarTraining()) 
          		{
          			ListItem item = new ListItem(getLabel("varTraining")); variabilities.add(item);
          		}
          		if (paw.isVarGuidance()) 
          		{
          			ListItem item = new ListItem(getLabel("varGuidance")); variabilities.add(item);
          		}
          		if (paw.isVarDecision()) 
          		{
          			ListItem item = new ListItem(getLabel("varDecision")); variabilities.add(item);
          		}
                
                
          		if(variabilities.size() > 0)
          		{
          		  PdfPCell varsCell = new PdfPCell(); varsCell.addElement(variabilities);
          		  table.addCell(new PdfPCell(new Phrase("Capability Area: Variability/Changes")));  table.addCell(varsCell);
          		}

                // ecv
                com.itextpdf.text.List essentials = new com.itextpdf.text.List(com.itextpdf.text.List.UNORDERED);
          		if (paw.isEcvAtmosphericSurface()) 
          		{
          			ListItem item = new ListItem(getLabel("ecvAtmosphericSurface")); essentials.add(item);
          		}
          		if (paw.isEcvAtmosphericUpperAir()) 
          		{
          			ListItem item = new ListItem(getLabel("ecvAtmosphericUpperAir")); essentials.add(item);
          		}
          		if (paw.isEcvAtmosphericComposition()) 
          		{
          			ListItem item = new ListItem(getLabel("ecvAtmosphericComposition")); essentials.add(item);
          		}
          		if (paw.isEcvOceanicSurface()) 
          		{
          			ListItem item = new ListItem(getLabel("ecvOceanicSurface")); essentials.add(item);
          		}
          		if (paw.isEcvOceanicSubsurface()) 
          		{
          			ListItem item = new ListItem(getLabel("ecvOceanicSubsurface")); essentials.add(item);
          		}
          		if (paw.isEcvTerrestrial()) 
          		{
          			ListItem item = new ListItem(getLabel("ecvTerrestrial")); essentials.add(item);
          		}
          		if(essentials.size() > 0)
          		{
          		  PdfPCell essCell = new PdfPCell(); essCell.addElement(essentials);
          		  table.addCell(new PdfPCell(new Phrase("ECV")));  table.addCell(essCell);
          		}
          		
          		// timeframe
          		com.itextpdf.text.List timeframes = new com.itextpdf.text.List(com.itextpdf.text.List.UNORDERED);
          		if (paw.isVarSeasonal()) 
          		{
          			ListItem item = new ListItem(getLabel("varSeasonal")); timeframes.add(item);
          		}
          		if (paw.isVarIntraAnnual()) 
          		{
          			ListItem item = new ListItem(getLabel("varIntraAnnual")); timeframes.add(item);
          		}
          		if (paw.isVarMultiDecadal()) 
          		{
          			ListItem item = new ListItem(getLabel("varMultiDecadal")); timeframes.add(item);
          		}
          		if(timeframes.size() > 0)
          		{
          		  PdfPCell essCell = new PdfPCell(); essCell.addElement(timeframes);
          		  table.addCell(new PdfPCell(new Phrase("Timeframe")));  table.addCell(essCell);
          		}
          		
                // impacts
                com.itextpdf.text.List impacts = new com.itextpdf.text.List(com.itextpdf.text.List.UNORDERED);
          		if (paw.isImpacts()) 
          		{
          			ListItem item = new ListItem(getLabel("impacts")); impacts.add(item);
          		}
          		if (paw.isClimateImpacts()) 
          		{
          			ListItem item = new ListItem(getLabel("climateImpacts")); impacts.add(item);
          		}
          		if (paw.isImpObservations()) 
          		{
          			ListItem item = new ListItem(getLabel("impObservations")); impacts.add(item);
          		}
          		if (paw.isImpResearch()) 
          		{
          			ListItem item = new ListItem(getLabel("impResearch")); impacts.add(item);
          		}
          		if (paw.isImpHistorical()) 
          		{
          			ListItem item = new ListItem(getLabel("impHistorical")); impacts.add(item);
          		}
          		if (paw.isImpProjections()) 
          		{
          			ListItem item = new ListItem(getLabel("impProjections")); impacts.add(item);
          		}
          		if (paw.isClimateAdaptation()) 
          		{
          			ListItem item = new ListItem(getLabel("climateAdaptation")); impacts.add(item);
          		}
          		if (paw.isImpTraining()) 
          		{
          			ListItem item = new ListItem(getLabel("impTraining")); impacts.add(item);
          		}
          		if (paw.isImpGuidance()) 
          		{
          			ListItem item = new ListItem(getLabel("impGuidance")); impacts.add(item);
          		}
          		if (paw.isImpDecision()) 
          		{
          			ListItem item = new ListItem(getLabel("impDecision")); impacts.add(item);
          		}
          		if (paw.isImpPolicies()) 
          		{
          			ListItem item = new ListItem(getLabel("impPolicies")); impacts.add(item);
          		}
          		if (paw.isImpAssessment()) 
          		{
          			ListItem item = new ListItem(getLabel("impAssessment")); impacts.add(item);
          		}
          		
          		if(impacts.size() > 0)
          		{
          		  PdfPCell typesCell = new PdfPCell(); typesCell.addElement(impacts);
          		  table.addCell(new PdfPCell(new Phrase("Capability Area: Impacts/Adaptations")));  table.addCell(typesCell);
          		}
                

          	    // sectors
          		com.itextpdf.text.List sectors = new com.itextpdf.text.List(com.itextpdf.text.List.UNORDERED);
          		if (paw.isHealth()) 
          		{
          			ListItem item = new ListItem(getLabel("health")); sectors.add(item);
          		}
          		if (paw.isFreshWater()) 
          		{
          			ListItem item = new ListItem(getLabel("freshWater")); sectors.add(item);
          		}
          		if (paw.isEnergy()) 
          		{
          			ListItem item = new ListItem(getLabel("energy")); sectors.add(item);
          		}
          		if (paw.isTransportation()) 
          		{
          			ListItem item = new ListItem(getLabel("transportation")); sectors.add(item);
          		}
          		if (paw.isPlanning()) 
          		{
          			ListItem item = new ListItem(getLabel("planning")); sectors.add(item);
          		}
          		if (paw.isSocioCultural()) 
          		{
          			ListItem item = new ListItem(getLabel("socioCultural")); sectors.add(item);
          		}
          		if (paw.isAgriculture()) 
          		{
          			ListItem item = new ListItem(getLabel("agriculture")); sectors.add(item);
          		}
          		if (paw.isRecreation()) 
          		{
          			ListItem item = new ListItem(getLabel("recreation")); sectors.add(item);
          		}
          		if (paw.isEcological()) 
          		{
          			ListItem item = new ListItem(getLabel("ecological")); sectors.add(item);
          		}
          		if (paw.isOtherSector()) 
          		{
          			ListItem item = new ListItem(getLabel("otherSector")); sectors.add(item);
          		}


          		if(sectors.size() > 0)
          		{
          		  PdfPCell typesCell = new PdfPCell(); typesCell.addElement(sectors);
          		  table.addCell(new PdfPCell(new Phrase("Sectors")));  table.addCell(typesCell);
          		}
          		
          		//status
          		com.itextpdf.text.List status = new com.itextpdf.text.List(com.itextpdf.text.List.UNORDERED);
          		if (paw.isCompleted())
          		{
          			ListItem item = new ListItem(getLabel("completed")); status.add(item);
          		}
          		if (paw.isOngoing())
          		{
          			ListItem item = new ListItem(getLabel("ongoing")); status.add(item);
          		}
          		if (paw.isPlanned())
          		{
          			ListItem item = new ListItem(getLabel("planned")); status.add(item);
          		}
          		if (paw.isProposed())
          		{
          			ListItem item = new ListItem(getLabel("proposed")); status.add(item);
          		}
          		
          		if (status.size() > 0)
          		{
           		  PdfPCell sCell = new PdfPCell(); sCell.addElement(status);
           		  table.addCell(new PdfPCell(new Phrase("Status")));  table.addCell(sCell);
          		}
	
          		
          		// focus area
          		com.itextpdf.text.List fa = new com.itextpdf.text.List(com.itextpdf.text.List.UNORDERED);
          		if(paw.isWater())
          		{
          			ListItem item = new ListItem(getLabel("water")); fa.add(item);
          		}
          		if(paw.isCoastal())
          		{
          			ListItem item = new ListItem(getLabel("coastal")); fa.add(item);
          		}
          		if(paw.isEcosystem())
          		{
          			ListItem item = new ListItem(getLabel("ecosystem")); fa.add(item);
          		}
          		
          		if (fa.size() > 0)
          		{
           		  PdfPCell faCell = new PdfPCell(); faCell.addElement(fa);
           		  table.addCell(new PdfPCell(new Phrase("Focus Area")));  table.addCell(faCell);
          		}

               
          		//region
          		com.itextpdf.text.List regions = new com.itextpdf.text.List(com.itextpdf.text.List.UNORDERED);
          		if (paw.isCentralNorthPacific())
          		{
          			ListItem item = new ListItem(getLabel("centralNorthPacific")); regions.add(item);
          		}
          		if (paw.isStateOfHawaii())
          		{
          			ListItem item = new ListItem(getLabel("stateOfHawaii")); regions.add(item);
          		}
          		if (paw.isNorthWestHawaiianIslands())
          		{
          			ListItem item = new ListItem(getLabel("northWesternHawaiianIslands")); regions.add(item);
          		}
          		if (paw.isPacificRemoteIslands())
          		{
          			ListItem item = new ListItem(getLabel("pacificRemoteIslands")); regions.add(item);
          		}
          		if (paw.isWesternNorthPacific())
          		{
          			ListItem item = new ListItem(getLabel("westernNorthPacific")); regions.add(item);
          		}
          		if (paw.isCnmi())
          		{
          			ListItem item = new ListItem(getLabel("cnmi")); regions.add(item);
          		}
          		if (paw.isFsm())
          		{
          			ListItem item = new ListItem(getLabel("fsm")); regions.add(item);
          		}
          		if (paw.isGuam())
          		{
          			ListItem item = new ListItem(getLabel("guam")); regions.add(item);
          		}
          		if (paw.isPalau())
          		{
          			ListItem item = new ListItem(getLabel("palau")); regions.add(item);
          		}
          		if (paw.isRmi())
          		{
          			ListItem item = new ListItem(getLabel("rmi")); regions.add(item);
          		}
          		if (paw.isOtherWesternNorthPacific())
          		{
          			ListItem item = new ListItem(getLabel("otherWesternNorthPacific")); regions.add(item);
          		}
          		if (paw.isSouthPacific())
          		{
          			ListItem item = new ListItem(getLabel("southPacific")); regions.add(item);
          		}
          		if (paw.isAmericanSamoa())
          		{
          			ListItem item = new ListItem(getLabel("americanSamoa")); regions.add(item);
          		}
          		if (paw.isAustralia())
          		{
          			ListItem item = new ListItem(getLabel("australia")); regions.add(item);
          		}
          		if (paw.isCookIslands())
          		{
          			ListItem item = new ListItem(getLabel("cookIslands")); regions.add(item);
          		}
          		if (paw.isFiji())
          		{
          			ListItem item = new ListItem(getLabel("fiji")); regions.add(item);
          		}
          		if (paw.isFrenchPolynesia())
          		{
          			ListItem item = new ListItem(getLabel("frenchPolynesia")); regions.add(item);
          		}
          		if (paw.isKiribati())
          		{
          			ListItem item = new ListItem(getLabel("kiribati")); regions.add(item);
          		}
          		if (paw.isNewZealand())
          		{
          			ListItem item = new ListItem(getLabel("newZealand")); regions.add(item);
          		}
          		if (paw.isPng())
          		{
          			ListItem item = new ListItem(getLabel("png")); regions.add(item);
          		}
          		if (paw.isSamoa())
          		{
          			ListItem item = new ListItem(getLabel("samoa")); regions.add(item);
          		}
          		if (paw.isSolomonIslands())
          		{
          			ListItem item = new ListItem(getLabel("solomonIslands")); regions.add(item);
          		}
          		if (paw.isTonga())
          		{
          			ListItem item = new ListItem(getLabel("tonga")); regions.add(item);
          		}
          		if (paw.isTuvalu())
          		{
          			ListItem item = new ListItem(getLabel("tuvalu")); regions.add(item);
          		}
          		if (paw.isVanuatu())
          		{
          			ListItem item = new ListItem(getLabel("vanuatu")); regions.add(item);
          		}
          		if (paw.isOtherSouthPacific())
          		{
          			ListItem item = new ListItem(getLabel("otherSouthPacific")); regions.add(item);
          		}
          		if (paw.isPacificBasin())
          		{
          			ListItem item = new ListItem(getLabel("pacificBasin")); regions.add(item);
          		}
          		if (paw.isGlobal())
          		{
          			ListItem item = new ListItem(getLabel("global")); regions.add(item);
          		}
          		
        		
          		if (regions.size() > 0)
          		{
           		  PdfPCell rCell = new PdfPCell(); rCell.addElement(regions);
           		  table.addCell(new PdfPCell(new Phrase("Regions")));  table.addCell(rCell);
          		}
          		
          		
          		
          		
          		// text fields
          		
          		if (StringUtils.isNotBlank(description))
          		{
          		  table.addCell(new PdfPCell(new Phrase("Description")));  table.addCell(new PdfPCell(new Phrase(description)));
          		}
          		
          		if (StringUtils.isNotBlank(objectives))
          		{
          		  table.addCell(new PdfPCell(new Phrase("Objectives/Outcomes")));  table.addCell(new PdfPCell(new Phrase(objectives)));
          		}
          		
          		if (StringUtils.isNotBlank(leadAgencies))
          		{
          		  table.addCell(new PdfPCell(new Phrase("Lead Agencies")));  table.addCell(new PdfPCell(new Phrase(leadAgencies)));
          		}

          		if (StringUtils.isNotBlank(contacts))
          		{
          		  table.addCell(new PdfPCell(new Phrase("Contacts")));  table.addCell(new PdfPCell(new Phrase(contacts)));
          		}
                
          		if (StringUtils.isNotBlank(partnering))
          		{
          		  table.addCell(new PdfPCell(new Phrase("Partnering Agencies")));  table.addCell(new PdfPCell(new Phrase(partnering)));
          		}
          		
          		if (StringUtils.isNotBlank(resources))
          		{
          		  table.addCell(new PdfPCell(new Phrase("Required Resources")));  table.addCell(new PdfPCell(new Phrase(resources)));
          		}

          		if (StringUtils.isNotBlank(timelines))
          		{
          		  table.addCell(new PdfPCell(new Phrase("Projected Timelines")));  table.addCell(new PdfPCell(new Phrase(timelines)));
          		}
          		
          		if (StringUtils.isNotBlank(feedback))
          		{
          		  table.addCell(new PdfPCell(new Phrase("Feedback/Evaluation")));  table.addCell(new PdfPCell(new Phrase(feedback)));
          		}
          		
          		if (StringUtils.isNotBlank(url))
          		{
                  Anchor link = new Anchor(StringUtils.trimToEmpty(url)); link.setReference(StringUtils.trimToEmpty(url));
          		  table.addCell(new PdfPCell(new Phrase("Url")));  table.addCell(new PdfPCell(link));
          		}

          		
          		document.add(table);
          		document.add(Chunk.NEWLINE);
      		
              
              
      } catch (DocumentException de) {
              logger.fatal(de.getMessage());
      }
      catch (IOException ie)
      {
    	 logger.warn("Could not find NOAA logo (likely)");
    	 logger.warn(ie);
      }

      // step 5: we close the document
      document.close();
      ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
      return new PDFStreamResponse(bais,"PacisAssessment" + System.currentTimeMillis());
  }	  

  
  ///////////////////////////////////////////////////////
  // private methods 
  
  private void clearRegions(Paw paw)
  {
   	paw.setCentralNorthPacific(false);
  	paw.setWesternNorthPacific(false);
  	paw.setSouthPacific(false);
  	paw.setPacificBasin(false);
  	paw.setGlobal(false);
  }
  
  private void clearFocusArea(Paw paw)
  {
	paw.setWater(false);
	paw.setCoastal(false);
	paw.setEcosystem(false);
  }
  
  private void clearCapabilities(Paw paw)
  {
	paw.setVariability(false);
	paw.setImpacts(false);
  }

  private String getLabel (String varName)
  {
	   String key = varName + "-label";
	   String value = "";
	   if (messages.contains(key)) value = messages.get(key);
	   else value = TapestryInternalUtils.toUserPresentable(varName);
	   return StringUtils.trimToEmpty(value);
  }
}