package org.ideademo.pawz.pages;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.tapestry5.PersistenceConstants;

import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.Persist;


import org.apache.tapestry5.hibernate.HibernateSessionManager;

import org.apache.tapestry5.ioc.annotations.Inject;


import org.hibernate.Session;

import org.hibernate.criterion.Example;
import org.hibernate.criterion.MatchMode;

import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.query.dsl.QueryBuilder;

import org.ideademo.pawz.entities.Paw;


import org.apache.log4j.Logger;


public class Index 
{
	 
  private static Logger logger = Logger.getLogger(Index.class);

  
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
	
	// then makes lists and sublists as per the search criteria 
	List<Paw> xlst=null; // xlst = Query by Example search List
    if(example != null)
    {
       Example ex = Example.create(example).excludeFalse().ignoreCase().enableLike(MatchMode.ANYWHERE);
       
       xlst = session.createCriteria(Paw.class).add(ex).list();
       
       
       if (xlst != null)
       {
    	   logger.info("Example Search Result List Size  = " + xlst.size() );
    	   Collections.sort(xlst);
       }
       else
       {
         logger.info("Example Search result did not find any results...");
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
       org.apache.lucene.search.Query luceneQuery = qb
			    .keyword()
			    .onFields("code","name","description", "keywords","contact", "organization", "url", "worksheet", "partners","objectives", "dates", "resources", "feedback")
			    .matching(searchText)
			    .createQuery();
      	  
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
    	List <Paw> alst = session.createCriteria(Paw.class).list(); // alst = List of all records 
    	if (alst != null && alst.size() > 0)
    	{
    		logger.info ("Returing all " + alst.size() + " Paw records");
        	Collections.sort(alst);
    	}
    	else
    	{
    		logger.warn("No Paw records found in the database");
    	}
        return alst; 
    }
    else if (xlst == null && tlst != null)
    {
    	// just text search results
    	logger.info("Returing " + tlst.size() + " records as a result of PURE text search (no QBE) for " + searchText);
    	return tlst;
    }
    else if (xlst != null && tlst == null)
    {
    	// just example query results
    	logger.info("Returning " + xlst.size() + " records as a result ofPURE Query-By-Example (QBE), no text string");
    	return xlst;
    }
    else 
    {
    	// get the INTERSECTION of the two lists
    	
    	// TRIVIAL: if one of them is empty, return the other
    	if (xlst.size() == 0 && tlst.size() > 0) return tlst;
    	if (tlst.size() == 0 && xlst.size() > 0) return xlst;
    	
    	
    	List <Paw> ivec = new Vector<Paw>();
    	// if both are empty, return this Empty vector. 
    	if (xlst.size() == 0 && tlst.size() == 0) return ivec; 
    	
    	// now deal with BOTH text and QBE being non-empty lists - by Id
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

}