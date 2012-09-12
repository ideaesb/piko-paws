package org.ideademo.pawz.entities;

import java.lang.Comparable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.search.annotations.DocumentId;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;

import org.ideademo.pawz.entities.Paw;

import org.apache.tapestry5.beaneditor.NonVisual;


@Entity @Indexed
public class Paw implements Comparable<Paw>
{
	
	//////////////////////////////////////////
	//Reserved indexing id 
	
	@Id @GeneratedValue @DocumentId @NonVisual
	private Long id;
	
	
	//////////////////////////////////////////////
	//String fields (being a keyword for Lucene)
	//
	
	@Field
	private String code="";

	@Field @Column (length=1024)
	private String name="";
	
	@Field  @Column (length=2048)
	private String organization="";
	
	@Field  @Column (length=2048)
	private String contact="";
	
	@Field  @Column (length=2048)
	private String partners="";

	@Field 
	private String url="";
	
	@Field @Column (length=4096)
	private String description="";
	
	@Field @Column (length=4096)
	private String keywords="";
	
	@Field  @Column (length=2048)
	private String objectives="";
	
	@Field 
	private String worksheet="";
	
	@Field   @Column (length=2048)
	private String dates="";
	
	@Field  @Column (length=2048) 
	private String resources="";
	
	@Field  @Column (length=2048)
	private String feedback="";
	

	//Status
	private boolean completed = false;
	private boolean ongoing = false;
	private boolean planned = false;
	private boolean proposed = false;
	
	
	//Priority Level 
	private boolean high = false;
	private boolean low = false;
	
	//Capability Area
    private boolean variability = false; 
        private boolean varObservations = false;
        private boolean varOperations = false;
        private boolean varResearch = false;
        private boolean varHistorical = false;
        private boolean varProjections = false;
        private boolean varTraining = false;
        private boolean varGuidance = false;
        private boolean varDecision = false;
        private boolean ecvAtmosphericSurface = false;
        private boolean ecvAtmosphericUpperAir = false;
        private boolean ecvAtmosphericComposition = false;
        private boolean ecvOceanicSurface = false;
        private boolean ecvOceanicSubsurface = false;
        private boolean ecvTerrestrial = false;
        private boolean varSeasonal = false;
        private boolean varIntraAnnual = false;
        private boolean varMultiDecadal = false;
        
    private boolean impacts = false; 
        private boolean climateImpacts = false;
           private boolean impObservations = false;
           private boolean impResearch = false;
           private boolean impHistorical = false;
           private boolean impProjections = false;
        private boolean climateAdaptation = false;
           private boolean impTraining = false;
           private boolean impGuidance = false;
           private boolean impDecision = false;
           private boolean impPolicies = false;
           private boolean impAssessment = false;

	
	//Focus Area
    private boolean water = false;
	private boolean coastal = false; 
	private boolean ecosystem = false;

	 //Region/Locale
	private boolean centralNorthPacific = false;
	    private boolean stateOfHawaii = false;
	    private boolean northWestHawaiianIslands = false;
	    private boolean pacificRemoteIslands = false;

	private boolean westernNorthPacific = false;
	    private boolean cnmi = false;
	    private boolean fsm = false;
	    private boolean guam = false;
	    private boolean palau = false;
	    private boolean rmi = false;
	    private boolean otherWesternNorthPacific = false;
	    
	private boolean southPacific = false;
	    private boolean americanSamoa = false;
	    private boolean australia = false;
	    private boolean cookIslands = false; 
	    private boolean fiji = false;
	    private boolean frenchPolynesia = false;
	    private boolean kiribati = false; 
	    private boolean newZealand = false;
	    private boolean png = false; 
	    private boolean samoa = false;
	    private boolean solomonIslands = false; 
	    private boolean tonga = false;
	    private boolean tuvalu = false; 
	    private boolean vanuatu = false; 
	    private boolean otherSouthPacific = false;
	    
	private boolean pacificBasin = false;
	private boolean global = false;

	// Sector
	private boolean health = false; 
	private boolean freshWater = false;
	private boolean energy = false;
	private boolean transportation = false;
	private boolean planning = false;
	private boolean socioCultural = false;
	private boolean agriculture = false;
	private boolean recreation = false;
	private boolean ecological = false;
	private boolean otherSector= false;

	
	////////////////
	// internal flag
	private boolean worksheetExists = false;

	
	////////////////////
	// getters and setters 
	//

	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
	}


	public String getCode() {
		return code;
	}


	public void setCode(String code) {
		this.code = code;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getOrganization() {
		return organization;
	}


	public void setOrganization(String organization) {
		this.organization = organization;
	}


	public String getContact() {
		return contact;
	}


	public void setContact(String contact) {
		this.contact = contact;
	}


	public String getPartners() {
		return partners;
	}


	public void setPartners(String partners) {
		this.partners = partners;
	}


	public String getUrl() {
		return url;
	}


	public void setUrl(String url) {
		this.url = url;
	}


	public String getDescription() {
		return description;
	}


	public void setDescription(String description) {
		this.description = description;
	}


	public String getKeywords() {
		return keywords;
	}


	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}


	public String getObjectives() {
		return objectives;
	}


	public void setObjectives(String objectives) {
		this.objectives = objectives;
	}


	public String getWorksheet() {
		return worksheet;
	}


	public void setWorksheet(String worksheet) {
		this.worksheet = worksheet;
	}


	public String getDates() {
		return dates;
	}


	public void setDates(String dates) {
		this.dates = dates;
	}


	public String getResources() {
		return resources;
	}


	public void setResources(String resources) {
		this.resources = resources;
	}


	public String getFeedback() {
		return feedback;
	}


	public void setFeedback(String feedback) {
		this.feedback = feedback;
	}


	public boolean isCompleted() {
		return completed;
	}


	public void setCompleted(boolean completed) {
		this.completed = completed;
	}


	public boolean isOngoing() {
		return ongoing;
	}


	public void setOngoing(boolean ongoing) {
		this.ongoing = ongoing;
	}


	public boolean isPlanned() {
		return planned;
	}


	public void setPlanned(boolean planned) {
		this.planned = planned;
	}


	public boolean isProposed() {
		return proposed;
	}


	public void setProposed(boolean proposed) {
		this.proposed = proposed;
	}


	public boolean isHigh() {
		return high;
	}


	public void setHigh(boolean high) {
		this.high = high;
	}


	public boolean isLow() {
		return low;
	}


	public void setLow(boolean low) {
		this.low = low;
	}


	public boolean isVariability() {
		return variability;
	}


	public void setVariability(boolean variability) {
		this.variability = variability;
	}


	public boolean isVarObservations() {
		return varObservations;
	}


	public void setVarObservations(boolean varObservations) {
		this.varObservations = varObservations;
	}


	public boolean isVarOperations() {
		return varOperations;
	}


	public void setVarOperations(boolean varOperations) {
		this.varOperations = varOperations;
	}


	public boolean isVarResearch() {
		return varResearch;
	}


	public void setVarResearch(boolean varResearch) {
		this.varResearch = varResearch;
	}


	public boolean isVarHistorical() {
		return varHistorical;
	}


	public void setVarHistorical(boolean varHistorical) {
		this.varHistorical = varHistorical;
	}


	public boolean isVarProjections() {
		return varProjections;
	}


	public void setVarProjections(boolean varProjections) {
		this.varProjections = varProjections;
	}


	public boolean isVarTraining() {
		return varTraining;
	}


	public void setVarTraining(boolean varTraining) {
		this.varTraining = varTraining;
	}


	public boolean isVarGuidance() {
		return varGuidance;
	}


	public void setVarGuidance(boolean varGuidance) {
		this.varGuidance = varGuidance;
	}


	public boolean isVarDecision() {
		return varDecision;
	}


	public void setVarDecision(boolean varDecision) {
		this.varDecision = varDecision;
	}


	public boolean isEcvAtmosphericSurface() {
		return ecvAtmosphericSurface;
	}


	public void setEcvAtmosphericSurface(boolean ecvAtmosphericSurface) {
		this.ecvAtmosphericSurface = ecvAtmosphericSurface;
	}


	public boolean isEcvAtmosphericUpperAir() {
		return ecvAtmosphericUpperAir;
	}


	public void setEcvAtmosphericUpperAir(boolean ecvAtmosphericUpperAir) {
		this.ecvAtmosphericUpperAir = ecvAtmosphericUpperAir;
	}


	public boolean isEcvAtmosphericComposition() {
		return ecvAtmosphericComposition;
	}


	public void setEcvAtmosphericComposition(boolean ecvAtmosphericComposition) {
		this.ecvAtmosphericComposition = ecvAtmosphericComposition;
	}


	public boolean isEcvOceanicSurface() {
		return ecvOceanicSurface;
	}


	public void setEcvOceanicSurface(boolean ecvOceanicSurface) {
		this.ecvOceanicSurface = ecvOceanicSurface;
	}


	public boolean isEcvOceanicSubsurface() {
		return ecvOceanicSubsurface;
	}


	public void setEcvOceanicSubsurface(boolean ecvOceanicSubsurface) {
		this.ecvOceanicSubsurface = ecvOceanicSubsurface;
	}


	public boolean isEcvTerrestrial() {
		return ecvTerrestrial;
	}


	public void setEcvTerrestrial(boolean ecvTerrestrial) {
		this.ecvTerrestrial = ecvTerrestrial;
	}


	public boolean isVarSeasonal() {
		return varSeasonal;
	}


	public void setVarSeasonal(boolean varSeasonal) {
		this.varSeasonal = varSeasonal;
	}


	public boolean isVarIntraAnnual() {
		return varIntraAnnual;
	}


	public void setVarIntraAnnual(boolean varIntraAnnual) {
		this.varIntraAnnual = varIntraAnnual;
	}


	public boolean isVarMultiDecadal() {
		return varMultiDecadal;
	}


	public void setVarMultiDecadal(boolean varMultiDecadal) {
		this.varMultiDecadal = varMultiDecadal;
	}


	public boolean isImpacts() {
		return impacts;
	}


	public void setImpacts(boolean impacts) {
		this.impacts = impacts;
	}


	public boolean isClimateImpacts() {
		return climateImpacts;
	}


	public void setClimateImpacts(boolean climateImpacts) {
		this.climateImpacts = climateImpacts;
	}


	public boolean isImpObservations() {
		return impObservations;
	}


	public void setImpObservations(boolean impObservations) {
		this.impObservations = impObservations;
	}


	public boolean isImpResearch() {
		return impResearch;
	}


	public void setImpResearch(boolean impResearch) {
		this.impResearch = impResearch;
	}


	public boolean isImpHistorical() {
		return impHistorical;
	}


	public void setImpHistorical(boolean impHistorical) {
		this.impHistorical = impHistorical;
	}


	public boolean isImpProjections() {
		return impProjections;
	}


	public void setImpProjections(boolean impProjections) {
		this.impProjections = impProjections;
	}


	public boolean isClimateAdaptation() {
		return climateAdaptation;
	}


	public void setClimateAdaptation(boolean climateAdaptation) {
		this.climateAdaptation = climateAdaptation;
	}


	public boolean isImpTraining() {
		return impTraining;
	}


	public void setImpTraining(boolean impTraining) {
		this.impTraining = impTraining;
	}


	public boolean isImpGuidance() {
		return impGuidance;
	}


	public void setImpGuidance(boolean impGuidance) {
		this.impGuidance = impGuidance;
	}


	public boolean isImpDecision() {
		return impDecision;
	}


	public void setImpDecision(boolean impDecision) {
		this.impDecision = impDecision;
	}


	public boolean isImpPolicies() {
		return impPolicies;
	}


	public void setImpPolicies(boolean impPolicies) {
		this.impPolicies = impPolicies;
	}


	public boolean isImpAssessment() {
		return impAssessment;
	}


	public void setImpAssessment(boolean impAssessment) {
		this.impAssessment = impAssessment;
	}


	public boolean isWater() {
		return water;
	}


	public void setWater(boolean water) {
		this.water = water;
	}


	public boolean isCoastal() {
		return coastal;
	}


	public void setCoastal(boolean coastal) {
		this.coastal = coastal;
	}


	public boolean isEcosystem() {
		return ecosystem;
	}


	public void setEcosystem(boolean ecosystem) {
		this.ecosystem = ecosystem;
	}


	public boolean isCentralNorthPacific() {
		return centralNorthPacific;
	}


	public void setCentralNorthPacific(boolean centralNorthPacific) {
		this.centralNorthPacific = centralNorthPacific;
	}


	public boolean isStateOfHawaii() {
		return stateOfHawaii;
	}


	public void setStateOfHawaii(boolean stateOfHawaii) {
		this.stateOfHawaii = stateOfHawaii;
	}


	public boolean isNorthWestHawaiianIslands() {
		return northWestHawaiianIslands;
	}


	public void setNorthWestHawaiianIslands(boolean northWestHawaiianIslands) {
		this.northWestHawaiianIslands = northWestHawaiianIslands;
	}


	public boolean isPacificRemoteIslands() {
		return pacificRemoteIslands;
	}


	public void setPacificRemoteIslands(boolean pacificRemoteIslands) {
		this.pacificRemoteIslands = pacificRemoteIslands;
	}


	public boolean isWesternNorthPacific() {
		return westernNorthPacific;
	}


	public void setWesternNorthPacific(boolean westernNorthPacific) {
		this.westernNorthPacific = westernNorthPacific;
	}


	public boolean isCnmi() {
		return cnmi;
	}


	public void setCnmi(boolean cnmi) {
		this.cnmi = cnmi;
	}


	public boolean isFsm() {
		return fsm;
	}


	public void setFsm(boolean fsm) {
		this.fsm = fsm;
	}


	public boolean isGuam() {
		return guam;
	}


	public void setGuam(boolean guam) {
		this.guam = guam;
	}


	public boolean isPalau() {
		return palau;
	}


	public void setPalau(boolean palau) {
		this.palau = palau;
	}


	public boolean isRmi() {
		return rmi;
	}


	public void setRmi(boolean rmi) {
		this.rmi = rmi;
	}


	public boolean isOtherWesternNorthPacific() {
		return otherWesternNorthPacific;
	}


	public void setOtherWesternNorthPacific(boolean otherWesternNorthPacific) {
		this.otherWesternNorthPacific = otherWesternNorthPacific;
	}


	public boolean isSouthPacific() {
		return southPacific;
	}


	public void setSouthPacific(boolean southPacific) {
		this.southPacific = southPacific;
	}


	public boolean isAmericanSamoa() {
		return americanSamoa;
	}


	public void setAmericanSamoa(boolean americanSamoa) {
		this.americanSamoa = americanSamoa;
	}


	public boolean isAustralia() {
		return australia;
	}


	public void setAustralia(boolean australia) {
		this.australia = australia;
	}


	public boolean isCookIslands() {
		return cookIslands;
	}


	public void setCookIslands(boolean cookIslands) {
		this.cookIslands = cookIslands;
	}


	public boolean isFiji() {
		return fiji;
	}


	public void setFiji(boolean fiji) {
		this.fiji = fiji;
	}


	public boolean isFrenchPolynesia() {
		return frenchPolynesia;
	}


	public void setFrenchPolynesia(boolean frenchPolynesia) {
		this.frenchPolynesia = frenchPolynesia;
	}


	public boolean isKiribati() {
		return kiribati;
	}


	public void setKiribati(boolean kiribati) {
		this.kiribati = kiribati;
	}


	public boolean isNewZealand() {
		return newZealand;
	}


	public void setNewZealand(boolean newZealand) {
		this.newZealand = newZealand;
	}


	public boolean isPng() {
		return png;
	}


	public void setPng(boolean png) {
		this.png = png;
	}


	public boolean isSamoa() {
		return samoa;
	}


	public void setSamoa(boolean samoa) {
		this.samoa = samoa;
	}


	public boolean isSolomonIslands() {
		return solomonIslands;
	}


	public void setSolomonIslands(boolean solomonIslands) {
		this.solomonIslands = solomonIslands;
	}


	public boolean isTonga() {
		return tonga;
	}


	public void setTonga(boolean tonga) {
		this.tonga = tonga;
	}


	public boolean isTuvalu() {
		return tuvalu;
	}


	public void setTuvalu(boolean tuvalu) {
		this.tuvalu = tuvalu;
	}


	public boolean isVanuatu() {
		return vanuatu;
	}


	public void setVanuatu(boolean vanuatu) {
		this.vanuatu = vanuatu;
	}


	public boolean isOtherSouthPacific() {
		return otherSouthPacific;
	}


	public void setOtherSouthPacific(boolean otherSouthPacific) {
		this.otherSouthPacific = otherSouthPacific;
	}


	public boolean isPacificBasin() {
		return pacificBasin;
	}


	public void setPacificBasin(boolean pacificBasin) {
		this.pacificBasin = pacificBasin;
	}


	public boolean isGlobal() {
		return global;
	}


	public void setGlobal(boolean global) {
		this.global = global;
	}


	public boolean isHealth() {
		return health;
	}


	public void setHealth(boolean health) {
		this.health = health;
	}


	public boolean isFreshWater() {
		return freshWater;
	}


	public void setFreshWater(boolean freshWater) {
		this.freshWater = freshWater;
	}


	public boolean isEnergy() {
		return energy;
	}


	public void setEnergy(boolean energy) {
		this.energy = energy;
	}


	public boolean isTransportation() {
		return transportation;
	}


	public void setTransportation(boolean transportation) {
		this.transportation = transportation;
	}


	public boolean isPlanning() {
		return planning;
	}


	public void setPlanning(boolean planning) {
		this.planning = planning;
	}


	public boolean isSocioCultural() {
		return socioCultural;
	}


	public void setSocioCultural(boolean socioCultural) {
		this.socioCultural = socioCultural;
	}


	public boolean isAgriculture() {
		return agriculture;
	}


	public void setAgriculture(boolean agriculture) {
		this.agriculture = agriculture;
	}


	public boolean isRecreation() {
		return recreation;
	}


	public void setRecreation(boolean recreation) {
		this.recreation = recreation;
	}


	public boolean isEcological() {
		return ecological;
	}


	public void setEcological(boolean ecological) {
		this.ecological = ecological;
	}


	public boolean isOtherSector() {
		return otherSector;
	}


	public void setOtherSector(boolean otherSector) {
		this.otherSector = otherSector;
	}


	public boolean isWorksheetExists() {
		return worksheetExists;
	}


	public void setWorksheetExists(boolean worksheetExists) {
		this.worksheetExists = worksheetExists;
	}
	
	
	////////////////////////////////////////////////
	/// default/natural sort order - String  - names
	
	public int compareTo(Paw ao) 
	{
	    boolean thisIsEmpty = false;
	    boolean aoIsEmpty = false; 
	    
	    if (this.getName() == null || this.getName().trim().length() == 0) thisIsEmpty = true; 
	    if (ao.getName() == null || ao.getName().trim().length() == 0) aoIsEmpty = true;
	    
	    if (thisIsEmpty && aoIsEmpty) return 0;
	    if (thisIsEmpty && !aoIsEmpty) return -1;
	    if (!thisIsEmpty && aoIsEmpty) return 1; 
	    return this.getName().compareToIgnoreCase(ao.getName());
    }
}
